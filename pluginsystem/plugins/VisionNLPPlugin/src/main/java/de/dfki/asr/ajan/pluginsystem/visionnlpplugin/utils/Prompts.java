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

public static String RDF_PROMPT = "# 1. Overview\n" +
        "You are a top-tier algorithm designed to convert a given structured text into an RDF TTL string. " +
        "The structured text contains information about an image given by a image-to-text model.\n" +
        "# 2. Output format\n" +
    "Give only the RDF in TTL format and no additional texts or quotes are allowed. Do not trim the output for brevity; include all necessary details.\n" +
    "Output only the RDF TTL string. No additional explanations, steps, descriptions, or formatting such as quotes or backticks are allowed. Only the RDF TTL string is required.\n" +
        "# 3. Consistency and Syntax\n" +
        "Your response should be able to be added to an RDF4J repository directly and should only contain the RDF in TTL format with proper prefixes as specified by the provided mapping. " +
        "Ensure that your response is syntactically correct and follows proper RDF TTL structure. Use blank nodes correctly and ensure all prefixes are used without typos. Avoid redundant definitions and maintain consistency in entity names. " +
        "Nested properties should be handled correctly, ensuring that all entities are properly defined and linked. " +
    "Avoid using nested blank nodes for properties like `ex:hasColor` or `ex:contains`. Instead, define these properties separately and link them appropriately.\n" +
    "# 4. Mapping\n" +
    "Here is the RML mapping string that specifies how to extract and structure the data. Use only the namespaces and URIs provided in this mapping:\n" +
    "%s\n" +  // Placeholder for the mapping variable
    "# 5. Structured Text\n" +
        "Given Structured Text: \"%s\"";

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
                "Given the image, answer the following question: '" + QUESTION + "'";

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
