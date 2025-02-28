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

import rdflib
from pydantic import BaseModel, Field, constr

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.data_type_def_xsd import DataTypeDefXsd
from py_aas_rdf.models.has_semantics import HasSemantics
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.qualifier_kind import QualifierKind
from py_aas_rdf.models.reference import Reference


class Qualifier(HasSemantics):
    kind: Optional[QualifierKind] = None
    type: constr(
        min_length=1,
        max_length=128,
    )
    valueType: DataTypeDefXsd
    value: Optional[str] = None
    valueId: Optional[Reference] = None

    @staticmethod
    def append_as_rdf(instance: "Qualifier", graph: rdflib.Graph, parent_node: rdflib.IdentifiedNode):
        # HasSemantics
        HasSemantics.append_as_rdf(instance, graph, parent_node)

        if instance.kind:
            graph.add(
                (
                    parent_node,
                    AASNameSpace.AAS["Qualifier/kind"],
                    AASNameSpace.AAS[f"QualifierKind/{instance.kind.value}"],
                )
            )
        graph.add((parent_node, AASNameSpace.AAS["Qualifier/type"], rdflib.Literal(instance.type)))
        graph.add(
            (
                parent_node,
                AASNameSpace.AAS["Qualifier/valueType"],
                AASNameSpace.AAS[f"DataTypeDefXsd/{instance.valueType.name}"],
            )
        )
        if instance.value:
            graph.add((parent_node, AASNameSpace.AAS["Qualifier/value"], rdflib.Literal(instance.value)))
        if instance.valueId:
            _, created_node = instance.valueId.to_rdf(graph, parent_node)
            graph.add((parent_node, AASNameSpace.AAS["Qualifier/valueId"], created_node))

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "Qualifier":
        # HasSemantics
        has_semantics = HasSemantics.from_rdf(graph, subject)

        kind_value = None
        kind_value_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Qualifier/kind"]),
            None,
        )
        if kind_value_ref:
            kind_value = QualifierKind[kind_value_ref[kind_value_ref.rfind("/") + 1 :]]
        type_value = None
        type_value_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Qualifier/type"]),
            None,
        )
        if type_value_ref:
            type_value = type_value_ref.value
        value_type_value = None
        value_type_value_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Qualifier/valueType"]),
            None,
        )
        if value_type_value_ref:
            value_type_value = DataTypeDefXsd[value_type_value_ref[value_type_value_ref.rfind("/") + 1 :]]
        value_value = None
        value_value_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Qualifier/value"]),
            None,
        )
        if value_value_ref:
            value_value = value_value_ref.value
        value_id_value = None
        value_id_value_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Qualifier/valueId"]),
            None,
        )
        if value_id_value_ref:
            value_id_value = Reference.from_rdf(graph, value_id_value_ref)
        return Qualifier(
            kind=kind_value,
            type=type_value,
            valueType=value_type_value,
            value=value_value,
            valueId=value_id_value,
            semanticId=has_semantics.semanticId,
            supplementalSemanticIds=has_semantics.supplementalSemanticIds,
        )
