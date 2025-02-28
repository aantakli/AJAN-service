# Copyright 2020-2021 by Michael Thies
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
This module provides a Python model of the OPC Core Properties (according to ECMA 376-2, section 11.). All available
properties (meta attributes) of a package are represented by an object of the `OPCCoreProperties` class. Additionally,
this module defines some helper functions to parse or serialize OPCCoreProperties objects from/into XML documents.
"""

import datetime
import re
from typing import Optional, List, Tuple, IO, Callable, Any, Union

import lxml.etree as etree  # type: ignore

XML_NAMESPACE = "{http://schemas.openxmlformats.org/package/2006/metadata/core-properties}"
XML_NAMESPACE_DC = "{http://purl.org/dc/elements/1.1/}"
XML_NAMESPACE_DCTERMS = "{http://purl.org/dc/terms/}"
XML_NAMESPACE_XSI = "{http://www.w3.org/2001/XMLSchema-instance}"
XML_NAMESPACE_XML = "{http://www.w3.org/XML/1998/namespace}"


def _extract_str(el: etree.Element) -> str:
    """
    Helper function to extract the contained text from an XML tag. To be used as extractor function in OPCCoreProperties
    XML for all standard string fields.
    """
    return el.text


def _to_xml_tag(tag: str, content: str) -> etree.Element:
    """
    Helper function to create an XML tag with a simple text content. To be used as a serializer for all string
    attributes in OPCCoreProperties.
    """
    result = etree.Element(tag)
    result.text = content
    return result


DATETIME_RE = re.compile(r'(\d\d\d\d)(-(\d\d)(-(\d\d)(T(\d\d)(:(\d\d)(:(\d\d)(\.\d+)?([+\-](\d\d):(\d\d)|Z)?)?)?)?)?)?')


def _extract_datetime(el: etree.Element) -> Optional[Union[datetime.datetime, datetime.date]]:
    """
    Extract date or datetime value from W3CDTF encoding (https://www.w3.org/TR/NOTE-datetime), contained in the text of
    an XML tag. To be used as a serializer function for the date attributes of OPCCoreProperties.
    """
    match = DATETIME_RE.match(el.text)
    if not match:
        # TODO log warning?
        return None
    if match[6]:
        return datetime.datetime(
            int(match[1]),
            int(match[3]) if match[3] else 0,
            int(match[5]) if match[5] else 0,
            int(match[7]) if match[7] else 0,
            int(match[9]) if match[9] else 0,
            int(match[11]) if match[11] else 0,
            int(float(match[12]) * 1e6) if match[12] else 0,
            _parse_xsd_date_tzinfo(match[13])
        )
    else:
        return datetime.date(
            int(match[1]),
            int(match[3]) if match[3] else 0,
            int(match[5]) if match[5] else 0,
        )


def _parse_xsd_date_tzinfo(value: str) -> Optional[datetime.tzinfo]:
    """ Helper function for `_extract_datetime()` to parse tzinfo part of a W3CDTF string. """
    if not value:
        return None
    if value == "Z":
        return datetime.timezone.utc
    return datetime.timezone(datetime.timedelta(hours=int(value[1:3]), minutes=int(value[4:6]))
                             * (-1 if value[0] == '-' else 1))


def _datetime_to_xml(tag: str, content: Union[datetime.date, datetime.datetime]) -> etree.Element:
    """
    Serialize date or datetime value into an XML tag in W3CDTF encoding (https://www.w3.org/TR/NOTE-datetime). To be
    used as a serializer function for the date attributes of OPCCoreProperties.
    """
    text: str
    if isinstance(content, (datetime.date, datetime.datetime)):
        text = content.isoformat()
    else:
        # Allow string pass through
        text = content  # type: ignore
    return _to_xml_tag(tag, text)


def _extract_keywords(el: etree.Element) -> List[Tuple[Optional[str], str]]:
    """
    Extract OPC Core Properties keywords list from a complex tag. To be used as a extractor function for the
    OPCCoreProperties.keywords attribute.
    """
    default_lang = el.attrib.get(XML_NAMESPACE_XML + "lang", None)
    result = []
    if el.text is not None and el.text.strip():
        result.append((default_lang, el.text.strip()))
    for key_tag in el:
        result.append((key_tag.attrib.get(XML_NAMESPACE_XML + "lang", default_lang), key_tag.text.strip()))
        if key_tag.tail is not None and key_tag.tail.strip():
            result.append((default_lang, key_tag.tail.strip()))
    return result


def _keywords_to_xml(tag: str, content: List[Tuple[Optional[str], str]]) -> etree.Element:
    """
    Serialize OPC Core Properties keywords list into a complex XML tag. To be used as a serializer function for the
    OPCCoreProperties.keywords attribute.
    """
    result = etree.Element(tag)
    for lang, keyword in content:
        key_tag = etree.Element(XML_NAMESPACE + "value")
        key_tag.text = keyword
        if lang:
            key_tag.attrib[XML_NAMESPACE_XML + 'lang'] = lang
        result.append(key_tag)
    return result


class OPCCoreProperties:
    """
    Class to represent the Core Properties Part, containing the meta data of an OPC package.

    The Core Properties are stored as an XML part in the package and referenced via a reference of type
    http://schemas.openxmlformats.org/.../core-properties from the package root.
    """
    def __init__(self):
        # Dublin Core does not define concrete Types for its attributes. However, we expect 'created', 'modified' and
        # 'lastPrinted' to be datetime-ish values in the dcterms:W3CDTF format (https://www.w3.org/TR/NOTE-datetime).

        self.category: Optional[str] = None  # A categorization of the content of this package. (e.g. "Letter")
        self.contentStatus: Optional[str] = None  # The status of the content. (e.g. "Draft", "Final")
        self.created: Optional[Union[datetime.date, datetime.datetime]] = None  # Date of creation of the resource.
        self.creator: Optional[str] = None  # An entity primarily responsible for making the content of the resource.
        self.description: Optional[str] = None  # An explanation of the content of the resource.
        self.identifier: Optional[str] = None  # An unambiguous reference to the resource within a given context.
        # A list of keywords to support searching and indexing. Each Keyword may or may not have its language specified
        # (using XML lang strings).
        self.keywords: Optional[List[Tuple[Optional[str], str]]] = None
        self.language: Optional[str] = None  # The language of the intellectual content of the resource (see RFC 3066).
        self.lastModifiedBy: Optional[str] = None  # The user who performed the last modification.
        # The date and time of the last printing.
        self.lastPrinted: Optional[Union[datetime.date, datetime.datetime]] = None
        # Date on which the resource was changed.
        self.modified: Optional[Union[datetime.date, datetime.datetime]] = None
        self.revision: Optional[str] = None  # The revision number.
        self.subject: Optional[str] = None  # The topic of the content of the resource.
        self.title: Optional[str] = None  # The name given to the resource.
        self.version: Optional[str] = None  # The version number. This value is set by the user or by the application.

    # Yay, some dynamic coding. This tuple defines all attributes of the object as well as the corresponding XML tag
    # and the transformation functions for converting the XML string value into the correct python type and vice versa.
    ATTRIBUTES: Tuple[Tuple[str, str, Callable[[etree.Element], Any], Callable[[str, Any], etree.Element]], ...] = (
        ('category', XML_NAMESPACE + 'category', _extract_str, _to_xml_tag),
        ('contentStatus', XML_NAMESPACE + 'contentStatus', _extract_str, _to_xml_tag),
        ('created', XML_NAMESPACE_DCTERMS + 'created', _extract_datetime, _datetime_to_xml),
        ('creator', XML_NAMESPACE_DC + 'creator', _extract_str, _to_xml_tag),
        ('description', XML_NAMESPACE_DC + 'description', _extract_str, _to_xml_tag),
        ('identifier', XML_NAMESPACE_DC + 'identifier', _extract_str, _to_xml_tag),
        ('keywords', XML_NAMESPACE + 'keywords', _extract_keywords, _keywords_to_xml),
        ('language', XML_NAMESPACE_DC + 'language', _extract_str, _to_xml_tag),
        ('lastModifiedBy', XML_NAMESPACE + 'lastModifiedBy', _extract_str, _to_xml_tag),
        ('lastPrinted', XML_NAMESPACE + 'lastPrinted', _extract_datetime, _datetime_to_xml),
        ('modified', XML_NAMESPACE_DCTERMS + 'modified', _extract_datetime, _datetime_to_xml),
        ('revision', XML_NAMESPACE + 'revision', _extract_str, _to_xml_tag),
        ('subject', XML_NAMESPACE_DC + 'subject', _extract_str, _to_xml_tag),
        ('title', XML_NAMESPACE_DC + 'title', _extract_str, _to_xml_tag),
        ('version', XML_NAMESPACE + 'version', _extract_str, _to_xml_tag),
    )
    ATTRIBUTES_BY_TAG = {a[1]: a for a in ATTRIBUTES}
    NSMAP = {
        None: XML_NAMESPACE[1:-1],
        'dc': XML_NAMESPACE_DC[1:-1],
        'dcterms': XML_NAMESPACE_DCTERMS[1:-1],
        'xml': XML_NAMESPACE_XML[1:-1],
        'xsi': XML_NAMESPACE_XSI[1:-1],
    }

    def write_xml(self, file: IO[bytes]) -> None:
        """
        Serialize and write these Core Properties into an XML file or package Part, according to the ECMA standard.
        """
        with etree.xmlfile(file, encoding="UTF-8") as xf:
            xf.write_declaration()
            with xf.element(XML_NAMESPACE + "coreProperties", nsmap=self.NSMAP):
                for attr, tag, extractor, serializer in self.ATTRIBUTES:
                    value = getattr(self, attr)
                    if value is not None:
                        xf.write(serializer(tag, value))

    @classmethod
    def from_xml(cls, file: IO[bytes]) -> "OPCCoreProperties":
        """
        Read and parse Core Properties from an XML file or package Part, according to the ECMA standard.
        """
        result = cls()
        for _event, elem in etree.iterparse(file):
            if elem.tag in cls.ATTRIBUTES_BY_TAG:
                attr, tag, extractor, serializer = cls.ATTRIBUTES_BY_TAG[elem.tag]
                setattr(result, attr, extractor(elem))
                elem.clear()
        return result
