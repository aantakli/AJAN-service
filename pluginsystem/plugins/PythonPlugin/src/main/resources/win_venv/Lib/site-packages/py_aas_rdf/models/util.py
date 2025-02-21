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
from __future__ import annotations
import rdflib
from rdflib import RDF

from py_aas_rdf.models.aas_namespace import AASNameSpace
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
from py_aas_rdf.models.submodel_element import SubmodelElement
from py_aas_rdf.models.submodel_element_collection import SubmodelElementCollection
from py_aas_rdf.models.submodel_element_list import SubmodelElementList


def from_unknown_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> SubmodelElement:
    # use modeltype as discriminator
    # This is my worst code ever :)
    type_ref: rdflib.URIRef = next(
        graph.objects(subject=subject, predicate=RDF.type),
        None,
    )
    if type_ref == AASNameSpace.AAS["AnnotatedRelationshipElement"]:
        return AnnotatedRelationshipElement.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["RelationshipElement"]:
        return RelationshipElement.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["BasicEventElement"]:
        return BasicEventElement.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["Blob"]:
        return Blob.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["File"]:
        return File.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["MultiLanguageProperty"]:
        return MultiLanguageProperty.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["Property"]:
        return Property.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["Range"]:
        return Range.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["ReferenceElement"]:
        return ReferenceElement.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["SubmodelElementCollection"]:
        return SubmodelElementCollection.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["SubmodelElementList"]:
        return SubmodelElementList.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["Entity"]:
        return Entity.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["Capability"]:
        return Capability.from_rdf(graph, subject)
    if type_ref == AASNameSpace.AAS["Operation"]:
        return Operation.from_rdf(graph, subject)
