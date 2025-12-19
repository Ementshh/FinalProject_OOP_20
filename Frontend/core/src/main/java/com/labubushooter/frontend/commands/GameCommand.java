package com.labubushooter.frontend.commands;

public interface GameCommand {
    void execute();
    void undo();
}