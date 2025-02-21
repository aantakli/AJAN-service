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

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.annotated_relationship_element import AnnotatedRelationshipElement
from py_aas_rdf.models.has_data_specification import HasDataSpecification
from py_aas_rdf.models.has_kind import HasKind
from py_aas_rdf.models.has_semantics import HasSemantics
from py_aas_rdf.models.identifiable import Identifiable
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.modeling_kind import ModellingKind
from py_aas_rdf.models.multi_language_property import MultiLanguageProperty
from py_aas_rdf.models.property import Property
from py_aas_rdf.models.qualifable import Qualifiable
from py_aas_rdf.models.qualifier import Qualifier
from py_aas_rdf.models.rdfiable import RDFiable
from py_aas_rdf.models.reference import Reference
from py_aas_rdf.models.submodel_element import SubmodelElement
from py_aas_rdf.models.submodel_element_choice import SubmodelElementChoice
from py_aas_rdf.models.util import from_unknown_rdf
from py_aas_rdf.models import base_64_url_encode, url_encode


class Submodel(Identifiable, HasKind, HasSemantics, Qualifiable, HasDataSpecification, RDFiable):
    # submodelElements: Optional[List[SubmodelElementChoice]] = Field(
    #     None, min_items=0, discriminator="modelType"
    # )
    # pydantic is not so nice with polymorphism so I can't simply say array of SubmodelElements
    submodelElements: Optional[List["SubmodelElementChoice"]] = None
    modelType: Literal["Submodel"] = ModelType.Submodel.value

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
            graph.bind("myaas", base_uri)

        if id_strategy == "base64-url-encode":
            node = rdflib.URIRef(f"{base_uri}{base_64_url_encode(self.id)}")
        else:
            node = rdflib.URIRef(f"{base_uri}{url_encode(self.id)}")
        graph.add((node, RDF.type, AASNameSpace.AAS["Submodel"]))
        # Identifiable
        Identifiable.append_as_rdf(self, graph, node)

        # HasSemantics
        HasSemantics.append_as_rdf(self, graph, node)

        # HasDataSpecification
        HasDataSpecification.append_as_rdf(self, graph, node)

        # HasKind
        if self.kind:
            graph.add(
                (
                    node,
                    AASNameSpace.AAS["HasKind/kind"],
                    rdflib.URIRef(AASNameSpace.AAS[f"ModellingKind/{self.kind.value}"]),
                )
            )
        # Qualifiable
        if self.qualifiers:
            for idx, qualifier_ref in enumerate(self.qualifiers):
                created_node = rdflib.BNode()
                graph.add((created_node, RDF.type, AASNameSpace.AAS["Qualifier"]))
                graph.add((created_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                Qualifier.append_as_rdf(qualifier_ref, graph, created_node)
                graph.add((node, AASNameSpace.AAS["Qualifiable/qualifiers"], created_node))
        # submodelElements
        if self.submodelElements:
            for idx, submodel_element in enumerate(self.submodelElements):
                # headache

                common_pref = ""
                if id_strategy == "base64-url-encode":
                    common_pref = f"{base_64_url_encode(self.id)}/submodel-elements/"
                else:
                    common_pref = f"{url_encode(self.id+'/submodel-elements/')}"

                _, created_node = submodel_element.to_rdf(
                    graph, node, prefix_uri=common_pref, base_uri=base_uri, id_strategy=id_strategy
                )
                graph.add((created_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add((node, AASNameSpace.AAS["Submodel/submodelElements"], created_node))
        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "Submodel":
        # HasSemantics
        has_semantics = HasSemantics.from_rdf(graph, subject)
        # Identifiable
        identifiable = Identifiable.from_rdf(graph, subject)
        # HasDataSpecification
        has_data_specification = HasDataSpecification.from_rdf(graph, subject)

        # HasKind
        kind_value = None
        kind_uriref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["HasKind/kind"]),
            None,
        )
        if kind_uriref:
            kind_value = ModellingKind[kind_uriref[kind_uriref.rfind("/") + 1 :]]
        # Qualifiable
        qualifiers_value = []
        for qualifier_uriref in graph.objects(subject=subject, predicate=AASNameSpace.AAS["Qualifiable/qualifiers"]):
            qualifier = Qualifier.from_rdf(graph, qualifier_uriref)
            qualifiers_value.append(qualifier)
        if len(qualifiers_value) == 0:
            qualifiers_value = None
        # submodelElements
        submodel_elements_value = []

        for submodel_element_uriref in graph.objects(
            subject=subject, predicate=AASNameSpace.AAS["Submodel/submodelElements"]
        ):
            element = from_unknown_rdf(graph, submodel_element_uriref)
            submodel_elements_value.append(element)

        if len(submodel_elements_value) == 0:
            submodel_elements_value = None

        return Submodel(
            kind=kind_value,
            qualifiers=qualifiers_value,
            submodelElements=submodel_elements_value,
            embeddedDataSpecifications=has_data_specification.embeddedDataSpecifications,
            id=identifiable.id,
            administration=identifiable.administration,
            category=identifiable.category,
            idShort=identifiable.idShort,
            displayName=identifiable.displayName,
            description=identifiable.description,
            extensions=identifiable.extensions,
            semanticId=has_semantics.semanticId,
            supplementalSemanticIds=has_semantics.supplementalSemanticIds,
        )
