package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils;

public class Prompts {
    public static String QUESTION = "What is present in the image?";

    public static String IMAGE_PROMPT1 = QUESTION +
            " Provide the response in a structured format with bullet points. Focus on extracting key details that can be converted into RDF triples. " +
            "Based on the type of question, When possible, include the name of the items and their counts. Use the format '- ItemName: Number' or '- ItemName: ?' if the count is unknown. " +
            "For other relevant details, use the format '- Description: [Brief description]', '- Context: [Relevant context]', or '- Inference: [Inference about the scene]'. Examples:\n" +
            "- If asked about the number of items: '- Boxes: 10'\n" +
            "- If the number is unknown: '- Items: ?'\n" +
            "- For descriptions: '- Description: A collection of items arranged neatly in rows.'\n" +
            "- For context or inference: '- Context: The scene appears to be in a warehouse with automated sorting.' or '- Inference: The setup suggests a manufacturing process.'";

public static String SPARQL_INSERT_PROMPT =
        "Task: Generate a SPARQL INSERT query to insert data from a natural language text into a graph database according to a given RML mapping.\n" +
        "For instance, to add '- Description: An empty street with 3 people walking.' with the mapping:\n" +
        "\n" +
        "```\n" +
        "@prefix ex: <http://example.org/> .\n" +
        "@prefix schema: <http://schema.org/> .\n" +
        "@prefix rml: <http://semweb.mmlab.be/ns/rml#> .\n" +
        "@prefix rr: <http://www.w3.org/ns/r2rml#> .\n" +
        "@prefix ql: <http://semweb.mmlab.be/ns/ql#> .\n" +
        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
        "\n" +
        "ex:Street\n" +
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
        "        rr:predicate schema:people ;\n" +
        "        rr:objectMap [\n" +
        "        rml:reference \"people\" ;\n" +
        "        ] ;\n" +
        "    ];\n" +
        "    rr:predicateObjectMap [\n" +
        "       rr:predicate schema:count ;\n" +
        "         rr:objectMap [\n" +
        "            rml:reference \"count\" ;\n" +
        "            ] ;\n" +
        "    ] .    \n" +
        "```\n" +
        "\n" +
        "The SPARQL INSERT query would be:\n" +
        "```\n" +
        "PREFIX ex: <http://example.org/>\n" +
        "PREFIX schema: <http://schema.org/>\n" +
        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
        "\n" +
        "INSERT DATA {\n" +
        "    ex:Street ex:description \"An empty street with 3 people walking.\" .\n" +
        "    ex:Street schema:location \"Street\" .\n" +
        "    schema:people [ schema:count \"3\"xsd:integer ] .\n" +
        "}\n" +
        "```\n" +
        "\n" +
        "Instructions:\n" +
        "Use only the provided properties and namespaces present in the mapping.\n" +
        "Do not use any other namespaces or properties that are not provided.\n" +
        "Make the query as short as possible and avoid adding unnecessary triples.\n" +
        "Use only the node types and properties provided in the schema.\n" +
        "Do not use any node types and properties that are not explicitly provided.\n" +
        "Include all necessary prefixes. \n" +
        "The mapping is as follows:\n" +
        "%s\n" +
        "\n" +
        "The Text is as follows:\n" +
        "%s\n" +
        "\n" +
        "Note: Be as concise as possible.\n" +
        "Do not include any explanations or apologies in your responses.\n" +
        "Do not respond to any questions that ask for anything else than for you to construct a SPARQL query.\n" +
        "Return only the generated SPARQL query, nothing else.";

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

private static final String SPARQL_CORRECTION_TASK =
        "Task: Check the correctness of the given SPARQL query and give the corrected SPARQL.\n" +
                "Instructions:\n" +
                "Make the query as short as possible and avoid adding unnecessary triples.\n" +
                "Output the corrected SPARQL query.\n" +
                "If the query is correct, output the same query.\n" +
                "The query is as follows:\n" +
                "%s\n" ;

private static final String SPARQL_CORRECTION_NOTE = "Note: Be as concise as possible.\n" +
                "Do not include any explanations or apologies in your responses.\n" +
                "Do not respond to any questions that ask for anything else than for you to construct a SPARQL query.\n" +
                "Return only the generated SPARQL query, nothing else.";

public static String SPARQL_CORRECTION_PROMPT =
                    SPARQL_CORRECTION_TASK +SPARQL_CORRECTION_NOTE;


public static String SPARQL_CORRECTION_PROMPT_WITH_ERROR =
                    SPARQL_CORRECTION_TASK +
                    "Error: '%s'\n" +
                    SPARQL_CORRECTION_NOTE;

public static String SPARQL_CORRECTION_PROMPT_WITH_ERROR_AND_NAMESPACE =
                    SPARQL_CORRECTION_TASK +
                    "Error: '%s'\n" +
                    "Available Namespaces: '%s'\n" +
                    SPARQL_CORRECTION_NOTE;
}
