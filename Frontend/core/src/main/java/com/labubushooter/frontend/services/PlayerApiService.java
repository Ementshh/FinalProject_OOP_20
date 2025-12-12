package com.labubushooter.frontend.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class PlayerApiService {
    private static final String BASE_URL = "http://localhost:8080/api/players";
    private final Json json;

    public PlayerApiService() {
        this.json = new Json();
    }

    public interface LoginCallback {
        void onSuccess(PlayerData playerData, boolean isNewPlayer);
        void onFailure(String error);
    }

    public interface SaveCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void login(String username, LoginCallback callback) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder.newRequest()
                .method(Net.HttpMethods.POST)
                .url(BASE_URL + "/login")
                .header("Content-Type", "application/json")
                .content(json.toJson(requestBody))
                .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String responseStr = httpResponse.getResultAsString();
                JsonValue jsonResponse = new com.badlogic.gdx.utils.JsonReader().parse(responseStr);
                
                JsonValue playerJson = jsonResponse.get("player");
                PlayerData playerData = new PlayerData();
                playerData.playerId = playerJson.getString("playerId");
                playerData.username = playerJson.getString("username");
                playerData.totalCoins = playerJson.getInt("totalCoins", 0);
                playerData.lastStage = playerJson.getInt("lastStage", 1);

                boolean isNewPlayer = jsonResponse.getBoolean("isNewPlayer", false);
                String message = jsonResponse.getString("message");

                Gdx.app.log("PlayerAPI", message);
                callback.onSuccess(playerData, isNewPlayer);
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("PlayerAPI", "Login failed: " + t.getMessage());
                callback.onFailure("Failed to connect to server: " + t.getMessage());
            }

            @Override
            public void cancelled() {
                callback.onFailure("Request cancelled");
            }
        });
    }

    public void saveProgress(String playerId, int lastStage, int coinsCollected, SaveCallback callback) {
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("lastStage", lastStage);
        requestBody.put("coinsCollected", coinsCollected);

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder.newRequest()
                .method(Net.HttpMethods.PUT)
                .url(BASE_URL + "/" + playerId + "/progress")
                .header("Content-Type", "application/json")
                .content(json.toJson(requestBody))
                .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                Gdx.app.log("PlayerAPI", "Progress saved successfully");
                callback.onSuccess();
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("PlayerAPI", "Save failed: " + t.getMessage());
                callback.onFailure("Failed to save: " + t.getMessage());
            }

            @Override
            public void cancelled() {
                callback.onFailure("Request cancelled");
            }
        });
    }

    public void resetProgress(String playerId, SaveCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder.newRequest()
                .method(Net.HttpMethods.PUT)
                .url(BASE_URL + "/" + playerId + "/reset")
                .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                Gdx.app.log("PlayerAPI", "Progress reset successfully");
                callback.onSuccess();
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("PlayerAPI", "Reset failed: " + t.getMessage());
                callback.onFailure("Failed to reset: " + t.getMessage());
            }

            @Override
            public void cancelled() {
                callback.onFailure("Request cancelled");
            }
        });
    }

    public static class PlayerData {
        public String playerId;
        public String username;
        public int totalCoins;
        public int lastStage;
    }
}
