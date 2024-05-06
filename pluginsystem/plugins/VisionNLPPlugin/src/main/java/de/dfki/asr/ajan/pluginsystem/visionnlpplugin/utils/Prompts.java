package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils;

public class Prompts {
    public static String IMAGE_PROMPT = "What is present in the image? Extract only the name of the items present and the number of the items in the image alone in a bullet point.";

    public static String RDF_PROMPT = "Convert the given sentence to an RDF and give the RDF alone in TTL format and no additional texts are allowed." +
            " Your response should be able to be added to an rdf4j repository and should only contain the TTL and no additional texts are allowed." +
            "Additional Info - available namespaces are: ajan-ns:<http://www.ajan.de/ajan-ns>. Given Sentence:";
}
