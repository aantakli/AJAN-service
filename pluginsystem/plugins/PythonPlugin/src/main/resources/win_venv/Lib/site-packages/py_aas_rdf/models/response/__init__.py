#  MIT License
#
#  Copyright (c) 2023. Mohammad Hossein Rimaz
#
#  Permission is hereby granted, free of charge, to any person obtaining a copy of
#  this software and associated documentation files (the “Software”), to deal in
#  the Software without restriction, including without limitation the rights to use,
#  copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
#  Software, and to permit persons to whom the Software is furnished to do so, subject
#   to the following conditions:
#
#  The above copyright notice and this permission notice shall be included in all
#  copies or substantial portions of the Software.
#
#  THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
#  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
#  PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
#  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
#  CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
#  OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

from enum import Enum
from typing import Any, List, Optional, Union, Literal

from pydantic import BaseModel, Field, constr

from py_aas_rdf.models.concept_description import ConceptDescription


class PagingMetadata(BaseModel):
    cursor: str


class GetConceptDescriptionsResult(BaseModel):
    paging_metadata: PagingMetadata
    result: Optional[List[ConceptDescription]] = None


class MessageType(Enum):
    Undefined = "Undefined"
    Info = "Info"
    Warning = "Warning"
    Error = "Error"
    Exception = "Exception"


class Message(BaseModel):
    code: Optional[constr(min_length=1, max_length=32)] = None
    correlationId: Optional[constr(min_length=1, max_length=128)] = None
    messageType: Optional[MessageType] = None
    text: str
    timestamp: constr(
        pattern=r"^-?(([1-9][0-9][0-9][0-9]+)|(0[0-9][0-9][0-9]))-((0[1-9])|(1[0-2]))-((0[1-9])|([12]["
        r"0-9])|(3[01]))T(((([01][0-9])|(2[0-3])):[0-5][0-9]:([0-5][0-9])(\.[0-9]+)?)|24:00:00("
        r"\.0+)?)(Z|\+00:00|-00:00)$"
    )


class Result(BaseModel):
    messages: Optional[List[Message]] = None


class RepositoryMetadata(BaseModel):
    total_items: int
    last_update: str


class ServiceDescription(BaseModel):
    profiles: List[
        Literal[
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellServiceSpecification/SSP-001",
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellServiceSpecification/SSP-002",
            "https://admin-shell.io/aas/API/3/0/SubmodelServiceSpecification/SSP-001",
            "https://admin-shell.io/aas/API/3/0/SubmodelServiceSpecification/SSP-002",
            "https://admin-shell.io/aas/API/3/0/SubmodelServiceSpecification/SSP-003",
            "https://admin-shell.io/aas/API/3/0/AasxFileServerServiceSpecification/SSP-001",
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRegistryServiceSpecification/SSP-001",
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRegistryServiceSpecification/SSP-002",
            "https://admin-shell.io/aas/API/3/0/SubmodelRegistryServiceSpecification/SSP-001",
            "https://admin-shell.io/aas/API/3/0/SubmodelRegistryServiceSpecification/SSP-002",
            "https://admin-shell.io/aas/API/3/0/DiscoveryServiceSpecification/SSP-001",
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRepositoryServiceSpecification/SSP-001",
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRepositoryServiceSpecification/SSP-002",
            "https://admin-shell.io/aas/API/3/0/SubmodelRepositoryServiceSpecification/SSP-001",
            "https://admin-shell.io/aas/API/3/0/SubmodelRepositoryServiceSpecification/SSP-002",
            "https://admin-shell.io/aas/API/3/0/SubmodelRepositoryServiceSpecification/SSP-003",
            "https://admin-shell.io/aas/API/3/0/SubmodelRepositoryServiceSpecification/SSP-004",
            "https://admin-shell.io/aas/API/3/0/ConceptDescriptionServiceSpecification/SSP-001",
        ]
    ]


class HealthResponse(BaseModel):
    status: str
    uptime: str


class APIException(Exception):
    message = """Unknown Error"""
    error_code = 500


class DatabaseConnectionException(APIException):
    message = """Unable to reach database"""
    error_code = 500


class ConceptNotFoundException(APIException):
    message = """The concept description does not exists."""
    error_code = 404


class DuplicateConceptException(APIException):
    message = """The concept description already exists."""
    error_code = 400


class UpdatePayloadIDMismatchException(APIException):
    message = """The provided base64-url-encoded-id does not match with the provided id in the payload."""
    error_code = 400


class InvalidPayloadException(APIException):
    message = """The provided payload does not comply with AAS specification."""
    error_code = 400


class OperationNotAllowedException(APIException):
    message = """This operation is not allowed."""
    error_code = 403


class InvalidBase64URLIdentifier(APIException):
    message = """The provided identifier is not a valid base64url."""
    error_code = 403
