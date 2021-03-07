package awele.bot.aaTraining.h1.minmax9;

import awele.bot.Bot;
import awele.core.Board;
import awele.core.InvalidBotException;

public class MinMaxBot extends Bot {

    /** Profondeur maximale */
    private static final int MAX_DEPTH = 9;

    /**
     * @throws InvalidBotException
     */
    public MinMaxBot () throws InvalidBotException
    {
        this.setBotName ("AlphaBetaDepth9");
        this.addAuthor ("Maxence");
    }

    /**
     * Rien à faire
     */
    @Override
    public void initialize ()
    {
    }

    /**
     * Pas d'apprentissage
     */
    @Override
    public void learn ()
    {
    }

    /**
     * Sélection du coup selon l'algorithme MinMax
     */
    @Override
    public double [] getDecision (Board board)
    {
        MinMaxNode.initialize (board, MinMaxBot.MAX_DEPTH);
        double[] decision = new MaxNode(board).getDecision();
        return decision;
    }

    /**
     * Rien à faire
     */
    @Override
    public void finish ()
    {
    }
}
