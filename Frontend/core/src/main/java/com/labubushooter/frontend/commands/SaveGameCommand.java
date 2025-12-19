package com.labubushooter.frontend.commands;

import com.labubushooter.frontend.services.PlayerApiService;
import com.labubushooter.frontend.services.PlayerApiService.PlayerData;
import com.badlogic.gdx.Gdx;

public class SaveGameCommand implements GameCommand {
    private PlayerApiService playerApi;
    private PlayerData playerData;
    private int currentLevel;
    private int coinsCollected;

    public SaveGameCommand(PlayerApiService playerApi, PlayerData playerData,
                           int currentLevel, int coinsCollected) {
        this.playerApi = playerApi;
        this.playerData = playerData;
        this.currentLevel = currentLevel;
        this.coinsCollected = coinsCollected;
    }

    @Override
    public void execute() {
        playerApi.saveProgress(
                playerData.playerId,
                currentLevel,
                coinsCollected,
                new PlayerApiService.SaveCallback() {
                    @Override
                    public void onSuccess() {
                        Gdx.app.log("SaveGame", "Progress saved successfully");
                    }

                    @Override
                    public void onFailure(String error) {
                        Gdx.app.error("SaveGame", "Failed to save: " + error);
                    }
                }
        );
    }

    @Override
    public void undo() {
        // Not applicable for save operation
    }
}