package awele.bot.aaTraining.h3;

import awele.core.Board;

public abstract class MinMaxNode1 {

    public static int player;
    protected double evaluation;
    protected double[] decision;

    protected final Board board;
    protected int score;
    protected int index;

    protected int numberChildren;
    protected int remainingBudget;


    public MinMaxNode1(Board board) {
        this.board = board;
        this.decision = new double[Board.NB_HOLES];
        this.evaluation = this.worst();
        this.numberChildren = 0;
    }

    /**
     * Pire score pour un joueur
     */
    protected abstract double worst();


    /**
     * Mise à jour de alpha
     *
     * @param evaluation L'évaluation courante du noeud
     * @param alpha      L'ancienne valeur d'alpha
     * @return double
     */
    protected abstract double alpha(double evaluation, double alpha);

    /**
     * Mise à jour de beta
     *
     * @param evaluation L'évaluation courante du noeud
     * @param beta       L'ancienne valeur de beta
     * @return double
     */
    protected abstract double beta(double evaluation, double beta);

    /**
     * Retourne le min ou la max entre deux valeurs, selon le type de noeud (MinNode ou MaxNode)
     *
     * @param eval1 Un double
     * @param eval2 Un autre double
     * @return Le min ou la max entre deux valeurs, selon le type de noeud
     */
    protected abstract double minmax(double eval1, double eval2);

    /**
     * Indique s'il faut faire une coupe alpha-beta, selon le type de noeud (MinNode ou MaxNode)
     *
     * @param eval  L'évaluation courante du noeud
     * @param alpha Le seuil pour la coupe alpha
     * @param beta  Le seuil pour la coupe beta
     * @return Un booléen qui indique s'il faut faire une coupe alpha-beta
     */
    protected abstract boolean alphabeta(double eval, double alpha, double beta);

    protected abstract MinMaxNode1 getNextNode(Board board);

    /**
     * L'évaluation du noeud
     *
     * @return L'évaluation du noeud
     */
    double getEvaluation() {
        return this.evaluation;
    }

    /**
     * L'évaluation de chaque coup possible pour le noeud
     *
     * @return decision
     */
    double[] getDecision() {
        return this.decision;
    }
}
