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
from rdflib import RDF, Graph

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.data_type_def_xsd import DataTypeDefXsd
from py_aas_rdf.models.has_semantics import HasSemantics
from py_aas_rdf.models.rdfiable import RDFiable
from py_aas_rdf.models.reference import Reference


class Extension(HasSemantics, RDFiable):
    name: constr(min_length=1, max_length=128)
    valueType: Optional[DataTypeDefXsd] = None
    value: Optional[str] = None
    refersTo: Optional[List[Reference]] = Field(None, min_length=1)

    def to_rdf(
        self,
        graph: rdflib.Graph = None,
        parent_node: rdflib.IdentifiedNode = None,
        prefix_uri: str = "",
        base_uri: str = "",
        id_strategy: str = "",
    ) -> (rdflib.Graph, rdflib.IdentifiedNode):
        if graph == None:
            graph = rdflib.Graph()
            graph.bind("aas", AASNameSpace.AAS)

        node = rdflib.BNode()
        graph.add((node, rdflib.RDF.type, AASNameSpace.AAS["Extension"]))
        # HasSemantics
        HasSemantics.append_as_rdf(self, graph, node)
        graph.add((node, AASNameSpace.AAS["Extension/name"], rdflib.Literal(self.name)))
        if self.valueType:
            graph.add(
                (
                    node,
                    AASNameSpace.AAS["Extension/valueType"],
                    AASNameSpace.AAS[f"DataTypeDefXsd/{self.valueType.name}"],
                )
            )

        if self.value:
            graph.add((node, AASNameSpace.AAS["Extension/value"], rdflib.Literal(self.value)))

        if self.refersTo and len(self.refersTo) > 0:
            for idx, refer in enumerate(self.refersTo):
                _, created_node = refer.to_rdf(graph=graph, parent_node=node)
                graph.add((created_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add((node, AASNameSpace.AAS["Extension/refersTo"], created_node))

        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        # HasSemantics
        # Not oop!
        hasSemantics = HasSemantics.from_rdf(graph, subject)

        name: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Extension/name"]),
            None,
        )
        value_type_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Extension/valueType"]),
            None,
        )
        value_type = None
        if value_type_ref:
            value_type = DataTypeDefXsd[value_type_ref[value_type_ref.rfind("/") + 1 :]]

        value = None
        value_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Extension/value"]),
            None,
        )
        if value_ref:
            value = value_ref.value

        refersTo_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Extension/refersTo"]),
            None,
        )
        refersTo = []
        if refersTo_ref in graph.objects(subject=subject, predicate=AASNameSpace.AAS["Extension/refersTo"]):
            refersTo.append(Reference.from_rdf(graph, refersTo_ref))

        if len(refersTo) == 0:
            refersTo = None
        return Extension.model_construct(
            name=name.value,
            valueType=value_type,
            value=value,
            refersTo=refersTo,
            supplementalSemanticIds=hasSemantics.supplementalSemanticIds,
            semanticId=hasSemantics.semanticId,
        )
