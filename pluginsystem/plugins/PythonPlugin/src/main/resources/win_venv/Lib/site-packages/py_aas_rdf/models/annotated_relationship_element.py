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
from typing import Any, List, Optional, Union, Literal, Annotated

import rdflib
from pydantic import BaseModel, Field, constr
from rdflib import RDF

from py_aas_rdf.models.aas_namespace import AASNameSpace
from py_aas_rdf.models.blob import Blob
from py_aas_rdf.models.file import File
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.multi_language_property import MultiLanguageProperty
from py_aas_rdf.models.property import Property
from py_aas_rdf.models.range import Range
from py_aas_rdf.models.reference_element import ReferenceElement
from py_aas_rdf.models.relationship_element_abstract import RelationshipElementAbstract


DataElementChoice = Annotated[
    Union[Property, MultiLanguageProperty, Range, ReferenceElement, File, Blob],
    Field(discriminator="modelType"),
]


class AnnotatedRelationshipElement(RelationshipElementAbstract):
    annotations: Optional[List[DataElementChoice]] = Field(None, min_length=0)
    modelType: Literal["AnnotatedRelationshipElement"] = ModelType.AnnotatedRelationshipElement.value

    def to_rdf(
        self,
        graph: rdflib.Graph = None,
        parent_node: rdflib.IdentifiedNode = None,
        prefix_uri: str = "",
        base_uri: str = "",
        id_strategy: str = "",
    ) -> (rdflib.Graph, rdflib.IdentifiedNode):
        created_graph, created_node = super().to_rdf(graph, parent_node, prefix_uri, base_uri, id_strategy)

        created_graph.add((created_node, RDF.type, AASNameSpace.AAS["AnnotatedRelationshipElement"]))
        if self.annotations:
            for idx, annotation in enumerate(self.annotations):
                _, created_sub_node = annotation.to_rdf(
                    graph,
                    created_node,
                    base_uri=base_uri,
                    prefix_uri=prefix_uri + self.idShort + ".",
                    id_strategy=id_strategy,
                )
                created_graph.add((created_sub_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                created_graph.add(
                    (created_node, AASNameSpace.AAS["AnnotatedRelationshipElement/annotations"], created_sub_node)
                )
        return created_graph, created_node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode) -> "AnnotatedRelationshipElement":
        relation_element_abstract = RelationshipElementAbstract.from_rdf(graph, subject)
        annotations_value = []
        from py_aas_rdf.models.util import from_unknown_rdf

        for annotation_uriref in graph.objects(
            subject=subject, predicate=AASNameSpace.AAS["AnnotatedRelationshipElement/annotations"]
        ):
            element = from_unknown_rdf(graph, annotation_uriref)
            annotations_value.append(element)
        if len(annotations_value) == 0:
            annotations_value = None
        return AnnotatedRelationshipElement(
            annotations=annotations_value,
            first=relation_element_abstract.first,
            second=relation_element_abstract.second,
            qualifiers=relation_element_abstract.qualifiers,
            category=relation_element_abstract.category,
            idShort=relation_element_abstract.idShort,
            displayName=relation_element_abstract.displayName,
            description=relation_element_abstract.description,
            extensions=relation_element_abstract.extensions,
            semanticId=relation_element_abstract.semanticId,
            supplementalSemanticIds=relation_element_abstract.supplementalSemanticIds,
            embeddedDataSpecifications=relation_element_abstract.embeddedDataSpecifications,
        )
