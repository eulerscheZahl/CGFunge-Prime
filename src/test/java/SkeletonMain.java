import com.codingame.gameengine.runner.MultiplayerGameRunner;
import com.codingame.gameengine.runner.SoloGameRunner;

public class SkeletonMain {
    public static void main(String[] args) {
        SoloGameRunner gameRunner = new SoloGameRunner();
        gameRunner.setAgent(Agent1.class);
        gameRunner.setTestCase("test5.json");
        gameRunner.start();
    }
}
