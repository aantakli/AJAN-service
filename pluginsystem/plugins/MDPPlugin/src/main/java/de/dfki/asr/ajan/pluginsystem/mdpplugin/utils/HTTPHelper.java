package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils;

import de.dfki.asr.ajan.behaviour.AJANLogger;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class HTTPHelper {
    public static <T> T sendPostRequest(String requestUrl, JSONObject jsonParams, AJANLogger logger, Class<?> thisClass, Class<T> returnJSON) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.setHeader("Content-Type", "application/json");
        HttpEntity postParams = new StringEntity(jsonParams.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(postParams);
        CloseableHttpResponse httpResponse;

        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            httpResponse = httpClient.execute(httpPost);
            StatusLine statusLine = httpResponse.getStatusLine();
            if(statusLine.getStatusCode() >= 300) {
                logger.info(thisClass,"POST Response Status: " + httpResponse.getStatusLine().getStatusCode());
            }
            if(returnJSON == JSONObject.class) {
                String responseBody = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                JSONObject returnValue = new JSONObject(responseBody);
                returnValue.put("statusCode",statusLine.getStatusCode());
                return returnJSON.cast(returnValue);
            } else if (returnJSON == ArrayList.class) {
                String responseBody = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                ArrayList<String> returnValue = new ArrayList<>();
                returnValue.add(String.valueOf(statusLine.getStatusCode()));
                returnValue.add(responseBody);
                return returnJSON.cast(returnValue);
            } else if (returnJSON == Boolean.class) {
                return returnJSON.cast(statusLine.getStatusCode() >= 300);
            }
            return returnJSON.cast(statusLine.getStatusCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
