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
from py_aas_rdf.models.asset_information import AssetInformation
from py_aas_rdf.models.has_data_specification import HasDataSpecification
from py_aas_rdf.models.identifiable import Identifiable
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.rdfiable import RDFiable
from py_aas_rdf.models.reference import Reference
from py_aas_rdf.models.submodel import Submodel
from py_aas_rdf.models import base_64_url_encode, url_encode


class AssetAdministrationShell(Identifiable, HasDataSpecification, RDFiable):
    derivedFrom: Optional[Reference] = None
    assetInformation: AssetInformation
    submodels: Optional[List[Reference]] = Field(None, min_length=0)
    modelType: Literal["AssetAdministrationShell"] = ModelType.AssetAdministrationShell.value

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

        graph.add((node, rdflib.RDF.type, AASNameSpace.AAS["AssetAdministrationShell"]))

        # Identifiable
        # TODO: find a way to refactor
        Identifiable.append_as_rdf(self, graph, node)

        # HasDataSpecification
        HasDataSpecification.append_as_rdf(self, graph, node)

        if self.derivedFrom:
            _, created_node = self.derivedFrom.to_rdf(
                graph, node, base_uri=base_uri, prefix_uri=prefix_uri, id_strategy=id_strategy
            )
            graph.add((node, AASNameSpace.AAS["AssetAdministrationShell/derivedFrom"], created_node))

        _, created_asset_info_node = self.assetInformation.to_rdf(
            graph, node, base_uri=base_uri, prefix_uri=prefix_uri, id_strategy=id_strategy
        )
        graph.add((node, AASNameSpace.AAS["AssetAdministrationShell/assetInformation"], created_asset_info_node))

        if self.submodels and len(self.submodels) > 0:
            for idx, submodel_ref in enumerate(self.submodels):
                _, created_ref_node = submodel_ref.to_rdf(
                    graph, node, base_uri=base_uri, prefix_uri=prefix_uri, id_strategy=id_strategy
                )
                graph.add((created_ref_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add((node, AASNameSpace.AAS["AssetAdministrationShell/submodels"], created_ref_node))

        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        # Identifiable
        identifiable = Identifiable.from_rdf(graph, subject)

        # HasDataSpecification
        has_data_specification = HasDataSpecification.from_rdf(graph, subject)

        derived_from_value = None
        derived_from_value_uriref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AssetAdministrationShell/derivedFrom"]),
            None,
        )
        if derived_from_value_uriref:
            derived_from_value = Reference.from_rdf(graph, derived_from_value_uriref)

        asset_information_value = None
        asset_information_value_uriref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AssetAdministrationShell/assetInformation"]),
            None,
        )
        if asset_information_value_uriref:
            asset_information_value = AssetInformation.from_rdf(graph, asset_information_value_uriref)

        submodels_value = []
        for submodel_uriref in graph.objects(
            subject=subject, predicate=AASNameSpace.AAS["AssetAdministrationShell/submodels"]
        ):
            submodel_ref = Reference.from_rdf(graph, submodel_uriref)
            submodels_value.append(submodel_ref)
        if len(submodels_value) == 0:
            submodels_value = None
        return AssetAdministrationShell(
            derivedFrom=derived_from_value,
            assetInformation=asset_information_value,
            submodels=submodels_value,
            id=identifiable.id,
            administration=identifiable.administration,
            category=identifiable.category,
            idShort=identifiable.idShort,
            displayName=identifiable.displayName,
            description=identifiable.description,
            extensions=identifiable.extensions,
            embeddedDataSpecifications=has_data_specification.embeddedDataSpecifications,
        )
