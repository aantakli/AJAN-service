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
from py_aas_rdf.models.data_specification_iec_61360 import DataSpecificationIec61360
from py_aas_rdf.models.rdfiable import RDFiable
from py_aas_rdf.models.reference import Reference


class EmbeddedDataSpecification(BaseModel, RDFiable):
    dataSpecification: Reference
    dataSpecificationContent: Union[DataSpecificationIec61360]

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
        graph.add((node, rdflib.RDF.type, AASNameSpace.AAS["EmbeddedDataSpecification"]))
        _, created_data_specification_node = self.dataSpecification.to_rdf(graph=graph, parent_node=node)
        graph.add(
            (node, AASNameSpace.AAS["EmbeddedDataSpecification/dataSpecification"], created_data_specification_node)
        )

        _, created_content_node = self.dataSpecificationContent.to_rdf(graph=graph, parent_node=node)
        graph.add(
            (
                node,
                AASNameSpace.AAS["EmbeddedDataSpecification/dataSpecificationContent"],
                created_content_node,
            )
        )

        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        # TODO: use type as discriminator

        data_specification_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["EmbeddedDataSpecification/dataSpecification"]),
            None,
        )
        data_specification = Reference.from_rdf(graph, data_specification_ref)
        content = None
        content_ref: rdflib.URIRef = next(
            graph.objects(
                subject=subject, predicate=AASNameSpace.AAS["EmbeddedDataSpecification/dataSpecificationContent"]
            ),
            None,
        )
        content_ref_type: rdflib.URIRef = next(
            graph.objects(subject=content_ref, predicate=rdflib.RDF.type),
            None,
        )
        if content_ref_type == AASNameSpace.AAS["DataSpecificationIec61360"]:
            content = DataSpecificationIec61360.from_rdf(graph, content_ref)
        return EmbeddedDataSpecification(dataSpecification=data_specification, dataSpecificationContent=content)
