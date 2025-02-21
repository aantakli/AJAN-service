from __future__ import annotations

import sys
from dataclasses import dataclass, field
from typing import Any

kwargs: dict[str, bool]
if sys.version_info < (3, 10):
    kwargs = {}
else:
    kwargs = {"slots": True}


@dataclass(**kwargs)
class Creator:
    # Name of the application used to export the log
    name: str
    # Version of the application used to export the log
    version: str
    comment: str | None = None


@dataclass(**kwargs)
class Browser:
    # Name of the browser used to export the log
    name: str
    # Version of the browser used to export the log
    version: str
    comment: str | None = None


@dataclass(**kwargs)
class Cache:
    """Info about a request coming from browser cache."""

    # State of a cache entry before the request
    beforeRequest: CacheEntry | None = None
    # State of a cache entry after the request
    afterRequest: CacheEntry | None = None
    comment: str | None = None

    def asdict(self) -> dict[str, Any]:
        data: dict[str, Any] = {}
        if self.beforeRequest is not None:
            data["beforeRequest"] = self.beforeRequest.asdict()
        if self.afterRequest is not None:
            data["afterRequest"] = self.afterRequest.asdict()
        if self.comment is not None:
            data["comment"] = self.comment
        return data


@dataclass(**kwargs)
class CacheEntry:
    # Expiration time of the cache entry
    expires: str
    # The last time the cache entry was opened
    lastAccess: str
    eTag: str
    # The number of times the cache entry has been opened
    hitCount: int
    comment: str | None = None

    def asdict(self) -> dict[str, Any]:
        data = {"expires": self.expires, "lastAccess": self.lastAccess, "eTag": self.eTag, "hitCount": self.hitCount}
        if self.comment is not None:
            data["comment"] = self.comment
        return data


@dataclass(**kwargs)
class Request:
    method: str
    # Absolute URL of the request (fragments are not included)
    url: str
    # Request HTTP Version
    httpVersion: str
    cookies: list[Cookie] = field(default_factory=list)
    headers: list[Record] = field(default_factory=list)
    queryString: list[Record] = field(default_factory=list)
    postData: PostData | None = None
    # Total number of bytes from the start of the HTTP request message until (and including) the double CRLF
    # before the body
    headersSize: int = -1
    # Size of the request body (POST data payload) in bytes
    bodySize: int = -1
    comment: str | None = None

    def asdict(self) -> dict[str, Any]:
        data = {
            "method": self.method,
            "url": self.url,
            "httpVersion": self.httpVersion,
            "cookies": [cookie.asdict() for cookie in self.cookies],
            "headers": [header.asdict() for header in self.headers],
            "queryString": [query.asdict() for query in self.queryString],
            "headersSize": self.headersSize,
            "bodySize": self.bodySize,
        }
        if self.postData is not None:
            data["postData"] = self.postData.asdict()
        if self.comment is not None:
            data["comment"] = self.comment
        return data


@dataclass(**kwargs)
class Cookie:
    # The name of the cookie
    name: str
    # The cookie value
    value: str
    # The path pertaining to the cookie
    path: str | None = None
    # The host of the cookie
    domain: str | None = None
    # Cookie expiration time
    expires: str | None = None
    # Set to true if the cookie is HTTP only, false otherwise
    httpOnly: bool | None = None
    # True if the cookie was transmitted over ssl, false otherwise
    secure: bool | None = None
    comment: str | None = None

    def asdict(self) -> dict[str, Any]:
        data: dict[str, Any] = {"name": self.name, "value": self.value}
        if self.path is not None:
            data["path"] = self.path
        if self.domain is not None:
            data["domain"] = self.domain
        if self.expires is not None:
            data["expires"] = self.expires
        if self.httpOnly is not None:
            data["httpOnly"] = self.httpOnly
        if self.secure is not None:
            data["secure"] = self.secure
        if self.comment is not None:
            data["comment"] = self.comment
        return data


@dataclass(**kwargs)
class Record:
    name: str
    value: str
    comment: str | None = None

    def asdict(self) -> dict[str, str]:
        data = {"name": self.name, "value": self.value}
        if self.comment is not None:
            data["comment"] = self.comment
        return data


