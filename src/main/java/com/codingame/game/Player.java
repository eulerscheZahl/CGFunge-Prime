package com.codingame.game;
import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.gameengine.core.AbstractSoloPlayer;

public class Player extends AbstractSoloPlayer {
    int expectedOutput = 1;
    @Override
    public int getExpectedOutputLines() {
        return expectedOutput;
    }

    public void setExpectedOutputLines(int value) {
        expectedOutput = value;
    }
}
