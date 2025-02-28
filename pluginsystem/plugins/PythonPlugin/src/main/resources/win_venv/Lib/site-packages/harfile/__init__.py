"""Write HTTP Archive (HAR) files."""

from __future__ import annotations

import builtins
import json
from datetime import datetime
from os import PathLike
from types import TracebackType
from typing import IO

from ._models import (
    Browser,
    Cache,
    CacheEntry,
    Content,
    Cookie,
    Creator,
    PostData,
    PostParameter,
    Record,
    Request,
    Response,
    Timings,
)
from ._version import VERSION

__all__ = [
    "open",
    "HarFile",
    "Browser",
    "Creator",
    "Cache",
    "CacheEntry",
    "Request",
    "Response",
    "Timings",
    "Cookie",
    "Record",
    "PostData",
    "PostParameter",
    "Content",
    "HAR_VERSION",
]

HAR_VERSION = "1.2"


class HarFile:
    _fd: IO[str]
    _creator: Creator
    _browser: Browser
    _comment: str | None
    _is_first_entry: bool = True
    _has_preable: bool = False
    closed: bool = False

    def __init__(
        self,
        fd: IO[str],
        *,
        creator: Creator | None = None,
        browser: Browser | None = None,
        comment: str | None = None,
    ):
        self._fd = fd
        self._creator = creator or Creator(name="harfile", version=VERSION)
        self._browser = browser or Browser(name="", version="")
        self._comment = comment

    @classmethod
    def open(
        cls,
        name_or_fd: str | PathLike | IO[str],
        *,
        creator: Creator | None = None,
        browser: Browser | None = None,
        comment: str | None = None,
    ) -> HarFile:
        """Open a HAR file for writing."""
        fd: IO[str]
        if isinstance(name_or_fd, (str, PathLike)):
            fd = builtins.open(name_or_fd, "w")
        else:
            fd = name_or_fd
        return cls(fd=fd, creator=creator, browser=browser, comment=comment)

    def close(self) -> None:
        """Close the HAR file."""
        if self.closed:
            return None
        self.closed = True
        if not self._has_preable:
            self._write_preamble()
            self._has_preable = True
        self._write_postscript()

    def __enter__(self) -> HarFile:
        return self

    def __exit__(
        self,
        type: type[BaseException] | None,
        value: BaseException | None,
        traceback: TracebackType | None,
    ) -> None:
        if type is None:
            self.close()
        return None

    def add_entry(
        self,
        *,
        startedDateTime: datetime | str,
        time: int | float,
        request: Request,
        response: Response,
        timings: Timings,
        cache: Cache | None = None,
        serverIPAddress: str | None = None,
        connection: str | None = None,
        comment: str | None = None,
    ) -> None:
        """Add an HTTP request to the HAR file."""
        if not self._has_preable:
            self._write_preamble()
            self._has_preable = True
        separator = "\n" if self._is_first_entry else ",\n"
        self._is_first_entry = False
        write = self._fd.write
        dumps = json.dumps
        if isinstance(startedDateTime, datetime):
            startedDateTime = startedDateTime.isoformat()
        write(f"{separator}            {{")
        write(f'\n                "startedDateTime": "{startedDateTime}",')
        write(f'\n                "time": {time},')
        write(f'\n                "request": {dumps(request.asdict())},')
        write(f'\n                "response": {dumps(response.asdict())},')
        write(f'\n                "timings": {dumps(timings.asdict())},')
        cache = cache or Cache()
        write(f'\n                "cache": {dumps(cache.asdict())}')
        if serverIPAddress:
            write(f',\n                "serverIPAddress": {dumps(serverIPAddress)}')
        if connection:
            write(f',\n                "connection": {dumps(connection)}')
        if comment:
            write(f',\n                "comment": {dumps(comment)}')
        write("\n            }")

    def _write_preamble(self) -> None:
        creator = f"""{{
            "name": "{self._creator.name}",
            "version": "{self._creator.version}\""""
        if self._creator.comment:
            creator += f',\n            "comment": "{self._creator.comment}"'
        creator += "\n        }"
        browser = f"""{{
            "name": "{self._browser.name}",
            "version": "{self._browser.version}\""""
        if self._browser.comment:
            browser += f',\n            "comment": "{self._browser.comment}"'
        browser += "\n        }"
        self._fd.write(f"""{{
    "log": {{
        "version": "{HAR_VERSION}",
        "creator": {creator},
        "browser": {browser}""")
        if self._comment:
            self._fd.write(f',\n        "comment": "{self._comment}"')
        self._fd.write(',\n        "entries": [')

    def _write_postscript(self) -> None:
        if self._is_first_entry:
            self._fd.write("]\n    }\n}")
        else:
            self._fd.write("\n        ]\n    }\n}")


open = HarFile.open
