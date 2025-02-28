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
from pydantic import BaseModel
from rdflib import RDF

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.event_element import EventElement
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.reference import Reference
from enum import Enum
from typing import Any, List, Optional, Union, Literal

import pydantic
from pydantic import BaseModel, Field, constr

from py_aas_rdf.models.annotated_relationship_element import AnnotatedRelationshipElement
from py_aas_rdf.models.blob import Blob
from py_aas_rdf.models.file import File
from py_aas_rdf.models.multi_language_property import MultiLanguageProperty
from py_aas_rdf.models.property import Property
from py_aas_rdf.models.range import Range
from py_aas_rdf.models.reference_element import ReferenceElement
from py_aas_rdf.models.relationship_element import RelationshipElement
from py_aas_rdf.models.submodel_element import SubmodelElement


class Direction(Enum):
    Input = "input"
    Output = "output"


class StateOfEvent(Enum):
    Off = "off"
    On = "on"


class BasicEventElement(EventElement):
    observed: Reference
    direction: Direction
    state: StateOfEvent
    messageTopic: Optional[
        constr(
            min_length=1,
            max_length=255,
        )
    ] = None
    messageBroker: Optional[Reference] = None
    lastUpdate: Optional[constr()] = None
    minInterval: Optional[constr()] = None
    maxInterval: Optional[constr()] = None
    modelType: Literal["BasicEventElement"] = ModelType.BasicEventElement.value

    def to_rdf(
        self,
        graph: rdflib.Graph = None,
        parent_node: rdflib.IdentifiedNode = None,
        prefix_uri: str = "",
        base_uri: str = "",
        id_strategy: str = "",
    ) -> (rdflib.Graph, rdflib.IdentifiedNode):
        created_graph, created_node = super().to_rdf(graph, parent_node, prefix_uri, base_uri, id_strategy)
        created_graph.add((created_node, RDF.type, AASNameSpace.AAS["BasicEventElement"]))

        _, created_observed_node = self.observed.to_rdf(created_graph, created_node)
        created_graph.add((created_node, AASNameSpace.AAS["BasicEventElement/observed"], created_observed_node))
        created_graph.add(
            (
                created_node,
                AASNameSpace.AAS["BasicEventElement/direction"],
                AASNameSpace.AAS[f"Direction/{self.direction.name}"],
            )
        )
        created_graph.add(
            (
                created_node,
                AASNameSpace.AAS["BasicEventElement/state"],
                AASNameSpace.AAS[f"StateOfEvent/{self.state.name}"],
            )
        )
        if self.messageTopic:
            _, created_message_broker_node = self.messageBroker.to_rdf(created_graph, created_node)
            created_graph.add(
                (created_node, AASNameSpace.AAS["BasicEventElement/messageBroker"], created_message_broker_node)
            )
        if self.messageBroker:
            created_graph.add(
                (created_node, AASNameSpace.AAS["BasicEventElement/messageTopic"], rdflib.Literal(self.messageTopic))
            )
        if self.lastUpdate:
            created_graph.add(
                (created_node, AASNameSpace.AAS["BasicEventElement/lastUpdate"], rdflib.Literal(self.lastUpdate))
            )
        if self.minInterval:
            created_graph.add(
                (created_node, AASNameSpace.AAS["BasicEventElement/minInterval"], rdflib.Literal(self.minInterval))
            )
        if self.maxInterval:
            created_graph.add(
                (created_node, AASNameSpace.AAS["BasicEventElement/maxInterval"], rdflib.Literal(self.maxInterval))
            )
        return created_graph, created_node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "BasicEventElement":
        observed_value = None
        observed_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["BasicEventElement/observed"]),
            None,
        )
        if observed_ref:
            observed_value = Reference.from_rdf(graph, observed_ref)

        direction_value = None
        direction_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["BasicEventElement/direction"]),
            None,
        )
        if direction_ref:
            direction_value = Direction[direction_ref[direction_ref.rfind("/") + 1 :]]

        state_value = None
        state_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["BasicEventElement/state"]),
            None,
        )
        if state_ref:
            state_value = StateOfEvent[state_ref[state_ref.rfind("/") + 1 :]]

        message_topic_value = None
        message_topic_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["BasicEventElement/messageTopic"]),
            None,
        )
        if message_topic_ref:
            message_topic_value = message_topic_ref.value

        message_broker_value = None
        message_broker_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["BasicEventElement/messageBroker"]),
            None,
        )
        if message_broker_ref:
            message_broker_value = Reference.from_rdf(graph, message_broker_ref)

        last_update_value = None
        last_update_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["BasicEventElement/lastUpdate"]),
            None,
        )
        if last_update_ref:
            last_update_value = last_update_ref.value
        min_interval_value = None
        min_interval_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["BasicEventElement/minInterval"]),
            None,
        )
        if min_interval_ref:
            min_interval_value = min_interval_ref.value

        max_interval_value = None
        max_interval_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["BasicEventElement/maxInterval"]),
            None,
        )
        if max_interval_ref:
            max_interval_value = max_interval_ref.value

        submodel_element = SubmodelElement.from_rdf(graph, subject)
        return BasicEventElement(
            observed=observed_value,
            direction=direction_value,
            state=state_value,
            messageTopic=message_topic_value,
            messageBroker=message_broker_value,
            lastUpdate=last_update_value,
            minInterval=min_interval_value,
            maxInterval=max_interval_value,
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
