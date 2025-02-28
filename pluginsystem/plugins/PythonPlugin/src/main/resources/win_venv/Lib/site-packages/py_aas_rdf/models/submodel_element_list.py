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
import rdflib
from rdflib import RDF

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.data_type_def_xsd import DataTypeDefXsd
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.reference import Reference
from py_aas_rdf.models.specific_asset_id import SpecificAssetId
from py_aas_rdf.models.submodel_element import SubmodelElement

from enum import Enum
from typing import Any, List, Optional, Union, Literal

import pydantic
from pydantic import BaseModel, Field, constr


class AasSubmodelElements(Enum):
    AnnotatedRelationshipElement = "AnnotatedRelationshipElement"
    BasicEventElement = "BasicEventElement"
    Blob = "Blob"
    Capability = "Capability"
    DataElement = "DataElement"
    Entity = "Entity"
    EventElement = "EventElement"
    File = "File"
    MultiLanguageProperty = "MultiLanguageProperty"
    Operation = "Operation"
    Property = "Property"
    Range = "Range"
    ReferenceElement = "ReferenceElement"
    RelationshipElement = "RelationshipElement"
    SubmodelElement = "SubmodelElement"
    SubmodelElementCollection = "SubmodelElementCollection"
    SubmodelElementList = "SubmodelElementList"


class SubmodelElementList(SubmodelElement):
    orderRelevant: Optional[bool] = None
    semanticIdListElement: Optional[Reference] = None
    typeValueListElement: AasSubmodelElements
    valueTypeListElement: Optional[DataTypeDefXsd] = None
    value: Optional[List["SubmodelElementChoice"]] = Field(None, min_length=0)
    modelType: Literal["SubmodelElementList"] = ModelType.SubmodelElementList.value

    def to_rdf(
        self,
        graph: rdflib.Graph = None,
        parent_node: rdflib.IdentifiedNode = None,
        prefix_uri: str = "",
        base_uri: str = "",
        id_strategy: str = "",
    ) -> (rdflib.Graph, rdflib.IdentifiedNode):
        created_graph, created_node = super().to_rdf(graph, parent_node, prefix_uri, base_uri, id_strategy)
        created_graph.add((created_node, RDF.type, AASNameSpace.AAS["SubmodelElementList"]))
        if self.orderRelevant:
            created_graph.add(
                (
                    created_node,
                    AASNameSpace.AAS["SubmodelElementList/orderRelevant"],
                    rdflib.Literal(self.orderRelevant, datatype=rdflib.XSD.boolean),
                )
            )
        if self.semanticIdListElement:
            _, created_sub_node = self.semanticIdListElement.to_rdf(graph, created_node)
            created_graph.add(
                (
                    created_node,
                    AASNameSpace.AAS["SubmodelElementList/semanticIdListElement"],
                    created_sub_node,
                )
            )
        created_graph.add(
            (
                created_node,
                AASNameSpace.AAS["SubmodelElementList/typeValueListElement"],
                AASNameSpace.AAS[f"AasSubmodelElements/{self.typeValueListElement.name}"],
            )
        )
        if self.valueTypeListElement:
            created_graph.add(
                (
                    created_node,
                    AASNameSpace.AAS["SubmodelElementList/valueTypeListElement"],
                    AASNameSpace.AAS[f"DataTypeDefXsd/{self.valueTypeListElement.name}"],
                )
            )
        if self.value:
            for idx, element_value in enumerate(self.value):
                _, created_sub_node = element_value.to_rdf(
                    graph,
                    created_node,
                    prefix_uri=prefix_uri + self.idShort + ".",
                    base_uri=base_uri,
                    id_strategy=id_strategy,
                )
                graph.add((created_sub_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add((created_node, AASNameSpace.AAS["SubmodelElementList/value"], created_sub_node))

        return created_graph, created_node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "SubmodelElementList":
        submodel_element = SubmodelElement.from_rdf(graph, subject)
        order_relevant_value = None
        order_relevant_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["SubmodelElementList/orderRelevant"]),
            None,
        )
        if order_relevant_ref:
            order_relevant_value = order_relevant_ref.value

        semantic_id_list_element_value = None
        semantic_id_list_element_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["SubmodelElementList/semanticIdListElement"]),
            None,
        )
        if semantic_id_list_element_ref:
            semantic_id_list_element_value = Reference.from_rdf(graph, semantic_id_list_element_ref)
        type_value_list_element_value = None
        type_value_list_element_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["SubmodelElementList/typeValueListElement"]),
            None,
        )
        if type_value_list_element_ref:
            type_value_list_element_value = AasSubmodelElements[
                type_value_list_element_ref[type_value_list_element_ref.rfind("/") + 1 :]
            ]

        value_type_list_element_value = None
        value_type_list_element_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["SubmodelElementList/valueTypeListElement"]),
            None,
        )
        if value_type_list_element_ref:
            value_type_list_element_value = DataTypeDefXsd[
                value_type_list_element_ref[value_type_list_element_ref.rfind("/") + 1 :]
            ]

        value_value = []
        from py_aas_rdf.models.util import from_unknown_rdf

        for submodel_element_uriref in graph.objects(
            subject=subject, predicate=AASNameSpace.AAS["SubmodelElementList/value"]
        ):
            element = from_unknown_rdf(graph, submodel_element_uriref)
            value_value.append(element)

        if len(value_value) == 0:
            value_value = None

        return SubmodelElementList(
            orderRelevant=order_relevant_value,
            semanticIdListElement=semantic_id_list_element_value,
            typeValueListElement=type_value_list_element_value,
            valueTypeListElement=value_type_list_element_value,
            value=value_value,
            qualifiers=submodel_element.qualifiers,
            category=submodel_element.category,
            idShort=submodel_element.idShort,
            displayName=submodel_element.displayName,
            description=submodel_element.description,
            extensions=submodel_element.extensions,
            semanticId=submodel_element.semanticId,
            supplementalSemanticIds=submodel_element.supplementalSemanticIds,
            embeddedDataSpecifications=submodel_element.embeddedDataSpecifications,
        )
