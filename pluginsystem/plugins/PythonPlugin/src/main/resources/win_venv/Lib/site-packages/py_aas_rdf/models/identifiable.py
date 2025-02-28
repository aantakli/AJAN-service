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
from rdflib import Graph

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.administrative_information import AdministrativeInformation
from py_aas_rdf.models.rdfiable import RDFiable
from py_aas_rdf.models.referable import Referable


class Identifiable(Referable):
    id: constr(min_length=1, max_length=2000)
    administration: Optional[AdministrativeInformation] = None

    @staticmethod
    def append_as_rdf(instance: "Identifiable", graph: rdflib.Graph, parent_node: rdflib.IdentifiedNode):
        graph.add((parent_node, AASNameSpace.AAS["Identifiable/id"], rdflib.Literal(instance.id)))

        if instance.administration:
            AdministrativeInformation.append_as_rdf(instance.administration, graph, parent_node)

        # Referable
        Referable.append_as_rdf(instance, graph, parent_node)

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "Identifiable":
        # Referable
        referable = Referable.from_rdf(graph, subject)

        id_value = None
        id_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Identifiable/id"]),
            None,
        )
        id_value = id_ref.value

        administration_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Identifiable/administration"]),
            None,
        )
        administration_value = None
        if administration_ref:
            administration_value = AdministrativeInformation.from_rdf(graph, administration_ref)

        return Identifiable(
            id=id_value,
            administration=administration_value,
            category=referable.category,
            idShort=referable.idShort,
            displayName=referable.displayName,
            description=referable.description,
            extensions=referable.extensions,
        )
