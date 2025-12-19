package com.labubushooter.frontend.commands;

import com.badlogic.gdx.utils.Array;

public class CommandInvoker {
    private Array<GameCommand> commandHistory;
    private static CommandInvoker instance;

    private CommandInvoker() {
        commandHistory = new Array<>();
    }

    public static CommandInvoker getInstance() {
        if (instance == null) {
            instance = new CommandInvoker();
        }
        return instance;
    }

    public void executeCommand(GameCommand command) {
        command.execute();
        commandHistory.add(command);
    }

    public void undoLastCommand() {
        if (commandHistory.size > 0) {
            GameCommand lastCommand = commandHistory.pop();
            lastCommand.undo();
        }
    }

    public void clearHistory() {
        commandHistory.clear();
    }
}