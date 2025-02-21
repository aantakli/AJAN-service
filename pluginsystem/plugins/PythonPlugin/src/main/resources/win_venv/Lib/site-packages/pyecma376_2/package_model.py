# Copyright 2020 by Michael Thies
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.
"""
Implementation of the logical OPC package model

This module provides abstract Reader and Writer classes for logical OPC packages, independent from concrete physical
mappings, as well as some auxiliary classes and functions for the logical package model. For reading and writing
actual OPC package files, Reader and Writer classes for a physical OPC package mapping are required, i.e. those from
the `zip_package` module.
"""

import abc
import collections
import enum
import io
import re
import types
import urllib.parse
# We need lxml's ElementTree implementation, as it allows correct handling of default namespaces (xmlns="…") when
# writing XML files. And since we already have it, we also use the iterative writer.
import lxml.etree as etree  # type: ignore
from typing import BinaryIO, Sequence, Dict, Iterable, NamedTuple, Optional, IO, Generator, List, DefaultDict, Tuple

from .core_properties import OPCCoreProperties

RE_RELS_PARTS = re.compile(r'^(.*/)_rels/([^/]*).rels$', re.IGNORECASE)
RE_FRAGMENT_ITEMS = re.compile(r'^(.*)/\[(\d+)\](.last)?.piece$', re.IGNORECASE)
RELATIONSHIPS_XML_NAMESPACE = "{http://schemas.openxmlformats.org/package/2006/relationships}"

RELATIONSHIP_TYPE_CORE_PROPERTIES = \
    "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties"
RELATIONSHIP_TYPE_THUMBNAIL = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail"
RELATIONSHIP_TYPE_DIGITAL_SIG_ORIGIN = \
    "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/origin"


