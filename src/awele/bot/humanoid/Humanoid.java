package awele.bot.humanoid;

import awele.bot.CompetitorBot;
import awele.core.Board;
import awele.core.InvalidBotException;

public class Humanoid extends CompetitorBot {


    protected static final int BUDGET = (int) 3e6;
    protected static int[] categories;
    protected ExplorationTree explorationTree;
    protected Tools tools;


    //LEARNING VARIABLES
    private static final int MAX_LEARNING_TIME = 1000 * 60 * 55 * 1; // 1 h
    protected static double startLearningTime;
    protected static double endLearningTime;


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
        this.explorationTree.initialValueDelta = 1.15f;
        tools.initCategoriesWithData();


        //TRAINING VS OTHER BOT TO UPGRADE CATEGORIES DURING 30 min (REST OF LEARNING TIME)
        do {
            tools.upgradeCategories(this);
            endLearningTime = System.currentTimeMillis();
        } while (endLearningTime - startLearningTime < MAX_LEARNING_TIME);
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

    }

}
