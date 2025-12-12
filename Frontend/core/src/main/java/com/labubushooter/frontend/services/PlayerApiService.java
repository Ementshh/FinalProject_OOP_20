package com.labubushooter.frontend.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class PlayerApiService {
    private static final String BASE_URL = "http://localhost:8080/api/players";
    private final JsonReader jsonReader;

    public PlayerApiService() {
        this.jsonReader = new JsonReader();
    }
    
    // Helper method to escape special characters in JSON strings
    private String escapeJson(String str) {
        if (str == null) return "";
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
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
        // Build JSON string manually - libGDX Json.toJson() produces non-standard format
        String jsonBody = "{\"username\":\"" + escapeJson(username) + "\"}";

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder.newRequest()
                .method(Net.HttpMethods.POST)
                .url(BASE_URL + "/login")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(10000)
                .content(jsonBody)
                .build();

        Gdx.app.log("PlayerAPI", "Sending login request for: " + username);
        Gdx.app.log("PlayerAPI", "URL: " + BASE_URL + "/login");
        Gdx.app.log("PlayerAPI", "Body: " + jsonBody);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                final int statusCode = httpResponse.getStatus().getStatusCode();
                final String responseStr = httpResponse.getResultAsString();
                
                Gdx.app.postRunnable(() -> {
                    Gdx.app.log("PlayerAPI", "Response status: " + statusCode);
                    Gdx.app.log("PlayerAPI", "Response body: " + responseStr);

                    // Check for empty response or connection issues
                    if (statusCode == -1 || responseStr == null || responseStr.trim().isEmpty()) {
                        Gdx.app.error("PlayerAPI", "Empty response or connection failed!");
                        callback.onFailure("Backend tidak merespons. Pastikan backend sudah running!");
                        return;
                    }

                    // Check if response is HTML (error page)
                    if (responseStr.trim().startsWith("<") || responseStr.contains("<!DOCTYPE") || responseStr.contains("<html")) {
                        Gdx.app.error("PlayerAPI", "Received HTML instead of JSON!");
                        callback.onFailure("Server error. Check backend logs.");
                        return;
                    }

                    // Check for error status codes
                    if (statusCode >= 400) {
                        Gdx.app.error("PlayerAPI", "HTTP Error: " + statusCode);
                        callback.onFailure("Server error: " + statusCode);
                        return;
                    }

                    try {
                        JsonValue jsonResponse = jsonReader.parse(responseStr);
                        
                        if (jsonResponse == null) {
                            callback.onFailure("Failed to parse JSON response");
                            return;
                        }
                        
                        JsonValue playerJson = jsonResponse.get("player");
                        if (playerJson == null) {
                            callback.onFailure("Invalid response: missing player data");
                            return;
                        }
                        
                        PlayerData playerData = new PlayerData();
                        playerData.playerId = playerJson.getString("playerId");
                        playerData.username = playerJson.getString("username");
                        playerData.totalCoins = playerJson.getInt("totalCoins", 0);
                        playerData.lastStage = playerJson.getInt("lastStage", 1);

                        boolean isNewPlayer = jsonResponse.getBoolean("isNewPlayer", false);
                        String message = jsonResponse.getString("message", "Login successful");

                        Gdx.app.log("PlayerAPI", message);
                        Gdx.app.log("PlayerAPI", "Player ID: " + playerData.playerId);
                        Gdx.app.log("PlayerAPI", "Last Stage: " + playerData.lastStage);
                        Gdx.app.log("PlayerAPI", "Total Coins: " + playerData.totalCoins);
                        
                        callback.onSuccess(playerData, isNewPlayer);
                    } catch (Exception e) {
                        Gdx.app.error("PlayerAPI", "JSON Parse error: " + e.getMessage());
                        Gdx.app.error("PlayerAPI", "Raw response: " + responseStr);
                        callback.onFailure("Failed to parse server response: " + e.getMessage());
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    Gdx.app.error("PlayerAPI", "Login failed: " + t.getMessage());
                    Gdx.app.error("PlayerAPI", "Make sure backend is running on http://localhost:8080");
                    callback.onFailure("Gagal koneksi ke server. Pastikan backend running!");
                });
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> callback.onFailure("Request cancelled"));
            }
        });
    }

    public void saveProgress(String playerId, int lastStage, int coinsCollected, SaveCallback callback) {
        // Build JSON string manually
        String jsonBody = "{\"lastStage\":" + lastStage + ",\"coinsCollected\":" + coinsCollected + "}";

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder.newRequest()
                .method(Net.HttpMethods.PUT)
                .url(BASE_URL + "/" + playerId + "/progress")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(10000)
                .content(jsonBody)
                .build();

        Gdx.app.log("PlayerAPI", "Saving progress - Stage: " + lastStage + ", Coins: " + coinsCollected);
        Gdx.app.log("PlayerAPI", "Body: " + jsonBody);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                final int statusCode = httpResponse.getStatus().getStatusCode();
                Gdx.app.postRunnable(() -> {
                    if (statusCode >= 200 && statusCode < 300) {
                        Gdx.app.log("PlayerAPI", "Progress saved successfully");
                        callback.onSuccess();
                    } else {
                        Gdx.app.error("PlayerAPI", "Save failed with status: " + statusCode);
                        callback.onFailure("Save failed: HTTP " + statusCode);
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    Gdx.app.error("PlayerAPI", "Save failed: " + t.getMessage());
                    callback.onFailure("Failed to save: " + t.getMessage());
                });
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> callback.onFailure("Request cancelled"));
            }
        });
    }

    public void resetProgress(String playerId, SaveCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder.newRequest()
                .method(Net.HttpMethods.PUT)
                .url(BASE_URL + "/" + playerId + "/reset")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(10000)
                .content("{}")
                .build();

        Gdx.app.log("PlayerAPI", "Resetting progress for player: " + playerId);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                final int statusCode = httpResponse.getStatus().getStatusCode();
                Gdx.app.postRunnable(() -> {
                    if (statusCode >= 200 && statusCode < 300) {
                        Gdx.app.log("PlayerAPI", "Progress reset successfully");
                        callback.onSuccess();
                    } else {
                        Gdx.app.error("PlayerAPI", "Reset failed with status: " + statusCode);
                        callback.onFailure("Reset failed: HTTP " + statusCode);
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    Gdx.app.error("PlayerAPI", "Reset failed: " + t.getMessage());
                    callback.onFailure("Failed to reset: " + t.getMessage());
                });
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> callback.onFailure("Request cancelled"));
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
