package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils;

public class Prompts {
    public static String QUESTION = "What is present in the image?";

    public static String IMAGE_PROMPT = QUESTION +
            " Provide the response in a structured format with bullet points. Focus on extracting key details that can be converted into RDF triples. " +
            "When possible, include the name of the items and their counts. Use the format '- ItemName: Number' or '- ItemName: ?' if the count is unknown. " +
            "For other relevant details, use the format '- Description: [Brief description]', '- Context: [Relevant context]', or '- Inference: [Inference about the scene]'. Examples:\n" +
            "- If asked about the number of items: '- Boxes: 10'\n" +
            "- If the number is unknown: '- Items: ?'\n" +
            "- For descriptions: '- Description: A collection of items arranged neatly in rows.'\n" +
            "- For context or inference: '- Context: The scene appears to be in a warehouse with automated sorting.' or '- Inference: The setup suggests a manufacturing process.'";

public static String RDF_PROMPT = "# 1. Overview\n" +
        "You are a top-tier algorithm designed to convert a given structured text into an RDF TTL string. " +
        "The structured text contains data from a perception model that contains information about an image given to it in a concise manner.\n" +
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



}
