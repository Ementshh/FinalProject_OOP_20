package com.labubushooter.frontend.commands;

import com.labubushooter.frontend.Main;

public class LoadLevelCommand implements GameCommand {
    private Main game;
    private int levelToLoad;
    private int previousLevel;

    public LoadLevelCommand(Main game, int levelToLoad) {
        this.game = game;
        this.levelToLoad = levelToLoad;
    }

    @Override
    public void execute() {
        previousLevel = game.getCurrentLevel();
        game.loadLevel(levelToLoad);
    }

    @Override
    public void undo() {
        game.loadLevel(previousLevel);
    }
}