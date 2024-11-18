package com.aws.demo.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonExtractor {

    public String extractPayload(String jsonString) {
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        JsonArray contentArray = jsonObject.getAsJsonArray("content");

        for (int i = 0; i < contentArray.size(); i++) {
            JsonObject contentObject = contentArray.get(i).getAsJsonObject();
            if (contentObject.get("type").getAsString().equals("text")) {
                String contentText = contentObject.get("text").getAsString();
                return contentText;
            }
        }
        return null;
    }
}
