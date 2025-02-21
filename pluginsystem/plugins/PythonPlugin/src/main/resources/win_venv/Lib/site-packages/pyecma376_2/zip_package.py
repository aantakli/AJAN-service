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
Implementation of physical OPC packages in PKZIP files.

This module uses the abstract Reader/Writer classes from the package_model module to extend them for reading and writing
ZIP-based OPC package files. The resulting `ZipPackageReader` and `ZipPackageWriter` classes are to be used by
other Python packages to read and write concrete OPC package files. However, they may be imported from `pyecma376_2`'s
main package.
"""
import os
import zipfile
from typing import Iterable, IO, Union, BinaryIO

from . import package_model

CONTENT_TYPES_STREAM_NAME = "/[Content_Types].xml"


class ZipPackageReader(package_model.OPCPackageReader, zipfile.ZipFile):
    content_types_stream_name = package_model.normalize_part_name(CONTENT_TYPES_STREAM_NAME)

    def __init__(self, file: Union[os.PathLike, str, IO]) -> None:
        package_model.OPCPackageReader.__init__(self)
        zipfile.ZipFile.__init__(self, file)
        self._init_data()

    def list_items(self) -> Iterable[str]:
        return ["/" + name for name in self.namelist()
                if name[-1] != '/']

    def open_item(self, name: str) -> BinaryIO:
        return self.open(name[1:])  # type: ignore  # This seems to be an issue of typeshed.

    def close(self) -> None:
        zipfile.ZipFile.close(self)


class ZipPackageWriter(package_model.OPCPackageWriter, zipfile.ZipFile):
    content_types_stream_name = CONTENT_TYPES_STREAM_NAME

    def __init__(self, file: Union[os.PathLike, str, IO]) -> None:
        package_model.OPCPackageWriter.__init__(self)
        zipfile.ZipFile.__init__(self, file, mode='w', compression=zipfile.ZIP_DEFLATED)

    def close(self) -> None:
        package_model.OPCPackageWriter.close(self)
        zipfile.ZipFile.close(self)

    def create_item(self, name: str, content_type: str) -> BinaryIO:
        return self.open(name[1:], mode='w')  # type: ignore  # This seems to be an issue of typeshed.
