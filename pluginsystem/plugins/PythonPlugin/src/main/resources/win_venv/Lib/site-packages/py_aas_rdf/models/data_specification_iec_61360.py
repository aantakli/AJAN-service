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
from py_aas_rdf.models.abstract_lang_string import AbstractLangString
from py_aas_rdf.models.model_type import ModelType
from py_aas_rdf.models.rdfiable import RDFiable
from py_aas_rdf.models.reference import Reference


class LangStringPreferredNameTypeIec61360(AbstractLangString, RDFiable):
    text: constr(min_length=1, max_length=255)

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
        graph.add((node, rdflib.RDF.type, AASNameSpace.AAS["LangStringPreferredNameTypeIec61360"]))
        graph.add((node, AASNameSpace.AAS["AbstractLangString/language"], rdflib.Literal(self.language)))
        graph.add((node, AASNameSpace.AAS["AbstractLangString/text"], rdflib.Literal(self.text)))
        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        language: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AbstractLangString/language"]), None
        )
        text: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AbstractLangString/text"]),
            None,
        )
        return LangStringPreferredNameTypeIec61360(language=language.value, text=text.value)


class LangStringShortNameTypeIec61360(AbstractLangString, RDFiable):
    text: constr(min_length=1, max_length=18)

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
        graph.add((node, rdflib.RDF.type, AASNameSpace.AAS["LangStringShortNameTypeIec61360"]))
        graph.add((node, AASNameSpace.AAS["AbstractLangString/language"], rdflib.Literal(self.language)))
        graph.add((node, AASNameSpace.AAS["AbstractLangString/text"], rdflib.Literal(self.text)))
        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        language: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AbstractLangString/language"]), None
        )
        text: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AbstractLangString/text"]),
            None,
        )
        return LangStringShortNameTypeIec61360(language=language.value, text=text.value)


class LangStringDefinitionTypeIec61360(AbstractLangString, RDFiable):
    text: constr(min_length=1, max_length=1023)

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
        graph.add((node, rdflib.RDF.type, AASNameSpace.AAS["LangStringDefinitionTypeIec61360"]))
        graph.add((node, AASNameSpace.AAS["AbstractLangString/language"], rdflib.Literal(self.language)))
        graph.add((node, AASNameSpace.AAS["AbstractLangString/text"], rdflib.Literal(self.text)))
        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        language: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AbstractLangString/language"]), None
        )
        text: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["AbstractLangString/text"]),
            None,
        )
        return LangStringDefinitionTypeIec61360(language=language.value, text=text.value)


class DataTypeIec61360(Enum):
    # TODO: Critical, change class names so it match the namespace
    BLOB = "BLOB"
    BOOLEAN = "BOOLEAN"
    Date = "DATE"
    FILE = "FILE"
    HTML = "HTML"
    INTEGER_COUNT = "INTEGER_COUNT"
    INTEGER_CURRENCY = "INTEGER_CURRENCY"
    INTEGER_MEASURE = "INTEGER_MEASURE"
    IRDI = "IRDI"
    IRI = "IRI"
    RATIONAL = "RATIONAL"
    RATIONAL_MEASURE = "RATIONAL_MEASURE"
    REAL_COUNT = "REAL_COUNT"
    REAL_CURRENCY = "REAL_CURRENCY"
    REAL_MEASURE = "REAL_MEASURE"
    STRING = "STRING"
    STRING_TRANSLATABLE = "STRING_TRANSLATABLE"
    TIME = "TIME"
    TIMESTAMP = "TIMESTAMP"


class LevelType(BaseModel, RDFiable):
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
        graph.add((node, rdflib.RDF.type, AASNameSpace.AAS["LevelType"]))
        graph.add((node, AASNameSpace.AAS["LevelType/min"], rdflib.Literal(self.min)))
        graph.add((node, AASNameSpace.AAS["LevelType/nom"], rdflib.Literal(self.nom)))
        graph.add((node, AASNameSpace.AAS["LevelType/typ"], rdflib.Literal(self.typ)))
        graph.add((node, AASNameSpace.AAS["LevelType/max"], rdflib.Literal(self.max)))
        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        min_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["LevelType/min"]), None
        )
        nom_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["LevelType/nom"]), None
        )
        typ_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["LevelType/typ"]), None
        )
        max_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["LevelType/max"]), None
        )
        return LevelType.model_construct(min=min_ref.value, nom=nom_ref.value, typ=typ_ref.value, max=max_ref.value)

    min: bool
    nom: bool
    typ: bool
    max: bool


