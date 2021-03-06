package awele.bot.maxence;

import awele.core.Board;
import awele.core.InvalidBotException;

public abstract class MinMaxNode1 {

    public static int player;
    private double evaluation;
    private double[] decision;

    private final Board board;
    private int score;
    private int index;

    private int numberChildren;
    protected int remainingBudget;

    protected static int[] weightFeatures;
    protected static double[] features;

    public static float detla = 1;

    public MinMaxNode1(Board board) {
        this.board = board;
        this.decision = new double[Board.NB_HOLES];
        this.evaluation = this.worst();
        this.numberChildren = 0;

        weightFeatures = new int[8];
        features = new double[8];

        weightFeatures[0] = 1;
        weightFeatures[1] = 1;
        weightFeatures[2] = 1;
        weightFeatures[3] = 1;
        weightFeatures[4] = 1;
        weightFeatures[5] = 1;
        weightFeatures[6] = 1;
        weightFeatures[7] = 1;
    }


    protected static MinMaxNode1 exploreNextNode(MinMaxNode1 node, int depth, double alpha, double beta) {
        MinMaxNode1[] sortedChildren = sortChildrenByScore(node, getChildren(node));
        int childrenRemainingBudget = (int) ((node.remainingBudget / node.numberChildren) * detla);

        if (MinMaxBot1.DEBUG) {
            if (MinMaxBot1.depthReached < depth) MinMaxBot1.depthReached = depth;
            if (MinMaxBot1.remainingBudgetMin > childrenRemainingBudget)
                MinMaxBot1.remainingBudgetMin = childrenRemainingBudget;
        }


        for (int i = 0; i < sortedChildren.length; i++) {
            if (sortedChildren[i] == null) continue;
            if (MinMaxBot1.DEBUG) {
                MinMaxBot1.explorationBudget--;
                MinMaxBot1.exploredNodes++;
            }

            sortedChildren[i].remainingBudget = childrenRemainingBudget;
            if (isTerminalNode(sortedChildren[i].board, sortedChildren[i].score) || sortedChildren[i].remainingBudget < 6) {
                node.decision[sortedChildren[i].index] = evaluateNode(sortedChildren[i].board);
            } else {
                exploreNextNode(sortedChildren[i], depth + 1, alpha, beta);
                node.decision[sortedChildren[i].index] = sortedChildren[i].getEvaluation();
            }

            if (node.minmax(node.decision[sortedChildren[i].index], node.evaluation) != node.evaluation) {
                node.evaluation = node.minmax(node.decision[sortedChildren[i].index], node.evaluation);
                updateCategoryScore(node, sortedChildren, i, sortedChildren[i]);
            }

            if (depth > 0) {
                if (node.alphabeta(node.evaluation, alpha, beta)) {
                    updateCategoryScore(node, sortedChildren, i, sortedChildren[i]);
                    return node;
                }
                alpha = node.alpha(node.evaluation, alpha);
                beta = node.beta(node.evaluation, beta);
            }
        }
        return node;
    }

    private static void updateCategoryScore(MinMaxNode1 node, MinMaxNode1[] sortedChildren, int i, MinMaxNode1 child) {
        updateScore(getCategory(node, child), i);
        for (int j = 0; j < i; j++) {
            if (sortedChildren[j] == null) continue;
            updateScore(getCategory(node, sortedChildren[j]), -1);
        }
    }

    private static void updateScore(int category, int i) {
        MinMaxBot1.categories[category] += i;
    }

    private static boolean isTerminalNode(Board board, int score) {
        return ((score < 0) ||
                (board.getScore(Board.otherPlayer(board.getCurrentPlayer())) >= 25) ||
                (board.getNbSeeds() <= 6));
    }

    private static MinMaxNode1[] getChildren(MinMaxNode1 parent) {
        MinMaxNode1[] children = new MinMaxNode1[6];
        parent.decision = new double[Board.NB_HOLES];
        for (int i = 0; i < Board.NB_HOLES; i++)
            if (parent.board.getPlayerHoles()[i] != 0) {
                double[] decision = new double[Board.NB_HOLES];
                decision[i] = 1;
                Board copy = (Board) parent.board.clone();
                parent.numberChildren++;
                try {
                    int score = copy.playMoveSimulationScore(copy.getCurrentPlayer(), decision);
                    copy = copy.playMoveSimulationBoard(copy.getCurrentPlayer(), decision);
                    children[i] = parent.getNextNode(copy);
                    children[i].score = score;
                    children[i].index = i;
                } catch (InvalidBotException e) {
                    e.printStackTrace();
                }
            }
        return children;
    }

