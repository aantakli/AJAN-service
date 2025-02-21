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
from py_aas_rdf.models.rdfiable import RDFiable
from py_aas_rdf.models.reference import Reference


class HasSemantics(BaseModel):
    semanticId: Optional[Reference] = None
    supplementalSemanticIds: Optional[List[Reference]] = Field(None, min_length=0)

    @staticmethod
    def append_as_rdf(instance: "HasSemantics", graph: rdflib.Graph, parent_node: rdflib.IdentifiedNode):
        if instance.semanticId:
            _, created_node_semantic_id = instance.semanticId.to_rdf(graph=graph, parent_node=parent_node)
            graph.add((parent_node, AASNameSpace.AAS["HasSemantics/semanticId"], created_node_semantic_id))
        if instance.supplementalSemanticIds and len(instance.supplementalSemanticIds) > 0:
            for idx, supplementalSemanticId in enumerate(instance.supplementalSemanticIds):
                _, created_node = supplementalSemanticId.to_rdf(graph=graph, parent_node=parent_node)
                graph.add((created_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add((parent_node, AASNameSpace.AAS["HasSemantics/supplementalSemanticIds"], created_node))

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "HasSemantics":
        semantic_id_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["HasSemantics/semanticId"]),
            None,
        )

        semantic_id = None
        if semantic_id_ref:
            semantic_id = Reference.from_rdf(graph, semantic_id_ref)

        supplementalSemanticIds = []
        for supp_semantic_id in graph.objects(
            subject=subject, predicate=AASNameSpace.AAS["HasSemantics/supplementalSemanticIds"]
        ):
            supplementalSemanticIds.append(Reference.from_rdf(graph, supp_semantic_id))

        if len(supplementalSemanticIds) == 0:
            supplementalSemanticIds = None

        return HasSemantics(semanticId=semantic_id, supplementalSemanticIds=supplementalSemanticIds)
