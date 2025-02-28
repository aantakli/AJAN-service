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
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.multi_language_property import MultiLanguageProperty
from py_aas_rdf.models.property import Property
from py_aas_rdf.models.specific_asset_id import SpecificAssetId
from py_aas_rdf.models.submodel_element import SubmodelElement

from enum import Enum
from typing import Any, List, Optional, Union, Literal

import pydantic
from pydantic import BaseModel, Field, constr


# TODO: recheck
class SubmodelElementCollection(SubmodelElement):
    value: Optional[List["SubmodelElementChoice"]] = Field(None, min_length=0)
    modelType: Literal["SubmodelElementCollection"] = ModelType.SubmodelElementCollection.value

    def to_rdf(
        self,
        graph: rdflib.Graph = None,
        parent_node: rdflib.IdentifiedNode = None,
        prefix_uri: str = "",
        base_uri: str = "",
        id_strategy: str = "",
    ) -> (rdflib.Graph, rdflib.IdentifiedNode):
        created_graph, created_node = super().to_rdf(graph, parent_node, prefix_uri, base_uri, id_strategy)

        created_graph.add((created_node, RDF.type, AASNameSpace.AAS["SubmodelElementCollection"]))
        if self.value:
            for idx, submodel_element in enumerate(self.value):
                # headache
                _, created_sub_node = submodel_element.to_rdf(
                    graph,
                    created_node,
                    prefix_uri=prefix_uri + self.idShort + ".",
                    base_uri=base_uri,
                    id_strategy=id_strategy,
                )
                graph.add((created_sub_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add((created_node, AASNameSpace.AAS["SubmodelElementCollection/value"], created_sub_node))
        return created_graph, created_node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "SubmodelElementCollection":
        # submodelElements
        submodel_elements_value = []
        from py_aas_rdf.models.util import from_unknown_rdf

        for submodel_element_uriref in graph.objects(
            subject=subject, predicate=AASNameSpace.AAS["SubmodelElementCollection/value"]
        ):
            element = from_unknown_rdf(graph, submodel_element_uriref)
            submodel_elements_value.append(element)

        if len(submodel_elements_value) == 0:
            submodel_elements_value = None
        submodel_element = SubmodelElement.from_rdf(graph, subject)

        return SubmodelElementCollection(
            value=submodel_elements_value,
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