class OPCPackageReader(metaclass=abc.ABCMeta):
    """
    Abstract implementation of a Reader for logical OPC packages.

    This class provides the base for implementing concrete physical package readers. It implements reading functionality
    of the logical package model (Parts, Relationships, ContentTypes, mapping to logical Items), but omits the mapping
    to a physical package format (reading of physical Items). Descendant classes need to override the abstract methods
    `list_items()` and `open_item()` to implement access to the physical package. They also may override the `close()`
    method.

    Objects of this class (resp. descendant classes) should be used as context managers: They (somhow) open the physical
    package at construction time and close it when the `close()` method is called or the with-context is exited.
    """
    content_types_stream_name: Optional[str] = None

    class _PartDescriptor(types.SimpleNamespace):
        content_type: str
        fragmented: bool
        physical_item_name: str

    def __init__(self):
        """
        The initialization method.

        Descendant classes should override the __init__() method and at least implement the following steps:
        * calling this init method (`super().__init__()`)
        * open/prepare the physical package for reading (so `list_items()` and `open_item()` will work)
        * call `self._init_data()`
        """
        # dict mapping all known normalized part names to (content type, fragmented, physical item name)
        self._parts: Dict[str, OPCPackageReader._PartDescriptor] = {}
        # A cache for the get_related_parts_by_type() method
        self._related_parts_cache: Dict[str, DefaultDict[str, List[str]]] = {}

    def _init_data(self) -> None:
        """
        Part of the initializer, which must be called after the physical package has been opened, to initialize
        cached part data.

        Must be called by each concrete descendant class in its __init__() method after opening the physical package.
        """
        # First run: Find all parts (including the Content Types Stream)
        for item_name in self.list_items():
            fragment_match = RE_FRAGMENT_ITEMS.match(item_name)
            if fragment_match:
                if fragment_match[2] != "0":
                    continue
                part_name = fragment_match[1]
                self._parts[normalize_part_name(part_name)] = self._PartDescriptor(content_type="", fragmented=True,
                                                                                   physical_item_name=part_name)
            else:
                self._parts[normalize_part_name(item_name)] = self._PartDescriptor(content_type="", fragmented=False,
                                                                                   physical_item_name=item_name)

        # Read ContentTypes data and update parts' data, remove ContentTypesStream part afterwards
        if self.content_types_stream_name is not None:
            with self.open_part(self.content_types_stream_name) as part:
                content_types = ContentTypesData.from_xml(part)
            for part_name, part_record in self._parts.items():
                if part_name == self.content_types_stream_name:
                    continue
                content_type = content_types.get_content_type(part_name)
                # TODO log warning if no content_type given
                if content_type is not None:
                    part_record.content_type = content_type
            del self._parts[self.content_types_stream_name]

    def list_parts(self, include_rels_parts=False) -> Iterable[Tuple[str, str]]:
        """
        Get all Parts in this package with part name and content type.

        Relationship XML parts can optionally be included into the result. The ContentTypesStream is never included.

        :param include_rels_parts: If True, Relationship XML parts are included into the result.
        :return: An iterator over a tuple (part name, content type) for each part
        """
        return ((part_descriptor.physical_item_name, part_descriptor.content_type)
                for normalized_name, part_descriptor in self._parts.items()
                if include_rels_parts or not RE_RELS_PARTS.match(normalized_name))

    def get_content_type(self, part_name: str) -> str:
        """
        Get a Part's content type.

        :param part_name: An (absolute) part name
        :return: The Part's content type
        :raises KeyError: If the Part does not exist in the package
        """
        return self._parts[normalize_part_name(part_name)].content_type

    def open_part(self, name: str) -> BinaryIO:
        """
        Open a Part of the package by its part name.

        The returned value is a file-like object with binary read functionality. It may or may not be seekable (use the
        usual `seekable()` method to find out. It will most probably not support writing or even may behave in an
        undefined way when used for writing.

        The returned file-like object shall be closed after usage, using the `.close()` method or using it as a context
        manager.

        :param name: The (abolsute) part name (must start with a '/')
        :return: A binary file-like object for reading the part
        """
        try:
            part_descriptor = self._parts[normalize_part_name(name)]
        except KeyError as e:
            raise KeyError(f"Could not find part {name} in package") from e
        if part_descriptor.fragmented:
            return FragmentedPartReader(part_descriptor.physical_item_name, self)  # type: ignore
        else:
            return self.open_item(part_descriptor.physical_item_name)

    def get_raw_relationships(self, part_name: str = "/") -> Generator["OPCRelationship", None, None]:
        """
        Get an iterator over the relationships of a specific part (or the package itself)

        :param part_name: The (absolute) part name of the source Part of the requested Relationships. If not specified
            or equal to "/", the package root's relationships are returned.
        :return: An iterator over the Relationships with the specified source Part as OPCRelationship objects.
        """
        try:
            rels_part = self.open_part(_rels_part_for(part_name))
        except KeyError:
            return
        yield from self._read_relationships(rels_part)

    def get_related_parts_by_type(self, part_name: str = "/") -> DefaultDict[str, List[str]]:
        """
        Get a dict of all *internal* relationships / related Parts by Relationship type from a given source Part.

        This function reads the relationships with the given source Part and groups all internal Relationships by type.
        It uses an internal cache to avoid reading and grouping the relationships of the same part multiple times from
        the package.

        :param part_name: The (absolute) part name of the source Part of the requested Relationships. If not specified
            or equal to "/", the package root's relationships are returned.
        :return: A dict, mapping Relationship types to a list of the target Parts of all Relationships with the given
            source Part and this type. The target Parts' names are transformed to absolute paths, i.e. starting with
            '/', to be used with `open_part()` or `get_content_type()`. Since the result is a defaultdict, requesting
            any non-occurring Relationship type will yield an empty list.
        """
        part_name = normalize_part_name(part_name)
        if part_name in self._related_parts_cache:
            return self._related_parts_cache[part_name]
        result = collections.defaultdict(list)  # type: DefaultDict[str, List[str]]
        for relationship in self.get_raw_relationships(part_name):
            if relationship.target_mode == OPCTargetMode.INTERNAL:
                result[relationship.type].append(part_realpath(relationship.target, part_name))
        self._related_parts_cache[part_name] = result
        return result

    def get_core_properties(self) -> OPCCoreProperties:
        """
        Convenience method to find, read and parse the package's Core Properties.

        If the package does not contain a Core Properties Part, an empty Core Properties object is returned. If more
        than one Core Properties Parts are contained in the package (which is not inadmissible according to the
        standard), only the first one is parsed.
        """
        rels = self.get_related_parts_by_type()
        try:
            part_name = rels[RELATIONSHIP_TYPE_CORE_PROPERTIES][0]
        except IndexError:
            return OPCCoreProperties()
        with self.open_part(part_name) as p:
            return OPCCoreProperties.from_xml(p)

    def close(self) -> None:
        """
        Close the PackageReader and the underlying physical package.

        You may as well use the PackageReader as a context manager to make sure it is closed correctly:

            with SomePackageReader(...) as reader:
                reader.open_part(...)
                ...

        This method should be overridden by a descendant class to close the physical package file.
        """
        pass

    def __enter__(self):
        return self

    def __exit__(self, _exc_type, _exc_val, _exc_tb):
        self.close()

    @staticmethod
    def _read_relationships(rels_part: IO[bytes]) -> Generator["OPCRelationship", None, None]:
        """ Internal helper method for parsing a Relationship XML part """
        for _event, elem in etree.iterparse(rels_part):
            if elem.tag == RELATIONSHIPS_XML_NAMESPACE + "Relationship":
                yield OPCRelationship(
                    elem.attrib["Id"],
                    elem.attrib["Type"],
                    elem.attrib["Target"],
                    OPCTargetMode.from_serialization(elem.attrib.get('TargetMode', 'Internal')))
                elem.clear()

    @abc.abstractmethod
    def list_items(self) -> Iterable[str]:
        """
        List the (logical) names of all items in the underlying physical package.

        This function must be overridden by concrete physical mappings of the pacakge model. It must implement the
        mapping of physical item names to logical item names. It must *not* convert item names to part names nor
        normalize the names. It shall not filter the list of item names.
        """
        pass

    @abc.abstractmethod
    def open_item(self, name: str) -> BinaryIO:
        """
        Internal method to open an item of the underlying physical package by logical item name.

        This function must be overridden by concrete physical mappings of the pacakge model. It must implement the
        mapping of logical item names to physical item names and access to the physical bytestream. It does *not* need
        to implement normaliziation of logical item names.

        :param name: The logical item name as it is present in the physical package (e.g. "/[Content_Types].xml",
            "/some/document.xml/[1].piece").
        :return: A readable file-like object for the item
        :raises KeyError: If no such item exists in the physical package
        """
        pass


