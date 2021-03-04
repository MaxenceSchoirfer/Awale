package awele.bot.alphabetanosort;

import awele.bot.CompetitorBot;
import awele.bot.DemoBot;
import awele.core.Board;
import awele.core.InvalidBotException;

public class MinMaxBot extends CompetitorBot {

    /** Profondeur maximale */
    private static final int MAX_DEPTH = 10;

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
