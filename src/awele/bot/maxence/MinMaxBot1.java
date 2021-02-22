package awele.bot.maxence;

import awele.bot.CompetitorBot;
import awele.core.Board;
import awele.core.InvalidBotException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MinMaxBot1 extends CompetitorBot {

    private static final int MAX_DEPTH = 6;

    public static int count;

    ArrayList<String> s = new ArrayList<>();

    public MinMaxBot1() throws InvalidBotException {
        this.setBotName("MinMaxMaxence");
        this.addAuthor("Maxence Schoirfer");
        count = 0;
    }

    @Override
    public void learn() {
    }

    @Override
    public void initialize() {
    }


    @Override
    public double[] getDecision(Board board) {
        String stringBuilder = "Coup : " + count + "\n" +
                "joueur : " + board.getCurrentPlayer() + "\n" + board.toString() + "\nScore joueur 0 : " + board.getScore(0) +
                "\nScore joueur 1 : " + board.getScore(1) + "\n";
        s.add(stringBuilder);
        MinMaxBot1.count++;
        MinMaxNode1.initialize(board, MAX_DEPTH);
        return new MaxNode1(board).getDecision();
    }

    @Override
    public void finish() {

        s.add(String.valueOf( MinMaxBot1.count++));
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