class FragmentedPartReader(io.RawIOBase):
    """
    Helper class for reading fragmented/interleaved Parts like a single file.

    This class behaves like a readable, non-seekable, binary file-like object. Its read() method reads from all
    items/fragments of the Part sequentially, opening the next item as soon as the current is empty.
    """
    def __init__(self, name: str, reader: OPCPackageReader):
        self._name: str = name
        self._reader: OPCPackageReader = reader
        self._fragment_number: int = 0
        self._finished = False
        self._current_item_handle: IO[bytes]
        self._open_next_item()

    def _open_next_item(self) -> None:
        try:
            self. _current_item_handle = self._reader.open_item(f"{self._name}/[{self._fragment_number}].piece")
            self._fragment_number += 1
        except KeyError:
            self._finished = True
            try:
                self._current_item_handle = self._reader.open_item(f"{self._name}/[{self._fragment_number}].last.piece")
                self._fragment_number += 1
            except KeyError as e:
                raise KeyError(f"Fragment {self._fragment_number} of part {self._name} is missing in package") from e

    def seekable(self) -> bool:
        return False

    def read(self, size: int = -1) -> Optional[bytes]:
        """
        Read new bytes from the (fragmented) part.

        This function issues only one read() request / system call to the underlying physical package, unless
        size == -1. This is compatible to the behavior of Python file objects.

        :param size: Maximum number of bytes to be read. If size == -1, multiple system calls are issued to read the
            whole part at once.
        :return: The next bytes of the part or None if the underlying physical item is a stream and no bytes are
            currently available.
        """
        result = self._current_item_handle.read(size)
        while result is not None and (size == -1 or 0 == len(result) < size) and not self._finished:
            self._current_item_handle.close()
            self._open_next_item()
            result += self._current_item_handle.read(size)
        return result

    def close(self) -> None:
        self._current_item_handle.close()


