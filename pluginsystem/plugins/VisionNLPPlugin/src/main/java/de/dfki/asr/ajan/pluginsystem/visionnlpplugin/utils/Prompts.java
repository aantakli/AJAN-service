package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils;

public class Prompts {
    public static String IMAGE_PROMPT = "What is present in the image? Extract only the name of the items present and the number of the items in the image alone in a bullet point. If you cannot figure out, use some generic names.";

    public static String RDF_PROMPT = "# 1. Overview\n" +
            "You are a top-tier algorithm designed to convert a given sentence properly into an RDF TTL string." +
            "The sentence contains data from a perception model that contains information about an image given to it in a concise manner.\n" +
            "# 2. Output format\n" +
            " Give only the RDF in TTL format and no additional texts are allowed.\n" +
            "# 3. Consistency \n" +
            " Your response should be able to be added to an rdf4j repository directly and should only contain the RDF in TTL format with proper prefixes." +
            " No additional texts are allowed here. The response string should be a complete TTL string representing the RDF graph.\n" +
            "# 4. Namespaces \n" +
            "Available RDF TTL namespace prefix to use are: \n" +
            "- `ajan-ns:<http://www.ajan.de/ajan-ns#>` \n" +
            "- `vision-nlp-ns:<http://www.ajan.de/behavior/vision-nlp-ns#>` \n" +
            "- `rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>` \n" +
            "- `rdfs:<http://www.w3.org/2000/01/rdf-schema#>` \n" +
            "- `xsd:<http://www.w3.org/2001/XMLSchema#>` \n" +
            "where, for example, `ajan-ns` is the prefix and `<http://www.ajan.de/ajan-ns#>` is the url for it." +
            "\n\n" +
            "Given Sentence: `%s`";
}
