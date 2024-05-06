package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.util.HashMap;

public class LanguageModel {

    static HashMap<String, ChatLanguageModel> chatLLMModel = new HashMap<>();

    public static ChatLanguageModel getChatLLMModel(String agentName){
        return chatLLMModel.get(agentName);
    }

    public static void setChatLLMModel(String agentName, ChatLanguageModel chatLLMModel){
        LanguageModel.chatLLMModel.put(agentName,chatLLMModel);
    }

    public static void initChatModel(String agentName, String modelName, String baseUrl){
        setChatLLMModel(agentName, OllamaChatModel.builder()
                .modelName(modelName)
                .baseUrl(baseUrl)
                .build());
    }

}
