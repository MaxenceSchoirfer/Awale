package awele.bot.maxence;

import awele.bot.Bot;
import awele.bot.CompetitorBot;
import awele.bot.maxence.bots.minmax9.MinMaxBot;
import awele.bot.maxence.bots.random.RandomBot;
import awele.core.Awele;
import awele.core.Board;
import awele.core.InvalidBotException;
import awele.run.Main;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MinMaxBot1 extends CompetitorBot {

    protected static final boolean DEBUG = true;
    protected static final boolean LEARN = true;
    protected static final int BUDGET = (int) 3e6;
    protected static int[] categories;

    //private static final int MAX_LEARNING_TIME = 1000 * 60 * 60 * 1; // 1 h
    private static final int MAX_LEARNING_TIME = 1000 * 60 * 30 * 1; // 1 h
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

    ArrayList<String> traces;
    ArrayList<String> average;
    ArrayList<String> categoriesStrings;


    RandomBot random;
    MinMaxBot minMaxBot9;

    public MinMaxBot1() throws InvalidBotException {
        this.setBotName("MinMaxWithBudget");
        this.addAuthor("Maxence Schoirfer");
        categoriesStrings = new ArrayList<>();
        if (DEBUG) {
            exploredNodes = 0;
            playedMoves = 0;
            depthReached = 0;
            traces = new ArrayList<>();
            average = new ArrayList<>();
            explorationBudget = BUDGET;
        }


    }

    @Override
    public void learn() {
        //   initCategories();
        startLearningTime = System.currentTimeMillis();
        categories = new int[14200];

        int count = 0;
        if (LEARN){
            try {
                random = new RandomBot();
                minMaxBot9 = new MinMaxBot();
            } catch (InvalidBotException e) {
                e.printStackTrace();
            }

            //choisir les poids heuristique
            //   parametrer delta

            do {
                upgradeCategories();
                count++;
                endLearningTime = System.currentTimeMillis();
                System.out.println("Learning : " + ((endLearningTime - startLearningTime)/1000));
                System.out.println("Count : " + count);
            } while (endLearningTime - startLearningTime < MAX_LEARNING_TIME);
            System.out.println("Fin Apprentissage");
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
          //  playGame(random);
            playGame(minMaxBot9);
            playGame(this);
            System.out.println("\n");
        } catch (Exception e) {
            System.out.println(e.toString());
            System.exit(-1);
        }
    }


    @Override
    public void initialize() {
        movesNumber = 0;
        totalTime = 0;
        MinMaxNode1.detla = 1.15f;
    }

    @Override
    public double[] getDecision(Board board) {
        startTimer = System.currentTimeMillis();

        MinMaxNode1 root = new MaxNode1(board);
        root.remainingBudget = BUDGET;
        MinMaxNode1.player = board.getCurrentPlayer();
        MinMaxNode1.exploreNextNode(root, 0, -Double.MAX_VALUE, Double.MAX_VALUE);
        endTimer = System.currentTimeMillis();
        double runningTime = endTimer - startTimer;
        totalTime += runningTime;
        movesNumber++;
        double averageRunningTime = totalTime / movesNumber;

        if (DEBUG) {
         //   System.out.println("\nMove " + playedMoves + " : \n" + "Nodes: " + exploredNodes + ", Depth : " + depthReached);
            totalExploredNodes += exploredNodes;
            exploredNodes = 0;
            playedMoves++;
            explorationBudget = BUDGET;
            remainingBudgetMin = BUDGET;
            depthReached = 0;
        }


  //      if (DEBUG) System.out.println("Temps d'execution : " + (runningTime));
    //    if (DEBUG) System.out.println("Temps d'execution moyen : " + averageRunningTime);
    //    if (DEBUG) System.out.println("Delta : " + (MinMaxNode1.detla));
        if (averageRunningTime < 90 && runningTime < 120) MinMaxNode1.detla *= 1.1;
        else if (averageRunningTime > 100 && runningTime > 80) MinMaxNode1.detla *= 0.9;
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
