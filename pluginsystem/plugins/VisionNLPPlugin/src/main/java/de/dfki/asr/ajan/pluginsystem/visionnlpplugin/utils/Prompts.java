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
        "For instance, to add '- Description: An empty street with 3 people walking.' with the RML mapping:\n" +
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
        "    ] .\n" +
        "```\n" +
        "\n" +
        "The SPARQL INSERT query would be:\n" +
        "```\n" +
        "PREFIX ex: <http://example.org/>\n" +
        "PREFIX schema: <http://schema.org/>\n" +
        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
        "\n" +
        "INSERT DATA {\n" +
    "    ex:Street schema:description \"An empty street with 3 people walking.\"^^xsd:string .\n" +
    "    ex:Street schema:location \"Street\"^^xsd:string .\n" +
    "    ex:Street schema:people [ schema:count \"3\"^^xsd:integer ] .\n" +
        "}\n" +
        "```\n" +
        "\n" +
        "Instructions:\n" +
        "- Use only the provided properties and namespaces present in the mapping.\n" +
        "- Do not use any other namespaces or properties that are not explicitly provided in the mapping.\n" +
        "- Make the query as short as possible and avoid adding unnecessary triples.\n" +
        "- Include all necessary prefixes.\n" +
    "- Ensure correct use of provided values, namespaces, and datatypes.\n" +
    "- Use the subject template from the mapping to generate the subject URI correctly.\n" +
        "The RML mapping is provided as follows:\n" +
        "%s\n" +
        "\n" +
        "The Text is as follows:\n" +
        "%s\n" +
        "\n" +
        "Note: Be as concise as possible.\n" +
        "All the triples should be valid and consistent with the provided mapping.\n" +
        "Do not create new namespaces or prefixes.\n" +
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
                "Task: Check the correctness of the given SPARQL INSERT query and provide the corrected query.\n" +
//                "Consider the following history of previous interactions for context:\n"+
//                "%s"+
                "Instructions:\n" +
                "- Make the query as short as possible, avoiding unnecessary triples\n" +
                "- Check for potential issues, such as but not limited to:\n" +
                        "  - Syntax errors (e.g., using ';' instead of '.' at end of the triple entry)\n" +
                        "  - Misuse or inconsistency in namespaces\n" +
                        "  - Adding triples that are not valid within the given context\n" +
                        "  - Incorrect use or scope of variables\n" +
                        "  - Improper use of xsd datatypes (use `\"0\"` for unknown values when the object datatype is xsd:integer)\n" +
                        "  - Unintended modifications to the query\n" +
                        "  - Mismanagement of prefixes\n" +
                "- Ensure the query is syntactically correct for execution.\n" +
                "- Ensure all triples are valid and consistent.\n" +
                "- Use only the provided namespaces and prefixes; do not create new ones.\n" +
                "- Correctly use provided values, ensuring correct datatype usage.\n" +
                "- If the query is correct, output the same query.\n" +
                "- If corrections are needed, provide the corrected query.\n" +
                "- Do not include explanations or apologies.\n" +
                "- Respond only with the SPARQL query.\n"+
                "The query is as follows:\n" +
                "%s\n" ;

private static final String SPARQL_CORRECTION_NOTE = "Note: " +
        "- Be as concise as possible.\n" +
        "- Do not include any explanations or apologies in your responses.\n" +
        "- Do not respond to any questions that ask for anything else than for you to construct a SPARQL INSERT query.\n" +
        "- Respond only with the SPARQL query.\n";

public static String SPARQL_CORRECTION_PROMPT =
                    SPARQL_CORRECTION_TASK +SPARQL_CORRECTION_NOTE;


public static String SPARQL_CORRECTION_PROMPT_WITH_ERROR =
                    SPARQL_CORRECTION_TASK +
                    "Error: %s\n" +
                    SPARQL_CORRECTION_NOTE;

public static String SPARQL_CORRECTION_PROMPT_WITH_ERROR_AND_NAMESPACE =
                    SPARQL_CORRECTION_TASK +
                    "Error: %s\n" +
                    "Available Namespaces:\n" +
                    "%s\n" +
                    SPARQL_CORRECTION_NOTE;
}
