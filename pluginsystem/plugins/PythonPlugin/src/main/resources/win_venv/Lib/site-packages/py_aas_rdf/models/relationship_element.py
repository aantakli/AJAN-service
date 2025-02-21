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
from typing import Literal

import rdflib
from rdflib import RDF

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.relationship_element_abstract import RelationshipElementAbstract


class RelationshipElement(RelationshipElementAbstract):
    modelType: Literal["RelationshipElement"] = ModelType.RelationshipElement.value

    def to_rdf(
        self,
        graph: rdflib.Graph = None,
        parent_node: rdflib.IdentifiedNode = None,
        prefix_uri: str = "",
        base_uri: str = "",
        id_strategy: str = "",
    ) -> (rdflib.Graph, rdflib.IdentifiedNode):
        created_graph, created_node = super().to_rdf(graph, parent_node, prefix_uri, base_uri, id_strategy)
        created_graph.add((created_node, RDF.type, AASNameSpace.AAS["RelationshipElement"]))
        return created_graph, created_node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "RelationshipElement":
        element = RelationshipElementAbstract.from_rdf(graph, subject)
        return RelationshipElement(
            first=element.first,
            second=element.second,
            qualifiers=element.qualifiers,
            category=element.category,
            idShort=element.idShort,
            displayName=element.displayName,
            description=element.description,
            extensions=element.extensions,
            semanticId=element.semanticId,
            supplementalSemanticIds=element.supplementalSemanticIds,
            embeddedDataSpecifications=element.embeddedDataSpecifications,
        )
