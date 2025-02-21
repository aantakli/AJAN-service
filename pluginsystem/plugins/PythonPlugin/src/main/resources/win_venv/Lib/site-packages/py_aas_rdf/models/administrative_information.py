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
from py_aas_rdf.models.has_data_specification import HasDataSpecification
from py_aas_rdf.models.reference import Reference


class AdministrativeInformation(HasDataSpecification):
    version: Optional[constr(min_length=1, max_length=4, pattern=r"^(0|[1-9][0-9]*)$")] = None
    revision: Optional[constr(min_length=1, max_length=4, pattern=r"^(0|[1-9][0-9]*)$")] = None
    creator: Optional[Reference] = None
    templateId: Optional[constr(min_length=1, max_length=2000)] = None

    @staticmethod
    def append_as_rdf(instance: "AdministrativeInformation", graph: rdflib.Graph, parent_node: rdflib.IdentifiedNode):
        node = rdflib.BNode()
        graph.add((node, rdflib.RDF.type, AASNameSpace.AAS["AdministrativeInformation"]))
        HasDataSpecification.append_as_rdf(instance, graph, node)
        if instance.version:
            graph.add((node, AASNameSpace.AAS["AdministrativeInformation/version"], rdflib.Literal(instance.version)))
        if instance.revision:
            graph.add((node, AASNameSpace.AAS["AdministrativeInformation/revision"], rdflib.Literal(instance.revision)))
        if instance.creator:
            _, created_creator_node = instance.creator.to_rdf(graph, node)
            graph.add((node, AASNameSpace.AAS["AdministrativeInformation/creator"], created_creator_node))

        if instance.templateId:
            graph.add(
                (node, AASNameSpace.AAS["AdministrativeInformation/templateId"], rdflib.Literal(instance.templateId))
            )

        graph.add((parent_node, AASNameSpace.AAS["Identifiable/administration"], node))

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        # HasDataSpecification
        hasDataSpecification = HasDataSpecification.from_rdf(graph, subject)

        version_value = None
        version_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AdministrativeInformation/version"]),
            None,
        )
        if version_ref:
            version_value = version_ref.value
        revision_value = None
        revision_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AdministrativeInformation/revision"]),
            None,
        )
        if revision_ref:
            revision_value = revision_ref.value
        creator_value = None
        creator_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AdministrativeInformation/creator"]),
            None,
        )
        if creator_ref:
            creator_value = Reference.from_rdf(graph, creator_ref)
        template_id_value = None
        template_id_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AdministrativeInformation/templateId"]),
            None,
        )
        if template_id_ref:
            template_id_value = template_id_ref.value
        return AdministrativeInformation(
            version=version_value,
            revision=revision_value,
            creator=creator_value,
            templateId=template_id_value,
            embeddedDataSpecifications=hasDataSpecification.embeddedDataSpecifications,
        )
