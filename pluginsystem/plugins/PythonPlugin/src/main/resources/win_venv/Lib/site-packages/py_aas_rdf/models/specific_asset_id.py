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
from rdflib import RDF

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.has_semantics import HasSemantics
from py_aas_rdf.models.rdfiable import RDFiable
from py_aas_rdf.models.reference import Reference


class SpecificAssetId(HasSemantics, RDFiable):
    name: constr(
        min_length=1,
        max_length=64,
    )
    value: constr(
        min_length=1,
        max_length=2000,
    )
    externalSubjectId: Optional[Reference] = None

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
        graph.add((node, RDF.type, AASNameSpace.AAS["SpecificAssetId"]))

        # HasSemantics
        HasSemantics.append_as_rdf(self, graph, node)

        graph.add((node, AASNameSpace.AAS["SpecificAssetId/name"], rdflib.Literal(self.name)))
        graph.add((node, AASNameSpace.AAS["SpecificAssetId/value"], rdflib.Literal(self.value)))
        if self.externalSubjectId:
            _, created_node = self.externalSubjectId.to_rdf(graph, node)
            graph.add((node, AASNameSpace.AAS["SpecificAssetId/externalSubjectId"], created_node))

        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        name_value = None

        name_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["SpecificAssetId/name"]), None
        )
        if name_ref:
            name_value = name_ref.value

        value_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["SpecificAssetId/value"]), None
        )
        value_value = None
        if value_ref:
            value_value = value_ref.value

        external_subject_id_value = None
        external_subject_id_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["SpecificAssetId/externalSubjectId"]), None
        )
        if external_subject_id_ref:
            external_subject_id_value = Reference.from_rdf(graph, external_subject_id_ref)

        # HasSemantics
        hasSemantics = HasSemantics.from_rdf(graph, subject)
        return SpecificAssetId(
            name=name_value,
            value=value_value,
            externalSubjectId=external_subject_id_value,
            semanticId=hasSemantics.semanticId,
            supplementalSemanticIds=hasSemantics.supplementalSemanticIds,
        )
