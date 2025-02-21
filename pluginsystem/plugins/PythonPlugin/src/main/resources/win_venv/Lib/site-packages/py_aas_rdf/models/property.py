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
from enum import Enum
from typing import Any, List, Optional, Union, Literal

import rdflib
from pydantic import BaseModel, Field, constr
from rdflib import RDF

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.data_element import DataElement

from enum import Enum
from typing import Any, List, Optional, Union, Literal

from pydantic import BaseModel, Field, constr

from py_aas_rdf.models.data_type_def_xsd import DataTypeDefXsd
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.reference import Reference
from py_aas_rdf.models.submodel_element import SubmodelElement


class Property(DataElement):
    valueType: DataTypeDefXsd
    value: Optional[str] = None
    valueId: Optional[Reference] = None
    modelType: Literal["Property"] = ModelType.Property.value

    def to_rdf(
        self,
        graph: rdflib.Graph = None,
        parent_node: rdflib.IdentifiedNode = None,
        prefix_uri: str = "",
        base_uri: str = "",
        id_strategy: str = "",
    ) -> (rdflib.Graph, rdflib.IdentifiedNode):
        created_graph, created_node = super().to_rdf(graph, parent_node, prefix_uri, base_uri, id_strategy)
        created_graph.add((created_node, RDF.type, AASNameSpace.AAS["Property"]))
        created_graph.add(
            (
                created_node,
                AASNameSpace.AAS["Property/valueType"],
                AASNameSpace.AAS[f"DataTypeDefXsd/{self.valueType.name}"],
            )
        )
        if self.value != None:
            created_graph.add((created_node, AASNameSpace.AAS["Property/value"], rdflib.Literal(self.value)))
        if self.valueId:
            _, value_id_ref_node = self.valueId.to_rdf(created_graph, created_node)
            created_graph.add((created_node, AASNameSpace.AAS["Property/valueId"], value_id_ref_node))
        return created_graph, created_node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "Property":
        value_type_value = None
        value_type_value_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Property/valueType"]),
            None,
        )
        if value_type_value_ref:
            value_type_value = DataTypeDefXsd[value_type_value_ref[value_type_value_ref.rfind("/") + 1 :]]

        value_value = None
        value_value_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Property/value"]),
            None,
        )
        if value_value_ref != None:
            value_value = value_value_ref.value
        value_id_value = None
        value_id_value_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Property/valueId"]),
            None,
        )
        if value_id_value_ref:
            value_id_value = Reference.from_rdf(graph, value_id_value_ref)
        submodel_element = SubmodelElement.from_rdf(graph, subject)
        return Property(
            valueType=value_type_value,
            value=value_value,
            valueId=value_id_value,
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
