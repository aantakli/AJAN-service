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

import pydantic
import rdflib
from pydantic import BaseModel, Field, constr

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.key_types import KeyTypes
from py_aas_rdf.models.rdfiable import RDFiable


class Key(BaseModel, RDFiable):
    type: KeyTypes
    value: constr(min_length=1, max_length=2000)
    # TODO: Add Pattern for Key

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

        graph.add((node, rdflib.RDF.type, AASNameSpace.AAS["Key"]))
        graph.add(
            (
                node,
                AASNameSpace.AAS["Key/type"],
                AASNameSpace.AAS[f"KeyTypes/{self.type.value}"],
            )
        )
        graph.add(
            (
                node,
                AASNameSpace.AAS["Key/value"],
                rdflib.Literal(self.value),
            )
        )
        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        payload = {}
        key_type: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Key/type"]),
            None,
        )
        value: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Key/value"]),
            None,
        )
        payload["type"] = key_type[key_type.rfind("/") + 1 :]
        payload["value"] = value.value
        return Key(**payload)


class SubmodelKey(Key):
    type: KeyTypes = KeyTypes.Submodel
    value: constr(min_length=1, max_length=2000)
