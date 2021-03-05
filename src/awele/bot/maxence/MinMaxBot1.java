package awele.bot.maxence;

import awele.bot.CompetitorBot;
import awele.core.Board;
import awele.core.InvalidBotException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinMaxBot1 extends CompetitorBot {

    protected static final boolean DEBUG = true;
    protected static final int BUDGET = (int) 3e6;
    protected static int[] categories;


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
        initCategories();
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
            System.out.println("\nMove " + playedMoves + " : \n" + "Nodes: " + exploredNodes + ", Depth : " + depthReached);
            totalExploredNodes += exploredNodes;
            exploredNodes = 0;
            playedMoves++;
            explorationBudget = BUDGET;
            remainingBudgetMin = BUDGET;
            depthReached = 0;
        }


        if (DEBUG) System.out.println("Temps d'execution : " + (runningTime));
        if (DEBUG) System.out.println("Temps d'execution moyen : " + averageRunningTime);
        if (DEBUG) System.out.println("Delta : " + (MinMaxNode1.detla));
        if (averageRunningTime < 90 && runningTime < 120) MinMaxNode1.detla *= 1.1;
        else if (averageRunningTime > 100 && runningTime > 80) MinMaxNode1.detla *= 0.9;
        return root.getDecision();
    }

    @Override
    public void finish() {
        if (DEBUG) {
            System.out.println("----------------------------------------------------------------- END -------------------------------------------------------------------");
            System.out.println("Moyenne de noeuds explorés : " + totalExploredNodes / playedMoves);
            MinMaxBot1.totalExploredNodes = 0;
            MinMaxBot1.exploredNodes = 0;
            MinMaxBot1.playedMoves = 0;
            MinMaxBot1.explorationBudget = BUDGET;
            depthReached = 0;
        }


        saveCategories();
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