class OPCPackageWriter(metaclass=abc.ABCMeta):
    """
    Abstract implementation of a Writer for logical OPC packages.

    This class provides the base for implementing concrete physical package writers. It implements writing functionality
    of the logical package model (Parts, Relationships, ContentTypes, mapping to logical Items), but omits the mapping
    to a physical package format (writing of Items). Descendant classes need to override the abstract method
    `create_item()` to implement access to the physical package. They also may override the `close()` method.

    Objects of this class (resp. descendant classes) should be used as context managers: They (somhow) open the physical
    package at construction time and close it when the `close()` method is called or the with-context is exited.
    """
    content_types_stream_name: Optional[str] = None

    def __init__(self):
        if self.content_types_stream_name is not None:
            self.content_types = ContentTypesData()
            self.content_types_written = False

    def open_part(self, name: str, content_type: str) -> BinaryIO:
        """
        Create a new Part with the given part name and open it as file-like object for writing.

        The part name shall be unique. Each part shall only be created/written once. The returned file-like object shall
        be closed after usage, using the `.close()` method or using it as a context manager.

        This method will make sure that the Part's Content Type is correctly specified in the Package. If the physical
        package format does not support native part Content Types and the Part's Content Type is currently not correctly
        reflected by the `content_types.default_types`, a Content Type override for this part will be added.

        :param name: The new Part's part name. Must be an absolute and unique name in URI notation, i.e. starting with
            a '/', occur for the first time, and not contain non-ASCII characters.
        :param content_type: The new Part's content type
        :return: A writable, binary file-like object to write the contents of the Part into it
        :raises RuntimeError: If a Content Type override must be added, but the ContentTypesStream has already been
            written.
        """
        check_part_name(name)
        if self.content_types_stream_name is not None:
            if self.content_types.get_content_type(name) != content_type:
                if self.content_types_written:
                    raise RuntimeError(f"Content Type of part {name} is not set correctly "
                                       f"but ContentTypeStream has been written already.")
                else:
                    self.content_types.overrides[name] = content_type
        return self.create_item(name, content_type)

    def write_relationships(self, relationships: Iterable["OPCRelationship"], part_name: str = "/") -> None:
        """
        Create and write the accompanying Relationships part for a given Part

        This method must only be called once for each Part.

        :param relationships: The list of relationships to be added
        :param part_name: The part name of the source Part of the relationships (as absolute, URI path)
        """
        # We do currently not support fragmented relationships parts
        if part_name != "/":
            # "/" is a special case, as it is allowed to end on a "/"
            check_part_name(part_name)
        with self.open_part(_rels_part_for(part_name), "application/vnd.openxmlformats-package.relationships+xml") as i:
            self._write_relationships(i, relationships)

    def create_fragmented_part(self, name: str, content_type: str) -> "FragmentedPartWriterHandle":
        """
        Create a new fragmented/interleaved Part with the given part name

        The part name shall be unique. Each part shall only be created once.

        This function returns a handle which can be used to open individual fragments (items) of the part, using its
        `open()` method. The handle's `open()` method returns file-like object for writing the Part fragment's contents
        and shall be closed after usage, using the `.close()` method or using it as a context manager:

            with SomePackageWriter(...) as writer:
                handle = writer.create_fragmented_part("/foo.txt", "text/plain")
                with handle.open() as f:
                    f.write(b"Hello, ")
                with writer.open_part("/bar.txt", "text/plain") as f:
                    f.write(b"Other part's contents")
                with handle.open(last=True) as f:
                    f.write(b"World!")

        This method will make sure that the Part's Content Type is correctly specified in the Package. If the physical
        package format does not support native part Content Types and the Part's Content Type is currently not correctly
        reflected by the `content_types.default_types`, a Content Type override for this part will be added.

        :param name: The new Part's part name. Must be an absolute and unique name in URI notation, i.e. starting with
            a '/', occur for the first time, and not containing non-ASCII characters.
        :param content_type: The new Part's content type
        :return: A handle to create and open individual fragments of this part
        :raises RuntimeError: If a Content Type override must be added, but the ContentTypesStream has already been
            written.
        """
        check_part_name(name)
        if self.content_types_stream_name is not None:
            if self.content_types.get_content_type(name) != content_type:
                if self.content_types_written:
                    raise RuntimeError(f"Content Type of part {name} is not set correctly "
                                       f"but ContentTypeStream has been written already.")
                else:
                    self.content_types.overrides[name] = content_type
        return FragmentedPartWriterHandle(name, content_type, self)

    def close(self) -> None:
        """
        Close the PackageWriter and the underlying physical package.

        This method will also trigger writing the Content
        You may as well use the PackageWriter as a context manager to make sure it is always closed correctly:

            with SomePackageReader(...) as reader:
                reader.open_part(...)
                ...

        This method should be overridden by a descendant class to close the physical package file. The overriding
        function must call this super-type method *before* closing the physical file.
        """
        if self.content_types_stream_name is not None:
            self.write_content_types_stream()

    def __enter__(self):
        return self

    def __exit__(self, _exc_type, _exc_val, _exc_tb):
        self.close()

    def write_content_types_stream(self) -> None:
        """
        Prematurely create and write the ContentTypesStream into the physical package, based on the `content_types`

        This method may be used to control, when the ContentTypesStream ("/[Content_Types].xml" in ZIP packages) is
        written into the physical package. If this method is not called by the user, the ContentTypesStream is
        written when closing the PackageWriter, i.e. at the (physical) end of the package file/stream.

        After this method has been called, modifications to the `content_types` will have no effect anymore. Thus,
        all Parts' Content Types must already be known and included in the `content_types` (either as Default Type or as
        an Override) when calling `write_content_types_stream()`.

        :raises RuntimeError: If the physical package type supports native Content Types, so no ContentTypesStream is
            required.
        """
        # We do currently not support interleaved Content Types Streams yet
        if self.content_types_stream_name is None:
            raise RuntimeError("Physical Package Format uses native content types. No Content Types Stream is required")
        if self.content_types_written:
            return
        with self.create_item(self.content_types_stream_name, "application/xml") as i:
            self.content_types.write_xml(i)
        self.content_types_written = True

    @abc.abstractmethod
    def create_item(self, name: str, content_type: str) -> BinaryIO:
        """
        Internal method to create and open an item of the underlying physical package by logical item name.

        This function must be overridden by concrete physical mappings of the pacakge model. It must implement the
        mapping of logical item names to physical item names and access to the physical bytestream. It does *not* need
        to implement normaliziation of logical item names.

        :param name: The logical item name of the item to be created (e.g. "/[Content_Types].xml",
            "/foo.txt/[1].piece").
        :return: A binary, writable file-like object for the item
        """
        pass

    @staticmethod
    def _write_relationships(rels_part: IO[bytes], relationships: Iterable["OPCRelationship"]) -> None:
        """ Internal helper function to serialize and write a list of Relationships into an XML Relationships part """
        with etree.xmlfile(rels_part, encoding="UTF-8") as xf:
            xf.write_declaration()
            with xf.element(RELATIONSHIPS_XML_NAMESPACE + "Relationships",
                            nsmap={None: RELATIONSHIPS_XML_NAMESPACE[1:-1]}):
                for relationship in relationships:
                    with xf.element(RELATIONSHIPS_XML_NAMESPACE + 'Relationship', {
                            'Target': relationship.target,
                            'Id': relationship.id,
                            'Type': relationship.type,
                            'TargetMode': relationship.target_mode.serialize()}):
                        # This is a bit strange, but the only way to use lxml's incremental XML serialization, which is
                        # required here, to achieve a consistent Namespace handling with the parent elements
                        pass


