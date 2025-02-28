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
from py_aas_rdf.models.embedded_data_specification import EmbeddedDataSpecification


class HasDataSpecification(BaseModel):
    embeddedDataSpecifications: Optional[List[EmbeddedDataSpecification]] = Field(None, min_length=0)

    @staticmethod
    def append_as_rdf(instance: "HasDataSpecification", graph: rdflib.Graph, parent_node: rdflib.IdentifiedNode):
        if instance.embeddedDataSpecifications and len(instance.embeddedDataSpecifications) > 0:
            for idx, data_specification in enumerate(instance.embeddedDataSpecifications):
                _, created_node = data_specification.to_rdf(
                    graph,
                )
                graph.add((created_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add(
                    (parent_node, AASNameSpace.AAS["HasDataSpecification/embeddedDataSpecifications"], created_node)
                )

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        embeddedDataSpecifications = []
        for embedded_ref in graph.objects(
            subject=subject, predicate=AASNameSpace.AAS["HasDataSpecification/embeddedDataSpecifications"]
        ):
            embeddedDataSpecifications.append(EmbeddedDataSpecification.from_rdf(graph, embedded_ref))

        if len(embeddedDataSpecifications) == 0:
            embeddedDataSpecifications = None

        return HasDataSpecification(embeddedDataSpecifications=embeddedDataSpecifications)
