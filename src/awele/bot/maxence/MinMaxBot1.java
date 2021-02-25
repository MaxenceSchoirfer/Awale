package awele.bot.maxence;

import awele.bot.CompetitorBot;
import awele.bot.minmax.MinMaxBot;
import awele.core.Board;
import awele.core.InvalidBotException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MinMaxBot1 extends CompetitorBot {

    protected static final int MAX_DEPTH = 2;

    public static HashMap<String,Integer> categorie;

    public static int nodes;
    public static int coup;
    public static int depth;
    public static int depthMAx = 0;

    public static int[] lastMoves;

    public static int budget = 0;

    ArrayList<String> s = new ArrayList<>();

    public MinMaxBot1() throws InvalidBotException {
        this.setBotName("MinMaxMaxence");
        this.addAuthor("Maxence Schoirfer");
        nodes = 0;
        coup = 0;
        lastMoves = new int[100];
        categorie = new HashMap<>();
    }

    @Override
    public void learn() {
    }

    @Override
    public void initialize() {
    }


    @Override
    public double[] getDecision(Board board) {
        String stringBuilder = "Coup : " + nodes + "\n" +
                "joueur : " + board.getCurrentPlayer() + "\n" + board.toString() + "\nScore joueur 0 : " + board.getScore(0) +
                "\nScore joueur 1 : " + board.getScore(1) + "\n";
        s.add("coup " + coup + " : \n" + "Nodes: " + nodes +  ", Depth : " + depthMAx + ", Budget : " + budget + "\n");
        MinMaxBot1.nodes = 0;
        MinMaxBot1.coup++;
        MinMaxBot1.budget = 34000;
        MinMaxNode1.initialize(board, MAX_DEPTH);
        return new MaxNode1(board).getDecision();
    }

    @Override
    public void finish() {
        s.add("-------------------------------- NOUVELLE PARTIE ---------------------------------");
        lastMoves = new int[100];
        MinMaxBot1.nodes = 0;
        MinMaxBot1.coup = 0;
        MinMaxBot1.budget = 34000;
        ecrireFichier("trace.txt",s);
    }



    public static void ecrireFichier(String nomFichier, List<String> lignes) {
        Writer fluxSortie = null;
        try {
            fluxSortie = new PrintWriter(new BufferedWriter(new FileWriter(
                    nomFichier)));
            for (int i = 0; i < lignes.size() - 1; i++) {
                fluxSortie.write(lignes.get(i) + "\n");
            }
            fluxSortie.write(lignes.get(lignes.size() - 1));
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        try {
            fluxSortie.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
