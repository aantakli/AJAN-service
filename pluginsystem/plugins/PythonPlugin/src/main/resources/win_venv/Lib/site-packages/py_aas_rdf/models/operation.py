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
from py_aas_rdf.models.rdfiable import RDFiable
from py_aas_rdf.models.specific_asset_id import SpecificAssetId
from py_aas_rdf.models.submodel_element import SubmodelElement

from enum import Enum
from typing import Any, List, Optional, Union, Literal

import pydantic
from pydantic import BaseModel, Field, constr


# TODO: recheck
class OperationVariable(BaseModel, RDFiable):
    value: "SubmodelElementChoice"

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
        graph.add((node, RDF.type, AASNameSpace.AAS["OperationVariable"]))
        _, created_node = self.value.to_rdf(graph, node)
        graph.add((node, AASNameSpace.AAS["OperationVariable/value"], created_node))
        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "OperationVariable":
        value_value = None
        from py_aas_rdf.models.util import from_unknown_rdf

        value_value_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["OperationVariable/value"]),
            None,
        )
        if value_value_ref:
            value_value = from_unknown_rdf(graph, value_value_ref)
        return OperationVariable(value=value_value)


class Operation(SubmodelElement):
    inputVariables: Optional[List[OperationVariable]] = Field(None, min_length=0)
    outputVariables: Optional[List[OperationVariable]] = Field(None, min_length=0)
    inoutputVariables: Optional[List[OperationVariable]] = Field(None, min_length=0)
    modelType: Literal["Operation"] = ModelType.Operation.value

    def to_rdf(
        self,
        graph: rdflib.Graph = None,
        parent_node: rdflib.IdentifiedNode = None,
        prefix_uri: str = "",
        base_uri: str = "",
        id_strategy: str = "",
    ) -> (rdflib.Graph, rdflib.IdentifiedNode):
        created_graph, created_node = super().to_rdf(graph, parent_node, prefix_uri, base_uri, id_strategy)

        created_graph.add((created_node, RDF.type, AASNameSpace.AAS["Operation"]))
        if self.inputVariables:
            for idx, input_variable in enumerate(self.inputVariables):
                _, created_sub_node = input_variable.to_rdf(created_graph, created_node)
                created_graph.add((created_sub_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                created_graph.add((created_node, AASNameSpace.AAS["Operation/inputVariables"], created_sub_node))

        if self.outputVariables:
            for idx, input_variable in enumerate(self.outputVariables):
                _, created_sub_node = input_variable.to_rdf(created_graph, created_node)
                created_graph.add((created_sub_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                created_graph.add((created_node, AASNameSpace.AAS["Operation/outputVariables"], created_sub_node))
        if self.inoutputVariables:
            for idx, input_variable in enumerate(self.inoutputVariables):
                _, created_sub_node = input_variable.to_rdf(created_graph, created_node)
                created_graph.add((created_sub_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                created_graph.add((created_node, AASNameSpace.AAS["Operation/inoutputVariables"], created_sub_node))

        return created_graph, created_node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "Operation":
        input_variables_value = []
        for variable_ref in graph.objects(subject=subject, predicate=AASNameSpace.AAS["Operation/inputVariables"]):
            element = OperationVariable.from_rdf(graph, variable_ref)
            input_variables_value.append(element)
        if len(input_variables_value) == 0:
            input_variables_value = None

        output_variables_value = []
        for variable_ref in graph.objects(subject=subject, predicate=AASNameSpace.AAS["Operation/outputVariables"]):
            element = OperationVariable.from_rdf(graph, variable_ref)
            output_variables_value.append(element)
        if len(output_variables_value) == 0:
            output_variables_value = None

        inoutput_variables_value = []
        for variable_ref in graph.objects(subject=subject, predicate=AASNameSpace.AAS["Operation/inoutputVariables"]):
            element = OperationVariable.from_rdf(graph, variable_ref)
            inoutput_variables_value.append(element)
        if len(inoutput_variables_value) == 0:
            inoutput_variables_value = None
        submodel_element = SubmodelElement.from_rdf(graph, subject)

        return Operation(
            inputVariables=input_variables_value,
            outputVariables=output_variables_value,
            inoutputVariables=inoutput_variables_value,
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