class FragmentedPartWriterHandle:
    """
    Handle for writing fragmented/interleaved Parts via OPCPackageWriter.

    Objects of this class are created by `OPCPackageWriter.create_fragmented_part()`. They provide an `open()` method
    to open each individual item/fragement of the Part for writing.
    """
    def __init__(self, name: str, content_type: str, writer: OPCPackageWriter):
        self.name: str = name
        self.content_type = content_type
        self.writer: OPCPackageWriter = writer
        self.fragment_number: int = 0
        self.finished = False

    def open(self, last: bool = False) -> BinaryIO:
        """
        Open a new fragement/item of the fragmented Part described by this handle for writing.

        The returned file-like object must be closed after usage, using the `.close()` method or using it as a context
        manager.
        The `last` argument *must* be set to True when opening the last fragment of the part. Afterwards, no other
        fragment can be opened anymore.

        :param last: True if this will be the last fragment of the Part, False otherwise
        :return: A writable, binary object to write the fragment's contents.
        :raises RuntimeError: when trying to open another fragment after the `last` fragment.
        """
        if self.finished:
            raise RuntimeError(f"Fragmented Part {self.name} has already been finished")
        f = self.writer.create_item(f'{self.name}/[{self.fragment_number}]{".last" if last else ""}.piece',
                                    self.content_type)
        self.fragment_number += 1
        self.finished = last
        return f


