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
from typing import Any, List, Optional, Union, Literal, Annotated

import pydantic
from pydantic import BaseModel, Field, constr

from py_aas_rdf.models.annotated_relationship_element import AnnotatedRelationshipElement
from py_aas_rdf.models.basic_event_element import BasicEventElement
from py_aas_rdf.models.blob import Blob
from py_aas_rdf.models.capability import Capability
from py_aas_rdf.models.entity import Entity
from py_aas_rdf.models.file import File
from py_aas_rdf.models.multi_language_property import MultiLanguageProperty
from py_aas_rdf.models.operation import Operation
from py_aas_rdf.models.property import Property
from py_aas_rdf.models.range import Range
from py_aas_rdf.models.reference_element import ReferenceElement
from py_aas_rdf.models.relationship_element import RelationshipElement
from py_aas_rdf.models.submodel_element_collection import SubmodelElementCollection
from py_aas_rdf.models.submodel_element_list import SubmodelElementList

SubmodelElementChoice = Annotated[
    Union[
        RelationshipElement,
        AnnotatedRelationshipElement,
        BasicEventElement,
        Blob,
        File,
        MultiLanguageProperty,
        Property,
        Range,
        ReferenceElement,
        SubmodelElementCollection,
        SubmodelElementList,
        Entity,
        Capability,
        Operation,
    ],
    Field(discriminator="modelType"),
]
