package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils;

import de.dfki.asr.ajan.behaviour.AJANLogger;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.IOException;

public class HTTPHelper {
    public static void sendPostRequest(String requestUrl, JSONObject jsonParams, AJANLogger logger, Class<?> thisClass) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader("Content-Type", "application/json");
        HttpEntity postParams = new StringEntity(jsonParams.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(postParams);
        CloseableHttpResponse httpResponse;

        try {
            httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();
            if(statusLine.getStatusCode() >= 300) {
                logger.info(thisClass,"POST Response Status: " + httpResponse.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