class OPCTargetMode(enum.Enum):
    """ Enum representation of the TargetMode attribute of an OPCRelationship. """
    INTERNAL = 1
    EXTERNAL = 2

    @classmethod
    def from_serialization(cls, serialization: str) -> "OPCTargetMode":
        return cls[serialization.upper()]

    def serialize(self) -> str:
        return self.name.capitalize()


class OPCRelationship(NamedTuple):
    """
    Tuple representation of a Relationship within an OPC package. From the OPC specs:

    * The Id type is xsd:ID and it shall conform to the naming restrictions for xsd:ID as specified in the W3C
      Recommendation “XML Schema Part 2: Datatypes.” The value of the Id attribute shall be unique within the
      Relationships part.
    * The package implementer shall require the Type attribute to be a URI that defines the role of the relationship and
      the format designer shall specify such a Type.
    * The package implementer shall require the Target attribute to be a URI reference pointing to a target resource.
      The URI reference shall be a URI or a relative reference.
    * The TargetMode indicates whether or not the target describes a resource inside the package or outside the package.
    """
    id: str
    type: str
    target: str
    target_mode: OPCTargetMode


class ContentTypesData:
    """
    Class to represent the "ContentTypesStream" used to represent the Content Types of OPC packages without native
    content types."""
    XML_NAMESPACE = "{http://schemas.openxmlformats.org/package/2006/content-types}"

    def __init__(self):
        self.default_types: Dict[str, str] = {}   # dict mapping file extensions to mime types
        self.overrides: Dict[str, str] = {}   # dict mapping part names to mime types

    def get_content_type(self, part_name: str) -> Optional[str]:
        """
        Get the Content Type of the part with the given name, according to this ContentTypesStream data.

        :param part_name: An (absolute) part name
        :return: The content type of the part, or None if it has not been defined (neither by a Default nor by an
            Override)
        """
        part_name = normalize_part_name(part_name)
        if part_name in self.overrides:
            return self.overrides[part_name]
        extension = part_name.split("/")[-1].split(".")[-1]
        if extension in self.default_types:
            return self.default_types[extension]
        return None

    @classmethod
    def from_xml(cls, content_types_file: IO[bytes]) -> "ContentTypesData":
        result = cls()
        for _event, elem in etree.iterparse(content_types_file):
            if elem.tag == cls.XML_NAMESPACE + "Default":
                result.default_types[elem.attrib["Extension"].lower()] = elem.attrib["ContentType"]
                elem.clear()
            elif elem.tag == cls.XML_NAMESPACE + "Override":
                result.overrides[normalize_part_name(elem.attrib["PartName"])] = elem.attrib["ContentType"]
                elem.clear()
        return result

    def write_xml(self, file: IO[bytes]) -> None:
        with etree.xmlfile(file, encoding="UTF-8") as xf:
            xf.write_declaration()
            with xf.element(self.XML_NAMESPACE + "Types",
                            nsmap={None: self.XML_NAMESPACE[1:-1]}):
                for extension, content_type in self.default_types.items():
                    with xf.element(self.XML_NAMESPACE + 'Default',
                                    {'Extension': extension, 'ContentType': content_type}):
                        # This is a bit strange, but the only way to use lxml's incremental XML serialization, which is
                        # required here, to achieve a consistent Namespace handling with the parent elements
                        pass
                for part_name, content_type in self.overrides.items():
                    with xf.element(self.XML_NAMESPACE + 'Override',
                                    {'PartName': part_name, 'ContentType': content_type}):
                        # ... same here
                        pass


