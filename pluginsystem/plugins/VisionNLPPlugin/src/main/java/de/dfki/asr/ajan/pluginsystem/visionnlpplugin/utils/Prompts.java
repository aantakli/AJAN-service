package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils;

public class Prompts {
    public static String QUESTION = "What is present in the image?";

    public static String IMAGE_PROMPT =
        "Task: Generate a structured text as response to a given question.Focus on extracting key details and be precise while answering. \n" +
                "The category of question can be about the number of items, descriptions, context, or inferences. \n" +
                "Based on the category, provide the response in the following format:\n" +
                "- For the number of items: '- ItemName: Number'\n" +
                "- If the number is unknown: '- ItemName: Unknown'\n" +
                "- For descriptions: '- Description: [Brief description]'\n" +
                "- For context or inference: '- Context: [Relevant context]' or '- Inference: [Inference about the scene]'\n" +
                "Examples:\n" +
                "- If asked about the number of items: '- Boxes: 10'\n" +
                "- If the number is unknown: '- Boxes: Unknown'\n" +
                "- For descriptions: '- Description: A collection of items arranged neatly in rows.'\n" +
                "- For context or inference: '- Context: The scene appears to be in a warehouse with automated sorting.' or '- Inference: The setup suggests a manufacturing process.' \n" +
                "Note: Be as concise as possible.Do not include any explanations or apologies in your responses. \n" +
                "Return only the generated structured text, nothing else.\n" +
                "Given the image, answer the following question: '%s'";


    public static String JSON_PROMPT =
        "Task: Generate a JSON-LD representation of the given text according to the mapping.\n" +
                "\n" +
                "Instructions:\n" +
                "Use the provided prefixes and namespaces.\n" +
                "Include all necessary prefixes.\n" +
                "Make the json as short as possible and avoid adding unnecessary entries.\n" +
                "Do not include any explanations or apologies in your responses.\n" +
                "Return only the generated JSON-LD representation, nothing else.\n" +
                "\n" +
                "For instance, given the text: 'An empty street with 3 people walking.' and the mapping:\n" +
                "    \n" +
                "```\n" +
                "@prefix ex: <http://example.org/> .\n" +
                "@prefix schema: <http://schema.org/> .\n" +
                "@prefix rml: <http://semweb.mmlab.be/ns/rml#> .\n" +
                "@prefix rr: <http://www.w3.org/ns/r2rml#> .\n" +
                "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n" +
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
                "@prefix domain: <http://localhost:8090/rdf4j/repositories/domain#> .\n" +
                "\n" +
                "domain:PeopleCountMapping {\n" +
                "    domain:PeopleCountMapping a rr:TriplesMap ;\n" +
                "    rml:logicalSource [\n" +
                "        rml:source [\n" +
                "            a carml:Stream ;\n" +
                "        ] ;\n" +
                "        rml:referenceFormulation ql:JSONPath ;\n" +
                "        rml:iterator \"$\"\n" +
                "    ] ;\n" +
                "    rr:subjectMap [\n" +
                "        rr:template \"http://example.org/street\" ;\n" +
                "        rr:class ex:Street ;\n" +
                "    ] ;\n" +
                "    rr:predicateObjectMap [\n" +
                "        rr:predicate schema:description ;\n" +
                "        rr:objectMap [\n" +
                "        rml:reference \"description\" ;\n" +
                "        ] ;\n" +
                "    ] ;\n" +
                "    rr:predicateObjectMap [\n" +
                "        rr:predicate schema:location ;\n" +
                "        rr:objectMap [\n" +
                "        rml:reference \"location\" ;\n" +
                "        ] ;\n" +
                "    ] ;\n" +
                "    rr:predicateObjectMap [\n" +
                "       rr:predicate schema:count ;\n" +
                "         rr:objectMap [\n" +
                "            rml:reference \"entity.count\" ;\n" +
                "            ] ;\n" +
                "    ] .   \n" +
                "}\n" +
                "```\n" +
                "The JSON representation would be:\n" +
                "```\n" +
                "{\n" +
                "    \"description\": \"An empty street with 3 people walking.\",\n" +
                "    \"location\": \"Street\",\n" +
                "    \"entity\": {\n" +
                "        \"type\": \"People\",\n" +
                "        \"count\": 3\n" +
                "    }\n" +
                "}\n" +
                "```" +
                "\n"+
                "The RML mapping is provided as follows:\n" +
                "%s\n" +
                "\n" +
                "The Text is as follows:\n" +
                "%s\n" +
                "\n" +
                "Note: Be as concise as possible.\n" +
                "Do not include any explanations or apologies in your responses.\n" +
                "Do not respond to any questions that ask for anything else than for you to construct a JSON-LD.\n" +
                "Return only the generated JSON-LD, nothing else.";
}