class ValueReferencePair(BaseModel, RDFiable):
    value: constr(min_length=1, max_length=2000)
    valueId: Reference


class ValueList(BaseModel, RDFiable):
    # TODO: MinLength
    valueReferencePairs: List[ValueReferencePair] = Field(..., min_length=0)


class DataSpecificationIec61360(BaseModel, RDFiable):
    # TODO: MinLength
    preferredName: List[LangStringPreferredNameTypeIec61360] = Field(..., min_length=0)
    # TODO: MinLength
    shortName: Optional[List[LangStringShortNameTypeIec61360]] = Field(None, min_length=0)
    unit: Optional[constr(min_length=1)] = None
    unitId: Optional[Reference] = None
    sourceOfDefinition: Optional[constr(min_length=1)] = None
    symbol: Optional[constr(min_length=1)] = None
    dataType: Optional[DataTypeIec61360] = None
    # TODO: MinLength
    definition: Optional[List[LangStringDefinitionTypeIec61360]] = Field(None, min_length=0)
    valueFormat: Optional[constr(min_length=1)] = None
    valueList: Optional[ValueList] = None
    value: Optional[constr(min_length=1, max_length=2000)] = None
    levelType: Optional[LevelType] = None
    modelType: ModelType = ModelType.DataSpecificationIec61360

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
        graph.add((node, rdflib.RDF.type, AASNameSpace.AAS["DataSpecificationIec61360"]))
        for idx, preferredName in enumerate(self.preferredName):
            _, created_node = preferredName.to_rdf(
                graph=graph, parent_node=node, prefix_uri=prefix_uri, base_uri=base_uri, id_strategy=id_strategy
            )
            graph.add((created_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
            graph.add((node, AASNameSpace.AAS["DataSpecificationIec61360/preferredName"], created_node))

        if self.shortName:
            for idx, shortName in enumerate(self.shortName):
                _, created_node = shortName.to_rdf(
                    graph=graph, parent_node=node, prefix_uri=prefix_uri, base_uri=base_uri, id_strategy=id_strategy
                )
                graph.add((created_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add(
                    (
                        node,
                        AASNameSpace.AAS["DataSpecificationIec61360/shortName"],
                        created_node,
                    )
                )

        if self.unit:
            graph.add((node, AASNameSpace.AAS["DataSpecificationIec61360/unit"], rdflib.Literal(self.unit)))

        if self.unitId:
            _, created_unit_id_node = self.unitId.to_rdf(graph=graph, parent_node=node)
            graph.add((node, AASNameSpace.AAS["DataSpecificationIec61360/unitId"], created_unit_id_node))

        if self.sourceOfDefinition:
            graph.add(
                (
                    node,
                    AASNameSpace.AAS["DataSpecificationIec61360/sourceOfDefinition"],
                    rdflib.Literal(self.sourceOfDefinition),
                )
            )

        if self.symbol:
            graph.add(
                (
                    node,
                    AASNameSpace.AAS["DataSpecificationIec61360/symbol"],
                    rdflib.Literal(self.symbol),
                )
            )
        if self.dataType:
            graph.add(
                (
                    node,
                    AASNameSpace.AAS["DataSpecificationIec61360/dataType"],
                    AASNameSpace.AAS[f"DataTypeIec61360/{self.dataType.name}"],
                )
            )

        if self.definition:
            for idx, definition_lang_text in enumerate(self.definition):
                _, created_node = definition_lang_text.to_rdf(graph=graph, parent_node=node)
                graph.add((created_node, AASNameSpace.AAS["index"], rdflib.Literal(idx)))
                graph.add(
                    (
                        node,
                        AASNameSpace.AAS["DataSpecificationIec61360/definition"],
                        created_node,
                    )
                )

        if self.valueFormat:
            graph.add(
                (
                    node,
                    AASNameSpace.AAS["DataSpecificationIec61360/valueFormat"],
                    rdflib.Literal(self.valueFormat),
                )
            )
        if self.value:
            graph.add(
                (
                    node,
                    AASNameSpace.AAS["DataSpecificationIec61360/value"],
                    rdflib.Literal(self.value),
                )
            )
        if self.levelType:
            _, created_level_type_node = self.levelType.to_rdf(graph=graph, parent_node=node)
            graph.add((node, AASNameSpace.AAS["DataSpecificationIec61360/levelType"], created_level_type_node))

        return graph, node

    @staticmethod
    def from_rdf(graph: rdflib.Graph, subject: rdflib.IdentifiedNode):
        # TODO: !
        pref_name_langs = []
        for lang in graph.objects(
            subject=subject, predicate=AASNameSpace.AAS["DataSpecificationIec61360/preferredName"]
        ):
            pref_name_langs.append(LangStringPreferredNameTypeIec61360.from_rdf(graph, lang))

        short_name_langs = []
        for lang in graph.objects(subject=subject, predicate=AASNameSpace.AAS["DataSpecificationIec61360/shortName"]):
            short_name_langs.append(LangStringShortNameTypeIec61360.from_rdf(graph, lang))
        if len(short_name_langs) == 0:
            short_name_langs = None

        unit = None
        unit_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["DataSpecificationIec61360/unit"]),
            None,
        )
        if unit_ref:
            unit = unit_ref.value

        unit_id = None
        unit_id_ref: rdflib.URIRef = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["DataSpecificationIec61360/unitId"]),
            None,
        )
        if unit_id_ref:
            unit_id = Reference.from_rdf(graph, unit_id_ref)

        source_of_def = None
        source_of_def_ref: rdflib.Literal = next(
            graph.objects(subject=subject, predicate=AASNameSpace.AAS["DataSpecificationIec61360/sourceOfDefinition"]),
            None,
        )
        if source_of_def_ref:
            source_of_def = source_of_def_ref.value

        symbol = None
        symbol_ref: rdflib.Literal = next(
            graph.objects(
                subject=subject,
                predicate=AASNameSpace.AAS["DataSpecificationIec61360/symbol"],
            ),
            None,
        )
        if symbol_ref:
            symbol = symbol_ref.value

        deta_type = None
        deta_type_ref: rdflib.URIRef = next(
            graph.objects(
                subject=subject,
                predicate=AASNameSpace.AAS["DataSpecificationIec61360/dataType"],
            ),
            None,
        )
        if deta_type_ref:
            deta_type = DataTypeIec61360[deta_type_ref[deta_type_ref.rfind("/") + 1 :]]

        defintion_langs = []
        for lang in graph.objects(subject=subject, predicate=AASNameSpace.AAS["DataSpecificationIec61360/definition"]):
            defintion_langs.append(LangStringDefinitionTypeIec61360.from_rdf(graph, lang))

        if len(defintion_langs) == 0:
            defintion_langs = None

        value_format = None
        value_format_ref: rdflib.Literal = next(
            graph.objects(
                subject=subject,
                predicate=AASNameSpace.AAS["DataSpecificationIec61360/valueFormat"],
            ),
            None,
        )
        if value_format_ref:
            value_format = value_format_ref.value
        value = None
        value_ref: rdflib.Literal = next(
            graph.objects(
                subject=subject,
                predicate=AASNameSpace.AAS["DataSpecificationIec61360/value"],
            ),
            None,
        )
        if value_ref:
            value = value_ref.value
        level_type_ref: rdflib.URIRef = next(
            graph.objects(
                subject=subject,
                predicate=AASNameSpace.AAS["DataSpecificationIec61360/levelType"],
            ),
            None,
        )
        level_type = None
        if level_type_ref:
            level_type = LevelType.from_rdf(graph, level_type_ref)
        return DataSpecificationIec61360(
            preferredName=pref_name_langs,
            shortName=short_name_langs,
            unit=unit,
            unitId=unit_id,
            sourceOfDefinition=source_of_def,
            symbol=symbol,
            dataType=deta_type,
            definition=defintion_langs,
            valueFormat=value_format,
            value=value,
            levelType=level_type,
        )
