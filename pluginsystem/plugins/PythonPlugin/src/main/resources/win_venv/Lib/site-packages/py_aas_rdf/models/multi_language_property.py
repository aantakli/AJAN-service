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
from rdflib import RDF

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.data_element import DataElement
from py_aas_rdf.models.lang_string_text_type import LangStringTextType
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.reference import Reference
from py_aas_rdf.models.submodel_element import SubmodelElement


class MultiLanguageProperty(DataElement):
    value: Optional[List[LangStringTextType]] = Field(None, min_length=0)
    valueId: Optional[Reference] = None
    modelType: Literal["MultiLanguageProperty"] = ModelType.MultiLanguageProperty.value

    def to_rdf(
        self,
        graph: rdflib.Graph = None,
        parent_node: rdflib.IdentifiedNode = None,
        prefix_uri: str = "",
        base_uri: str = "",
        id_strategy: str = "",
    ) -> (rdflib.Graph, rdflib.IdentifiedNode):
        created_graph, created_node = super().to_rdf(graph, parent_node, prefix_uri, base_uri, id_strategy)
        created_graph.add((created_node, RDF.type, AASNameSpace.AAS["MultiLanguageProperty"]))
        # TODO: Empty string should not be None
        if self.value:
            for lang_value in self.value:
                lang_node = rdflib.BNode()
                created_graph.add((lang_node, RDF.type, AASNameSpace.AAS["LangStringTextType"]))
                created_graph.add(
                    (lang_node, AASNameSpace.AAS["AbstractLangString/language"], rdflib.Literal(lang_value.language))
                )
                created_graph.add(
                    (lang_node, AASNameSpace.AAS["AbstractLangString/text"], rdflib.Literal(lang_value.text))
                )
                created_graph.add((created_node, AASNameSpace.AAS["MultiLanguageProperty/value"], lang_node))
        if self.valueId:
            _, value_id_ref_node = self.valueId.to_rdf(created_graph, created_node)
            created_graph.add((created_node, AASNameSpace.AAS["MultiLanguageProperty/valueId"], value_id_ref_node))
        return created_graph, created_node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "MultiLanguageProperty":
        value_value = []
        for lang_ref in graph.objects(subject=subject, predicate=AASNameSpace.AAS["MultiLanguageProperty/value"]):
            lang_value = None
            lang_uriref: rdflib.Literal = next(
                graph.objects(subject=lang_ref, predicate=AASNameSpace.AAS["AbstractLangString/language"]), None
            )
            if lang_uriref:
                lang_value = lang_uriref.value
            text_value = None
            text_uriref: rdflib.Literal = next(
                graph.objects(subject=lang_ref, predicate=AASNameSpace.AAS["AbstractLangString/text"]), None
            )
            if text_uriref:
                text_value = text_uriref.value
            language = LangStringTextType(language=lang_value, text=text_value)
            value_value.append(language)

        if len(value_value) == 0:
            value_value = None

        value_id_value = None
        value_id_value_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["MultiLanguageProperty/valueId"]),
            None,
        )
        if value_id_value_ref:
            value_id_value = Reference.from_rdf(graph, value_id_value_ref)
        submodel_element = SubmodelElement.from_rdf(graph, subject)
        return MultiLanguageProperty(
            value=value_value,
            valueId=value_id_value,
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