def _rels_part_for(part_name: str) -> str:
    """ Get the name of the XML part with the relationships of `part_name` according to the OPC spec """
    name_parts = part_name.split("/")
    return "/".join(name_parts[:-1] + ["_rels", name_parts[-1]]) + ".rels"


def normalize_part_name(part_name: str) -> str:
    """ Converts a part name to the normalized URI representation (i.e. uschars are %-encoded) and to lowercase """
    part_name = urllib.parse.quote(part_name, safe='/#%[]=:;$&()+,!?*@\'~')
    return part_name.lower()


RE_PART_NAME = re.compile(r'^(/[A-Za-z0-9\-\._~%:@!$&\'()*+,;= ]*[A-Za-z0-9\-_~%:@!$&\'()*+,;= ])+$')
RE_PART_NAME_FORBIDDEN = re.compile(r'%5c|%2f', re.IGNORECASE)


def check_part_name(part_name: str) -> None:
    """ Check if `part_name` is a valid OPC part name in URI (not IRI) representation.

    :raises ValueError: if it is not.
    """
    if not RE_PART_NAME.match(part_name):
        raise ValueError(f"{repr(part_name)} is not an URI path with multiple segments "
                         f"(each not empty and not starting with '.') "
                         "or not starting with '/' or ending with '/'")
    if RE_PART_NAME_FORBIDDEN.search(part_name):
        raise ValueError(f"{repr(part_name)} contains URI encoded '/' or '\\'.")


def part_realpath(part_name: str, source_part_name: str) -> str:
    """ Get an absolute part name from a relative part name (e.g. from a relationship)

    @:param part_name: A relative or absolute part name to be transformed
    @:param source_part_name: Base part (source of the relationship etc.) to use as starting point of part_name if it
        is a relative path
    """
    if part_name[0] == "/":
        return part_name
    path_segments = part_name.split("/")
    result = source_part_name.split("/")[:-1]
    for segment in path_segments:
        if segment in ('.', ''):
            pass
        elif segment == '..':
            result.pop()
        else:
            result.append(segment)
    return "/".join(result)
