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

import pydantic
import rdflib
from pydantic import BaseModel, Field, constr
from rdflib import RDF

from py_aas_rdf.models import base_64_url_encode, url_encode
from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.has_data_specification import HasDataSpecification
from py_aas_rdf.models.has_semantics import HasSemantics
from py_aas_rdf.models.qualifable import Qualifiable
from py_aas_rdf.models.qualifier import Qualifier
from py_aas_rdf.models.rdfiable import RDFiable
from py_aas_rdf.models.referable import Referable


class SubmodelElement(Referable, HasSemantics, Qualifiable, HasDataSpecification, RDFiable):
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

        node = rdflib.URIRef(f"{base_uri}{prefix_uri}{self.idShort}")

        # Referable
        Referable.append_as_rdf(self, graph, node)
        # HasSemantics
        HasSemantics.append_as_rdf(self, graph, node)
        # Qualifiable
        if self.qualifiers:
            for idx, qualifier_ref in enumerate(self.qualifiers):
                created_node = rdflib.BNode()
                graph.add((created_node, RDF.type, AASNameSpace.AAS["Qualifier"]))
                graph.add((created_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                Qualifier.append_as_rdf(qualifier_ref, graph, created_node)
                graph.add((node, AASNameSpace.AAS["Qualifiable/qualifiers"], created_node))
        # HasDataSpecification
        HasDataSpecification.append_as_rdf(self, graph, node)

        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "SubmodelElement":
        # Referable
        referable = Referable.from_rdf(graph, subject)
        # HasSemantics
        has_semantics = HasSemantics.from_rdf(graph, subject)

        # HasDataSpecification
        has_data_specification = HasDataSpecification.from_rdf(graph, subject)

        qualifiers_value = []
        for qualifier_uriref in graph.objects(subject=subject, predicate=AASNameSpace.AAS["Qualifiable/qualifiers"]):
            qualifier = Qualifier.from_rdf(graph, qualifier_uriref)
            qualifiers_value.append(qualifier)
        if len(qualifiers_value) == 0:
            qualifiers_value = None

        return SubmodelElement(
            qualifiers=qualifiers_value,
            category=referable.category,
            idShort=referable.idShort,
            displayName=referable.displayName,
            description=referable.description,
            extensions=referable.extensions,
            semanticId=has_semantics.semanticId,
            supplementalSemanticIds=has_semantics.supplementalSemanticIds,
            embeddedDataSpecifications=has_data_specification.embeddedDataSpecifications,
        )
