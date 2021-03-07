package awele.bot.aaTraining.h1;

import awele.bot.Bot;
import awele.bot.CompetitorBot;
import awele.bot.aaTraining.h1.minmax9.MinMaxBot;
import awele.bot.aaTraining.h1.random.RandomBot;
import awele.core.Awele;
import awele.core.Board;
import awele.core.InvalidBotException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinMaxBot1 extends CompetitorBot {

    protected static final boolean DEBUG = false;
    protected static final boolean LEARN = true;
    protected static final int BUDGET = (int) 3e6;
    protected static int[] categories;

    //private static final int MAX_LEARNING_TIME = 1000 * 60 * 60 * 1; // 1 h
    private static final int MAX_LEARNING_TIME = 1000 * 60 * 20 * 1; // 1 h
    protected static double startLearningTime;
    protected static double endLearningTime;


    protected int movesNumber;

    protected double totalTime;
    protected double startTimer;
    protected double endTimer;


    //other variables for debug
    public static int exploredNodes;
    public static int totalExploredNodes;
    public static int playedMoves;
    public static int depthReached;
    public static int explorationBudget;
    public static int remainingBudgetMin;


    public int[] weightBase;
    public int[] bestWeight;
    public int maxWeight = 24;

    ArrayList<String> categoriesStrings;

    ArrayList<Integer> weights;


    RandomBot random;
    MinMaxBot minMaxBot9;


    ExplorationTree explorationTree;

    public MinMaxBot1() throws InvalidBotException {
        this.setBotName("H1");
        this.addAuthor("Maxence Schoirfer");
        this.explorationTree = new ExplorationTree();

        this.weights = new ArrayList<>();
        //categoriesStrings = new ArrayList<>();
    }

    @Override
    public void learn() {
        //   initCategories();
        startLearningTime = System.currentTimeMillis();
        categories = new int[14200];

        weightBase = new int[8];
        Arrays.fill(weightBase, 12);
        bestWeight = weightBase;
        int count = 0;
        if (LEARN) {
            try {
                random = new RandomBot();
                minMaxBot9 = new MinMaxBot();
            } catch (InvalidBotException e) {
                e.printStackTrace();
            }

            this.explorationTree.weightFeatures = bestWeight;
            this.explorationTree.initialValueDelta = 1.15f;

            do {
                upgradeCategories();
                count++;
                endLearningTime = System.currentTimeMillis();
            } while (endLearningTime - startLearningTime < MAX_LEARNING_TIME);
        } else {
            this.explorationTree.weightFeatures = weightBase;
            this.explorationTree.initialValueDelta = 1.15f;
        }
    }


    private void playGame(Bot opponent) throws InvalidBotException {
        Awele awele = new Awele(opponent, this);
        //  long e = System.currentTimeMillis();
        awele.play();
        //  System.out.println(System.currentTimeMillis() - e);
    }

    private void upgradeCategories() {
        try {
            playGame(random);
            playGame(minMaxBot9);
            playGame(this);
        } catch (Exception e) {
            System.out.println(e.toString());
            System.exit(-1);
        }
    }


    @Override
    public void initialize() {
        exploredNodes = 0;
        playedMoves = 0;
        depthReached = 0;
        explorationBudget = BUDGET;
        movesNumber = 0;
        totalTime = 0;
        explorationTree.detla = explorationTree.initialValueDelta;
    }

    @Override
    public double[] getDecision(Board board) {
        startTimer = System.currentTimeMillis();
        MinMaxNode1 root = new MaxNode1(board);
        root.remainingBudget = BUDGET;
        MinMaxNode1.player = board.getCurrentPlayer();
        explorationTree.exploreNode(root, 0, -Double.MAX_VALUE, Double.MAX_VALUE);
        endTimer = System.currentTimeMillis();

        double runningTime = endTimer - startTimer;
        totalTime += runningTime;
        movesNumber++;
        double averageRunningTime = totalTime / movesNumber;
        if (averageRunningTime < 90 && runningTime < 120) explorationTree.detla *= 1.1;
        else if (averageRunningTime > 100 && runningTime > 80) explorationTree.detla *= 0.9;

        if (DEBUG) {
            totalExploredNodes += exploredNodes;
            exploredNodes = 0;
            playedMoves++;
            explorationBudget = BUDGET;
            remainingBudgetMin = BUDGET;
            depthReached = 0;
            System.out.println("\nMove " + playedMoves + " : \n" + "Nodes: " + exploredNodes + ", Depth : " + depthReached);
            System.out.println("Temps d'execution : " + (runningTime));
            System.out.println("Temps d'execution moyen : " + averageRunningTime);
            System.out.println("Delta : " + (explorationTree.detla));
        }
        return root.getDecision();
    }

    @Override
    public void finish() {
        if (DEBUG) {
            System.out.println("----------------------------------------------------------------- END -------------------------------------------------------------------");
            //  System.out.println("Moyenne de noeuds explorés : " + totalExploredNodes / playedMoves);
            MinMaxBot1.totalExploredNodes = 0;
            MinMaxBot1.exploredNodes = 0;
            MinMaxBot1.playedMoves = 0;
            MinMaxBot1.explorationBudget = BUDGET;
            depthReached = 0;
        }


        //   saveCategories();
    }

    private void saveCategories() {
        for (int i = 0; i < categories.length; i++) categoriesStrings.add(i + "/" + categories[i]);
        if (categoriesStrings.size() > 0) writeFile("data.txt", categoriesStrings);
    }

    private void initCategories() {
        categories = new int[14200];
        try {
            File f = new File("data.txt");
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine;
            while ((readLine = b.readLine()) != null) {
                String[] parts = readLine.split("/");
                categories[Integer.parseInt(parts[0])] = Integer.parseInt(parts[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
