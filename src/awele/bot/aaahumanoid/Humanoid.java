package awele.bot.aaahumanoid;

import awele.bot.CompetitorBot;
import awele.core.Board;
import awele.core.InvalidBotException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Humanoid extends CompetitorBot {


    protected static final int BUDGET = (int) 3e6;
    protected static int[] categories;
    protected ExplorationTree explorationTree;
    protected Tools tools;


    //LEARNING VARIABLES
    private static final int MAX_LEARNING_TIME = 1000 * 60 * 59 * 1; // 1 h
 //   private static final int MAX_LEARNING_TIME = 1000 * 60 * 10 * 1; // 1 h
  //  private static final int MAX_LEARNING_TIME = 0; // 1 h
    protected static double startLearningTime;
    protected static double endLearningTime;

    //WEIGHT FEATURES VARIABLES
    public int[] weightBase;
    public int[] bestWeight;


    //DECISION TIME VARIABLES
    protected int numberMoves;
    protected double totalDecisionTime;
    protected double startDecisionTimer;
    protected double endDecisionTimer;


    public Humanoid() throws InvalidBotException {
        this.setBotName("Humanoïd, the mad lion !");
        this.addAuthor("Maxence Schoirfer");
        this.addAuthor("Alexandre Kroher");
        this.explorationTree = new ExplorationTree();
        this.tools = new Tools();
        categories = new int[14200];
    }


    //FUNCTION CALLED DURING 1 HOUR
    @Override
    public void learn() {
        startLearningTime = System.currentTimeMillis();

        //SEARCH BETTER WEIGHT FEATURES DURING 30 min
        this.explorationTree.weightFeatures = tools.getBetterWeightFeatures(MAX_LEARNING_TIME/2);
     //   this.explorationTree.weightFeatures = tools.getBetterWeightFeatures(0);
        this.explorationTree.initialValueDelta = 1.15f;

        System.out.println("Start initialization with awele.data");
        tools.initCategoriesWithData();


        //TRAINING VS OTHER BOT TO UPGRADE CATEGORIES DURING 30 min (REST OF LEARNING TIME)
        do {
            System.out.println("Start upgrade categories");
            tools.upgradeCategories(this);
            System.out.println("End upgrade categories");
            endLearningTime = System.currentTimeMillis();
        } while (endLearningTime - startLearningTime < MAX_LEARNING_TIME);
        saveCategories();
    }


    @Override
    public void initialize() {
        numberMoves = 0;
        totalDecisionTime = 0;
        explorationTree.detla = explorationTree.initialValueDelta;
    }

    @Override
    public double[] getDecision(Board board) {
        startDecisionTimer = System.currentTimeMillis();
        MinMaxNode root = new MaxNode(board);
        root.remainingBudget = BUDGET;
        MinMaxNode.player = board.getCurrentPlayer();
        explorationTree.exploreNode(root, 0, -Double.MAX_VALUE, Double.MAX_VALUE);
        endDecisionTimer = System.currentTimeMillis();

        double runningTime = endDecisionTimer - startDecisionTimer;
        totalDecisionTime += runningTime;
        numberMoves++;
        double averageRunningTime = totalDecisionTime / numberMoves;
        if (averageRunningTime < 95 && runningTime < 120 && explorationTree.detla < 1.75) explorationTree.detla *= 1.1;
        else if (averageRunningTime > 120 && runningTime > 80 && explorationTree.detla > 0.5)
            explorationTree.detla *= 0.9;
        return root.getDecision();
    }

    @Override
    public void finish() {
        System.out.println("");

    }

    private void saveCategories() {
        List<String> categoriesStrings = new ArrayList<>();
        for (int i = 0; i < categories.length; i++) categoriesStrings.add(i + "/" + categories[i]);
        if (categoriesStrings.size() > 0) writeFile("data.txt", categoriesStrings);
    }

    private static void writeFile(String name, List<String> lines) {
        try {
            Writer out = new PrintWriter(new BufferedWriter(new FileWriter(name)));
            for (int i = 0; i < lines.size() - 1; i++) {
                out.write(lines.get(i) + "\n");
            }
            out.write(lines.get(lines.size() - 1));
            out.close();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

}
