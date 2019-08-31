package CGFunge;

import com.codingame.gameengine.module.entities.*;

import java.util.ArrayList;
import java.util.Stack;

public class Interpreter {
    private final int[] dx = {1, 0, -1, 0};
    private final int[] dy = {0, 1, 0, -1};
    private char[][] grid;
    private int width;
    private int height;

    private Stack<Integer> stack = new Stack<>();
    private int x = 0;
    private int y = 0;
    private int dir = 0;
    private boolean finished = false;
    private boolean quoted = false;
    private boolean skip = false;
    private String output = "";

    private Text outputText;
    private Stack<Text> textStack = new Stack<>();
    private Circle programPointer;
    GraphicEntityModule graphics;

    public Interpreter(String input) {
        stack.push(Integer.parseInt(input));
    }

    public void setCode(ArrayList<String> code) {
        width = 0;
        height = code.size();
        for (int i = 0; i < height; i++) {
            width = Math.max(width, code.get(i).length());
        }

        grid = new char[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[x][y] = ' ';
                if (x < code.get(y).length()) grid[x][y] = code.get(y).charAt(x);
            }
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    private final int GRID_SIZE = 50;

    public void initView(GraphicEntityModule graphicEntityModule) {
        this.graphics = graphicEntityModule;
        graphicEntityModule.createSprite().setImage("background.png");

        programPointer = graphicEntityModule.createCircle().setRadius(GRID_SIZE / 2);
        programPointer.setFillColor(0x00FF00);
        programPointer.setAlpha(0.2).setZIndex(2);
        placeInCodeGroup(programPointer, GRID_SIZE / 2, GRID_SIZE / 2);

        outputText = graphicEntityModule.createText("").setX(340).setY(945).setFillColor(0xFF0000).setFontSize(50);

        createStackText(stack.peek());
    }

    private void placeInCodeGroup(Entity entity, double gridX, double gridY) {
        double scale = Math.min(1600.0 / width, 750.0 / height) / GRID_SIZE;
        int x = (int) (280 + scale * gridX);
        int y = (int) (90 + scale * gridY);
        if (entity.getX() != x) entity.setX(x);
        if (entity.getY() != y) entity.setY(y);
        entity.setScale(scale);
    }

    private int drawnLines = 0;

    public void buildView(GraphicEntityModule graphicEntityModule) {
        if (drawnLines >= height) return;
        int y = drawnLines;
        drawnLines++;
        for (int x = 0; x < width; x++) {
            Sprite sprite = graphicEntityModule.createSprite().setImage("tile.png");
            placeInCodeGroup(sprite, x * GRID_SIZE, y * GRID_SIZE);
            if (grid[x][y] != ' ') {
                Text text = graphicEntityModule.createText(String.valueOf(grid[x][y])).setFontFamily("Nimbus Mono L").setFontWeight(Text.FontWeight.BOLD);
                placeInCodeGroup(text, x * GRID_SIZE + 15, y * GRID_SIZE + 7);
                text.setFillColor(0xFF0000);
                text.setFontSize(30);
            }
        }
       graphics.commitWorldState(0);
    }

    public boolean isFinished() {
        return finished;
    }

    public String getOutput() {
        return output;
    }

    public boolean outOfRange() {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    private void movePointer() {
        x += dx[dir];
        y += dy[dir];
        placeInCodeGroup(programPointer, x * GRID_SIZE + GRID_SIZE / 2, y * GRID_SIZE + GRID_SIZE / 2);
    }

    public void step() {
        handleAction();
        movePointer();
    }

    private void setStackText(Text text, int value) {
        String content = String.valueOf(value);
        if (value >= 32 && value < 128) content += " ('" + (char) value + "')";
        text.setText(content);
    }

    private Stack<Text> cache = new Stack<>();
    private void createStackText(int value) {
        String content = String.valueOf(value);
        if (value >= 32 && value < 128) content += " ('" + (char) value + "')";

        Text text = null;
        if (cache.size() == 0) {
            text = graphics.createText(content).setFillColor(0xFF0000).setFontSize(30).setX(80);
            text.setY(950 - 70 * textStack.size());
        }
        else {
            text = cache.pop();
            text.setText(content);
            text.setY(950 - 70 * textStack.size());
            graphics.commitEntityState(0, text);
            text.setAlpha(1);
        }
        textStack.push(text);
    }

    private Text popStack() {
        Text text = textStack.pop();
        cache.push(text);
        return text;
    }

    public void handleAction() {
        if (skip) {
            skip = false;
            return;
        }

        char c = grid[x][y];
        if (c == '"') {
            quoted = !quoted;
            return;
        }
        if (quoted) {
            stack.push((int) c);
            createStackText(stack.peek());
            return;
        }

        if (c == '+') {
            stack.push(stack.pop() + stack.pop());
            popStack().setY(textStack.peek().getY()).setAlpha(0);
            setStackText(textStack.peek(), stack.peek());
        } else if (c == '-') {
            stack.push(-stack.pop() + stack.pop());
            popStack().setY(textStack.peek().getY()).setAlpha(0);
            setStackText(textStack.peek(), stack.peek());
        } else if (c == '*') {
            stack.push(stack.pop() * stack.pop());
            popStack().setY(textStack.peek().getY()).setAlpha(0);
            setStackText(textStack.peek(), stack.peek());
        } else if (c == '/') {
            int v1 = stack.pop();
            int v2 = stack.pop();
            stack.push(v2 / v1);
            popStack().setY(textStack.peek().getY()).setAlpha(0);
            setStackText(textStack.peek(), stack.peek());
        } else if (c >= '0' && c <= '9') {
            stack.push(c - '0');
            createStackText(stack.peek());
        } else if (c == 'I') {
            output += stack.pop();
            popStack().setAlpha(0);
            outputText.setText(output);
        } else if (c == 'C') {
            output += (char) ((int) stack.pop());
            popStack().setAlpha(0);
            outputText.setText(output);
        } else if (c == '>')
            dir = 0;
        else if (c == 'v')
            dir = 1;
        else if (c == '<')
            dir = 2;
        else if (c == '^')
            dir = 3;
        else if (c == ':') {
            int v = stack.pop();
            popStack().setAlpha(0);
            if (v < 0) dir = (dir + 3) % 4;
            if (v > 0) dir = (dir + 1) % 4;
        } else if (c == 'P') {
            stack.pop();
            popStack().setAlpha(0);
        } else if (c == 'D') {
            stack.push(stack.peek());
            createStackText(stack.peek());
            textStack.peek().setY(textStack.peek().getY() + 70);
            graphics.commitEntityState(0, textStack.peek());
            textStack.peek().setY(textStack.peek().getY() - 70);
        } else if (c == 'X') {
            int index = stack.pop();
            textStack.pop().setAlpha(0);
            int value = stack.get(stack.size() - 1 - index);
            stack.remove(stack.size() - 1 - index);
            stack.push(value);

            Text text = textStack.get(textStack.size() - 1 - index);
            textStack.remove(textStack.size() - 1 - index);
            textStack.push(text);
            for (int i = 0; i < textStack.size(); i++) {
                int y = 950 - 70 * i;
                if (textStack.get(i).getY() != y) textStack.get(i).setY(y);
            }
        } else if (c == 'S')
            skip = true;
        else if (c == 'E')
            finished = true;
    }
}
