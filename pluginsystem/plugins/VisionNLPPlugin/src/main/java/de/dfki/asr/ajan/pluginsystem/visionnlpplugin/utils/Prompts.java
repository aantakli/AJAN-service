package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils;

public class Prompts {
    public static String QUESTION = "What is present in the image?";

    public static String IMAGE_PROMPT = QUESTION +
            " Provide the response in a structured format with bullet points. " +
            "When possible, include the name of the items and their counts. Use the format '- ItemName: Number' or '- ItemName: ?' if the count is unknown. " +
            "For other types of questions, provide concise and relevant information. Examples:\n" +
            "- If asked about the number of items: '- Boxes: 10'\n" +
            "- If the number is unknown: '- Items: ?'\n" +
            "- For descriptions: '- Description: [Brief description]'\n" +
            "- For context or inference: '- Context: [Relevant context or inference]'.";

    public static String RDF_PROMPT = "# 1. Overview\n" +
            "You are a top-tier algorithm designed to convert a given structured text into an RDF TTL string." +
            " The structured text contains data from a perception model that contains information about an image given to it in a concise manner.\n" +
            "# 2. Output format\n" +
            "Give only the RDF in TTL format and no additional texts are allowed.\n" +
            "# 3. Consistency\n" +
            "Your response should be able to be added to an rdf4j repository directly and should only contain the RDF in TTL format with proper prefixes." +
            " No additional texts are allowed here. The response string should be a complete TTL string representing the RDF graph.\n" +
            "# 4. Namespaces\n" +
            "Available RDF TTL namespace prefixes to use are:\n" +
            "- `ajan-ns:<http://www.ajan.de/ajan-ns#>`\n" +
            "- `vision-nlp-ns:<http://www.ajan.de/behavior/vision-nlp-ns#>`\n" +
            "- `rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>`\n" +
            "- `rdfs:<http://www.w3.org/2000/01/rdf-schema#>`\n" +
            "- `xsd:<http://www.w3.org/2001/XMLSchema#>`\n" +
            "where, for example, `ajan-ns` is the prefix and `<http://www.ajan.de/ajan-ns#>` is the URL for it.\n" +
            "# 5. Examples\n" +
            "Given Structured Text: \"- Boxes: 10\"\n" +
            "@prefix ajan-ns: <http://www.ajan.de/ajan-ns#> .\n" +
            "@prefix vision-nlp-ns: <http://www.ajan.de/behavior/vision-nlp-ns#> .\n" +
            "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
            "ajan-ns:ItemCollection1 rdf:type ajan-ns:ItemCollection ;\n" +
            "  rdfs:label \"Box Collection\" ;\n" +
            "  vision-nlp-ns:contains ajan-ns:Box ;\n" +
            "  vision-nlp-ns:itemCount \"10\"^^xsd:integer .\n" +
            "ajan-ns:Box rdf:type ajan-ns:Box ;\n" +
            "  rdfs:label \"Box\" .\n" +
            "Given Structured Text: \"- Items: ? A collection of items that resemble boxes, arranged in a way that suggests an automated sorting or processing system.\"\n" +
            "@prefix ajan-ns: <http://www.ajan.de/ajan-ns#> .\n" +
            "@prefix vision-nlp-ns: <http://www.ajan.de/behavior/vision-nlp-ns#> .\n" +
            "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
            "ajan-ns:ItemCollection2 rdf:type ajan-ns:ItemCollection ;\n" +
            "  rdfs:label \"Unspecified Items Collection\" ;\n" +
            "  vision-nlp-ns:description \"A collection of items that resemble boxes, arranged in a way that suggests an automated sorting or processing system.\" .\n" +
            "Given Structured Text: \"- Context: The scene appears to be in a warehouse with automated sorting.\"\n" +
            "@prefix ajan-ns: <http://www.ajan.de/ajan-ns#> .\n" +
            "@prefix vision-nlp-ns: <http://www.ajan.de/behavior/vision-nlp-ns#> .\n" +
            "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
            "ajan-ns:Scene rdf:type ajan-ns:Context ;\n" +
            "  rdfs:label \"Warehouse Scene\" ;\n" +
            "  vision-nlp-ns:description \"The scene appears to be in a warehouse with automated sorting.\" .\n" +
            "# 6. Validation\n" +
            "Ensure the output is a valid TTL string and can be parsed by standard RDF parsers without errors.\n" +
            "# 7. Structured Text\n" +
            "Given Structured Text: \"%s\"";
}