@dataclass(**kwargs)
class PostData:
    # MIME type of the posted data
    mimeType: str
    # List of posted parameters (in case of URL encoded parameters)
    params: list[PostParameter] | None = None
    # Plain text posted data
    text: str | None = None
    comment: str | None = None

    def asdict(self) -> dict[str, Any]:
        data: dict[str, Any] = {"mimeType": self.mimeType}
        if self.params is not None:
            data["params"] = [param.asdict() for param in self.params]
        if self.text is not None:
            data["text"] = self.text
        if self.comment is not None:
            data["comment"] = self.comment
        return data


@dataclass(**kwargs)
class PostParameter:
    name: str
    value: str | None = None
    fileName: str | None = None
    contentType: str | None = None
    comment: str | None = None

    def asdict(self) -> dict[str, str]:
        data = {"name": self.name}
        if self.value is not None:
            data["value"] = self.value
        if self.fileName is not None:
            data["fileName"] = self.fileName
        if self.contentType is not None:
            data["contentType"] = self.contentType
        if self.comment is not None:
            data["comment"] = self.comment
        return data


@dataclass(**kwargs)
class Content:
    size: int = 0
    # Number of bytes saved
    compression: int | float | None = None
    # MIME type of the response text (value of the Content-Type response header).
    # The charset attribute of the MIME type is included
    mimeType: str = ""
    # Response body sent from the server or loaded from the browser cache.
    # This field is populated with textual content only.
    # The text field is either HTTP decoded text or a encoded (e.g. "base64") representation of the response body
    text: str | None = None
    encoding: str | None = None
    # Encoding used for response text field e.g "base64"
    comment: str | None = None

    def asdict(self) -> dict[str, Any]:
        data: dict[str, Any] = {"size": self.size}
        if self.compression is not None:
            data["compression"] = self.compression
        if self.mimeType is not None:
            data["mimeType"] = self.mimeType
        if self.text is not None:
            data["text"] = self.text
        if self.encoding is not None:
            data["encoding"] = self.encoding
        if self.comment is not None:
            data["comment"] = self.comment
        return data


@dataclass(**kwargs)
class Response:
    # Response status
    status: int
    # Response status description
    statusText: str
    # Response HTTP Version
    httpVersion: str
    cookies: list[Cookie] = field(default_factory=list)
    headers: list[Record] = field(default_factory=list)
    # Details about the response body
    content: Content = field(default_factory=Content)
    # Redirection target URL from the Location response header
    redirectURL: str = ""
    # Total number of bytes from the start of the HTTP response message
    # until (and including) the double CRLF before the body
    headersSize: int = -1
    # Size of the received response body in bytes
    bodySize: int = -1
    comment: str | None = None

    def asdict(self) -> dict[str, Any]:
        data = {
            "status": self.status,
            "statusText": self.statusText,
            "httpVersion": self.httpVersion,
            "cookies": [cookie.asdict() for cookie in self.cookies],
            "headers": [header.asdict() for header in self.headers],
            "content": self.content.asdict(),
            "redirectURL": self.redirectURL,
            "headersSize": self.headersSize,
            "bodySize": self.bodySize,
        }
        if self.comment is not None:
            data["comment"] = self.comment
        return data


@dataclass(**kwargs)
class Timings:
    # Time required to send HTTP request to the server
    send: int | float
    # Waiting for a response from the server
    wait: int | float
    # Time required to read entire response from the server (or cache)
    receive: int | float
    # Time spent in a queue waiting for a network connection
    blocked: int | float = -1
    # DNS resolution time. The time required to resolve a host name
    dns: int | float = -1
    # Time required to create TCP connection
    connect: int | float = -1
    # Time required for SSL/TLS negotiation
    ssl: int | float = -1
    comment: str | None = None

    def asdict(self) -> dict[str, Any]:
        data: dict[str, Any] = {
            "send": self.send,
            "wait": self.wait,
            "receive": self.receive,
            "blocked": self.blocked,
            "dns": self.dns,
            "connect": self.connect,
            "ssl": self.ssl,
        }
        if self.comment is not None:
            data["comment"] = self.comment
        return data