    private static MinMaxNode1[] sortChildrenByScore(MinMaxNode1 parent, MinMaxNode1[] children) {
        for (int i = children.length - 1; i < 1; i++) {
            for (int j = 0; j < i - 1; j++) {
                int catJ1 = getCategory(parent, children[j + 1]);
                int valueJ1 = MinMaxBot1.categories[catJ1];
                int catJ = getCategory(parent, children[j]);
                int valueJ = MinMaxBot1.categories[catJ];
                if (valueJ1 < valueJ) {
                    MinMaxNode1 temp = children[j + 1];
                    children[j + 1] = children[j];
                    children[j] = temp;
                }
            }
        }
        return children;
    }

    private static int getCategory(MinMaxNode1 parent, MinMaxNode1 child) {
        int[][] lastState = new int[2][Board.NB_HOLES];
        int[][] currentState = new int[2][Board.NB_HOLES];

        lastState[0] = parent.board.getPlayerHoles();
        lastState[1] = parent.board.getOpponentHoles();

        currentState[0] = child.board.getOpponentHoles();
        currentState[1] = child.board.getPlayerHoles();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < Board.NB_HOLES; j++) {
                //j = start hole position || lastState[i][j] = number of seeds on start hole || currentState[i1][j1] = number of seeds on end hole
                if (currentState[i][j] == 0 && lastState[i][j] != 0) {
                    int nSeeds = lastState[i][j];
                    int i1 = i;
                    int j1 = j;
                    boardCourse:
                    while (nSeeds > 0) {
                        if (i1 == i) {
                            if (nSeeds == lastState[i][j]) j1 = j;
                            else j1 = 0;
                            for (; j1 < Board.NB_HOLES; j1++) {
                                if (j1 == j) continue;
                                nSeeds--;
                                if (nSeeds == 0) break boardCourse;
                            }
                            i1 = (i1 + 1) % 2;
                        }
                        if (i1 != i) {
                            for (j1 = Board.NB_HOLES - 1; j1 >= 0; j1--) {
                                nSeeds--;
                                if (nSeeds == 0) break boardCourse;
                            }
                            i1 = (i1 + 1) % 2;
                        }

                    }
                    return (j + 6 * lastState[i][j] + 6 * 48 * currentState[i1][j1]);
                }
            }
        }
        return -1;
    }

    /**
     * Pire score pour un joueur
     */
    protected abstract double worst();

    private static double evaluateNode(Board board) {
        features[0] = board.getScore(MinMaxNode1.player); //score
        features[1] = board.getScore(Board.otherPlayer(MinMaxNode1.player)); //score opponent
        features[2] = 0;//n holes with 1 or 2 seeds
        features[3] = 0;//n holes opponent with 1 or 2 seeds
        features[4] = 0;//n holes with 0 seed
        features[5] = 0;//n holes opponent with 0 seed
        features[6] = 0;//n holes with >= 12 seeds
        features[7] = 0;//n opponent holes with >= 12 seeds


        for (int j = 0; j < Board.NB_HOLES; j++) {
            if (board.getPlayerHoles()[j] == 1 && board.getPlayerHoles()[j] == 2) features[2]++;
            else if (board.getPlayerHoles()[j] == 0) features[4]++;
            else if (board.getPlayerHoles()[j] >= 12) features[6]++;
        }

        for (int j = Board.NB_HOLES - 1; j >= 0; j--) {
            if (board.getOpponentHoles()[j] == 1 && board.getOpponentHoles()[j] == 2) features[3]++;
            else if (board.getOpponentHoles()[j] == 0) features[5]++;
            else if (board.getOpponentHoles()[j] >= 12) features[7]++;
        }

        return weightFeatures[0] * features[0] + (-weightFeatures[1]) * features[1];
    }


    /**
     * Mise à jour de alpha
     *
     * @param evaluation L'évaluation courante du noeud
     * @param alpha      L'ancienne valeur d'alpha
     * @return
     */
    protected abstract double alpha(double evaluation, double alpha);

    /**
     * Mise à jour de beta
     *
     * @param evaluation L'évaluation courante du noeud
     * @param beta       L'ancienne valeur de beta
     * @return
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
     * @return
     */
    double[] getDecision() {
        return this.decision;
    }
}
