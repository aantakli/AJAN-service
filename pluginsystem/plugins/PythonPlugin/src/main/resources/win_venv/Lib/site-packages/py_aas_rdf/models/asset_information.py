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
from rdflib.namespace import FOAF, RDF, Namespace
import pydantic
import rdflib
from pydantic import BaseModel, Field, constr
from rdflib import Graph, XSD
from rdflib.plugins.serializers.turtle import TurtleSerializer

import pydantic
from pydantic import BaseModel, Field, constr

from py_aas_rdf.models import url_encode, base_64_url_encode
from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.asset_kind import AssetKind
from py_aas_rdf.models.rdfiable import RDFiable
from py_aas_rdf.models.resource import Resource
from py_aas_rdf.models.specific_asset_id import SpecificAssetId


class AssetInformation(BaseModel, RDFiable):
    assetKind: AssetKind
    globalAssetId: Optional[
        constr(
            min_length=1,
            max_length=2000,
        )
    ] = None
    specificAssetIds: Optional[List[SpecificAssetId]] = Field(None, min_length=0)
    assetType: Optional[
        constr(
            min_length=1,
            max_length=2000,
        )
    ] = None
    defaultThumbnail: Optional[Resource] = None

    def to_rdf(
        self,
        graph: rdflib.Graph = None,
        parent_node: rdflib.IdentifiedNode = None,
        prefix_uri: str = "",
        base_uri: str = "",
        id_strategy: str = "",
    ) -> (rdflib.Graph, rdflib.IdentifiedNode):
        if graph == None:
            graph = Graph()
            graph.bind("aas", AASNameSpace.AAS)

        if self.globalAssetId:
            if id_strategy == "base64-url-encode":
                node = rdflib.URIRef(f"{base_uri}{base_64_url_encode(self.globalAssetId)}")
            else:
                node = rdflib.URIRef(url_encode(f"{base_uri}{url_encode(self.globalAssetId)}"))
        else:
            node = rdflib.BNode()
        graph.add((node, RDF.type, rdflib.URIRef(AASNameSpace.AAS["AssetInformation"])))

        graph.add(
            (
                node,
                rdflib.URIRef(AASNameSpace.AAS["AssetInformation/assetKind"]),
                rdflib.URIRef(AASNameSpace.AAS[f"AssetKind/{self.assetKind.value}"]),
            )
        )
        if self.globalAssetId:
            graph.add(
                (
                    node,
                    rdflib.URIRef(AASNameSpace.AAS["AssetInformation/globalAssetId"]),
                    rdflib.Literal(self.globalAssetId),
                )
            )
        if self.assetType:
            graph.add(
                (
                    node,
                    rdflib.URIRef(AASNameSpace.AAS["AssetInformation/assetType"]),
                    rdflib.Literal(self.assetType),
                )
            )
        if self.defaultThumbnail:
            thumbnail = rdflib.BNode()
            graph.add((thumbnail, RDF.type, AASNameSpace.AAS["Resource"]))
            graph.add((thumbnail, AASNameSpace.AAS["Resource/path"], rdflib.Literal(self.defaultThumbnail.path)))
            if self.defaultThumbnail.contentType:
                graph.add(
                    (
                        thumbnail,
                        AASNameSpace.AAS["Resource/contentType"],
                        rdflib.Literal(self.defaultThumbnail.contentType),
                    )
                )
            graph.add((node, AASNameSpace.AAS["AssetInformation/defaultThumbnail"], thumbnail))
        if self.specificAssetIds and len(self.specificAssetIds) > 0:
            for idx, specific_asset_id_ref in enumerate(self.specificAssetIds):
                _, created_node = specific_asset_id_ref.to_rdf(graph, node, prefix_uri, base_uri, id_strategy)
                graph.add((created_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add((node, AASNameSpace.AAS["AssetInformation/specificAssetIds"], created_node))
        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        asset_kind_value = None
        asset_kind_uriref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AssetInformation/assetKind"]), None
        )
        if asset_kind_uriref:
            asset_kind_value = asset_kind_uriref[asset_kind_uriref.rfind("/") + 1 :]

        asset_type_value = None
        asset_type_uriref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AssetInformation/assetType"]),
            None,
        )
        if asset_type_uriref:
            asset_type_value = asset_type_uriref.value

        global_asset_id_value = None
        global_asset_id_uriref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AssetInformation/globalAssetId"]),
            None,
        )
        if global_asset_id_uriref:
            global_asset_id_value = global_asset_id_uriref.value

        default_thumbnail_value = None
        default_thumbnail_uriref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AssetInformation/defaultThumbnail"]),
            None,
        )
        if default_thumbnail_uriref:
            path_value = None
            path_uriref: rdflib.Literal = next(
                graph.objects(subject=default_thumbnail_uriref, predicate=AASNameSpace.AAS["Resource/path"]),
                None,
            )
            if path_uriref:
                path_value = path_uriref.value

            content_type_value = None
            content_type_uriref: rdflib.Literal = next(
                graph.objects(subject=default_thumbnail_uriref, predicate=AASNameSpace.AAS["Resource/contentType"]),
                None,
            )
            if content_type_uriref:
                content_type_value = content_type_uriref.value

            default_thumbnail_value = Resource(path=path_value, contentType=content_type_value)

        specific_asset_ids_value = []
        for specific_asset_uref in graph.objects(
            subject=subject, predicate=AASNameSpace.AAS["AssetInformation/specificAssetIds"]
        ):
            specific_asset_id = SpecificAssetId.from_rdf(graph, specific_asset_uref)
            specific_asset_ids_value.append(specific_asset_id)
        if len(specific_asset_ids_value) == 0:
            specific_asset_ids_value = None
        return AssetInformation(
            assetKind=asset_kind_value,
            globalAssetId=global_asset_id_value,
            specificAssetIds=specific_asset_ids_value,
            assetType=asset_type_value,
            defaultThumbnail=default_thumbnail_value,
        )
