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

    protected static final int MAX_DEPTH = 9;
    protected static final int BUDGET = 100000;

    public static int exploredNodes;
    public static int totalExploredNodes;
    public static int playedMoves;
    public static int depthReached;
    public static int explorationBudget;

    public static int[] categories;

    ArrayList<String> traces = new ArrayList<>();
    ArrayList<String> average = new ArrayList<>();
    ArrayList<String> categoriesStrings = new ArrayList<>();

    public MinMaxBot1() throws InvalidBotException {
        this.setBotName("MinMaxMaxence");
        this.addAuthor("Maxence Schoirfer");
        exploredNodes = 0;
        playedMoves = 0;
        depthReached = 0;
        explorationBudget = BUDGET;
    }

    @Override
    public void learn() {
        initCategories();

    }

    @Override
    public void initialize() {
      //   traces.add("---------- New Game ----------");
    }

    @Override
    public double[] getDecision(Board board) {
        //     System.out.println("Move " + playedMoves + " : \n" + "Nodes: " + exploredNodes + ", Depth : " + depthReached + ", Budget : " + explorationBudget + "\n");
        traces.add("Move " + playedMoves + " : \n" + "Nodes: " + exploredNodes + ", Depth : " + depthReached + ", Budget : " + explorationBudget + "\n");
        totalExploredNodes += exploredNodes;
       // if (exploredNodes > 75000) System.out.println("EXPLORED NODES : " + exploredNodes);
        exploredNodes = 0;
        explorationBudget = BUDGET;
        playedMoves++;
        return MinMaxNode1.exploreNextNode(new MaxNode1(board), 0, -Double.MAX_VALUE, Double.MAX_VALUE).getDecision();
    }

    @Override
    public void finish() {
        //   System.out.println("Move " + playedMoves + " : \n" + "Nodes: " + exploredNodes + ", Depth : " + depthReached + ", Budget : " + explorationBudget + "\n");
     //      traces.add("Move " + playedMoves + " : \n" + "Nodes: " + exploredNodes + ", Depth : " + depthReached + ", Budget : " + explorationBudget + "\n");
     //     traces.add("-------------------------------- END ---------------------------------");
          average.add("Moyenne de noeuds explorés : " + totalExploredNodes/playedMoves);

        MinMaxBot1.totalExploredNodes = 0;
        MinMaxBot1.exploredNodes = 0;
        MinMaxBot1.playedMoves = 0;
        MinMaxBot1.explorationBudget = BUDGET;
     //     writeFile("trace.txt", traces);
         writeFile("average.txt", average);
          saveCategories();
    }

    private void saveCategories() {
        for (int i = 0; i < categories.length; i++) {
            //if( categories[i] != 0 )
                categoriesStrings.add(i + "/" + categories[i]);
        }
//        for (Map.Entry<String, Integer> entry : categories.entrySet()) {
//            categoriesStrings.add(entry.getKey() + "/" + entry.getValue());
//        }
        if (categoriesStrings.size() > 0) writeFile("data.txt", categoriesStrings);
    }

    private void initCategories() {
        categories = new int[10000];
        try {
            File f = new File("data.txt");
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine;
            while ((readLine = b.readLine()) != null) {
                String[] parts = readLine.split("/");
                categories[Integer.parseInt(parts[0])] = Integer.parseInt(parts[1]);
                //categories.put(parts[0], Integer.valueOf(parts[1]));
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
