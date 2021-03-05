package awele.bot.aheuristique.h8;

import awele.core.Board;

public class MinNode1 extends MinMaxNode1
{
    public MinNode1(Board board) {
        super(board);
    }

    /**
     * Retourne le min
     * @param eval1 Un double
     * @param eval2 Un autre double
     * @return Le min entre deux valeurs, selon le type de noeud
     */
    @Override
    protected double minmax (double eval1, double eval2)
    {
        return Math.min (eval1, eval2);
    }

    /**
     * Indique s'il faut faire une coupe alpha-beta
     * (si l'évaluation courante du noeud est inférieure à l'évaluation courante du noeud parent)
     * @param eval L'évaluation courante du noeud
     * @param alpha Le seuil pour la coupe alpha
     * @param beta Le seuil pour la coupe beta
     * @return Un booléen qui indique s'il faut faire une coupe alpha-beta
     */
    @Override
    protected boolean alphabeta (double eval, double alpha, double beta)
    {
        return eval <= alpha;
    }

    @Override
    protected MaxNode1 getNextNode(Board board) {
        return new MaxNode1(board);
    }

    /**
     * Mise à jour de alpha
     * @param evaluation L'évaluation courante du noeud
     * @param alpha L'ancienne valeur d'alpha
     * @return
     */
    @Override
    protected double alpha (double evaluation, double alpha)
    {
        return alpha;
    }

    /**
     * Mise à jour de beta
     * @param evaluation L'évaluation courante du noeud
     * @param beta L'ancienne valeur de beta
     * @return
     */
    @Override
    protected double beta (double evaluation, double beta)
    {
        return Math.min (evaluation, beta);
    }

    /** Pire score : une grande valeur */
    @Override
    protected double worst ()
    {
        return Double.MAX_VALUE;
    }
}
