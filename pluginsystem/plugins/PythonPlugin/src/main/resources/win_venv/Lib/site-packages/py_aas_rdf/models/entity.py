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
import rdflib
from rdflib import RDF

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.specific_asset_id import SpecificAssetId
from py_aas_rdf.models.submodel_element import SubmodelElement

from enum import Enum
from typing import Any, List, Optional, Union, Literal

import pydantic
from pydantic import BaseModel, Field, constr


class EntityType(Enum):
    CoManagedEntity = "CoManagedEntity"
    SelfManagedEntity = "SelfManagedEntity"


class Entity(SubmodelElement):
    statements: Optional[List["SubmodelElementChoice"]] = Field(None, min_length=0)
    entityType: EntityType
    globalAssetId: Optional[
        constr(
            min_length=1,
            max_length=2000,
        )
    ] = None
    specificAssetIds: Optional[List[SpecificAssetId]] = Field(None, min_length=0)
    modelType: Literal["Entity"] = ModelType.Entity.value

    def to_rdf(
        self,
        graph: rdflib.Graph = None,
        parent_node: rdflib.IdentifiedNode = None,
        prefix_uri: str = "",
        base_uri: str = "",
        id_strategy: str = "",
    ) -> (rdflib.Graph, rdflib.IdentifiedNode):
        created_graph, created_node = super().to_rdf(graph, parent_node, prefix_uri, base_uri, id_strategy)
        created_graph.add((created_node, RDF.type, AASNameSpace.AAS["Entity"]))

        created_graph.add(
            (
                created_node,
                AASNameSpace.AAS["Entity/entityType"],
                AASNameSpace.AAS[f"EntityType/{self.entityType.name}"],
            )
        )
        if self.statements:
            for idx, statement in enumerate(self.statements):
                _, created_sub_node = statement.to_rdf(
                    graph,
                    created_node,
                    prefix_uri=prefix_uri + self.idShort + ".",
                    base_uri=base_uri,
                    id_strategy=id_strategy,
                )
                graph.add((created_sub_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add((created_node, AASNameSpace.AAS["Entity/statements"], created_sub_node))
        if self.globalAssetId:
            created_graph.add(
                (
                    created_node,
                    AASNameSpace.AAS["Entity/globalAssetId"],
                    rdflib.Literal(self.globalAssetId),
                )
            )

        if self.specificAssetIds:
            for idx, specific_asset_id in enumerate(self.specificAssetIds):
                _, created_sub_node = specific_asset_id.to_rdf(graph, created_node, prefix_uri=str(created_node) + ".")
                graph.add((created_sub_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add((created_node, AASNameSpace.AAS["Entity/specificAssetIds"], created_sub_node))
        return created_graph, created_node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "Entity":
        statements_value = []
        from py_aas_rdf.models.util import from_unknown_rdf

        for statement_uriref in graph.objects(subject=subject, predicate=AASNameSpace.AAS["Entity/statements"]):
            element = from_unknown_rdf(graph, statement_uriref)
            statements_value.append(element)

        if len(statements_value) == 0:
            statements_value = None

        entity_type_value = None
        entity_type_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Entity/entityType"]),
            None,
        )
        if entity_type_ref:
            entity_type_value = EntityType[entity_type_ref[entity_type_ref.rfind("/") + 1 :]]

        global_asset_id_value = None
        global_asset_id_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["Entity/globalAssetId"]),
            None,
        )
        if global_asset_id_ref:
            global_asset_id_value = global_asset_id_ref.value

        specificAssetIds_value = []
        for statement_uriref in graph.objects(subject=subject, predicate=AASNameSpace.AAS["Entity/specificAssetIds"]):
            element = SpecificAssetId.from_rdf(graph, statement_uriref)
            specificAssetIds_value.append(element)
        if len(specificAssetIds_value) == 0:
            specificAssetIds_value = None
        submodel_element = SubmodelElement.from_rdf(graph, subject)

        return Entity(
            statements=statements_value,
            entityType=entity_type_value,
            globalAssetId=global_asset_id_value,
            specificAssetIds=specificAssetIds_value,
            qualifiers=submodel_element.qualifiers,
            category=submodel_element.category,
            idShort=submodel_element.idShort,
            displayName=submodel_element.displayName,
            description=submodel_element.description,
            extensions=submodel_element.extensions,
            semanticId=submodel_element.semanticId,
            supplementalSemanticIds=submodel_element.supplementalSemanticIds,
            embeddedDataSpecifications=submodel_element.embeddedDataSpecifications,
        )
