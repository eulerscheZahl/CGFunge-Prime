package com.codingame.game;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

import CGFunge.Interpreter;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

public class Referee extends AbstractReferee {
    @Inject
    private SoloGameManager<Player> gameManager;
    @Inject
    private GraphicEntityModule graphicEntityModule;

    final int MAX_HEIGHT = 30;
    final int MAX_WIDTH = 40;

    private Interpreter interpreter;
    private String expectedOutput;

    private boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    @Override
    public void init() {
        String input = gameManager.getTestCaseInput().get(0);
        interpreter = new Interpreter(input);
        interpreter.initView(graphicEntityModule);
        gameManager.setMaxTurns(3000);

        int value = Integer.parseInt(input);
        expectedOutput = isPrime(value) ? "PRIME" : "NOT PRIME";
    }

    @Override
    public void gameTurn(int turn) {
        if (turn == 1) {
            gameManager.getPlayer().execute();
            int lines = 0;
            try {
                lines = Integer.parseInt(gameManager.getPlayer().getOutputs().get(0));
                gameManager.getPlayer().setExpectedOutputLines(lines);
            } catch (Exception e) {
                gameManager.loseGame("No number of lines provided");
            }
            ArrayList<String> code = new ArrayList<>();
            try {
                gameManager.getPlayer().execute();
                for (int i = 0; i < lines; i++) {
                    code.add(gameManager.getPlayer().getOutputs().get(i));
                    if (i == MAX_HEIGHT) gameManager.loseGame("Maximum line count exceeded");
                    if (code.get(i).length() > MAX_WIDTH) gameManager.loseGame("Maximum line width exceeded");
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                gameManager.loseGame("No code provided");
            } catch (TimeoutException e) {
                gameManager.loseGame("Player timed out");
            }

            interpreter.setCode(code);
        } else {
            interpreter.buildView(graphicEntityModule);
            try {
                interpreter.step();
            } catch (ArrayIndexOutOfBoundsException ex) {
                gameManager.loseGame("Stack underflow");
            } catch(ArithmeticException ex) {
                gameManager.loseGame("Division by 0");
            } catch (EmptyStackException ex) {
                gameManager.loseGame("The stack is empty");
            } catch (Exception ex) {
                gameManager.loseGame("You fool!");
            }
            if (interpreter.outOfRange())
                gameManager.loseGame("Program pointer out of code area at (" + interpreter.getX() + "/" + interpreter.getY() + ")");
            if (interpreter.isFinished()) {
                if (interpreter.getOutput().equals(expectedOutput)) {
                    gameManager.putMetadata("Points", String.valueOf(turn - 1));
                    gameManager.winGame(expectedOutput + " is correct");
                }
                else gameManager.loseGame("Output was: " + interpreter.getOutput() + "; expected: " + expectedOutput);
            }
        }
    }
}
