package awele.bot.aheuristique.h4;

import awele.core.Board;
import awele.core.InvalidBotException;

public abstract class MinMaxNode1 {

    public static int player;
    private double evaluation;
    private double[] decision;

    private Board board;
    private int score;
    private int index;

    private int numberChildren;
    protected int remainingBudget;

    private double detla = 1;

    public MinMaxNode1(Board board) {
        this.board = board;
        this.decision = new double[Board.NB_HOLES];
        this.evaluation = this.worst();
        this.numberChildren = 0;
    }


    private static int heurisitque2(Board board) {
        int SequenceLess3Seeds = 0;
        int SequenceLess3SeedsOpponent = 0;


        int a1 = board.getScore(MinMaxNode1.player);
        int a3 = 0;
        int a5 = 0;
        int a7 = 0;
        int a9 = 0;

        boolean sequence = false;
        for (int j = 0; j < Board.NB_HOLES; j++) {
            if (board.getPlayerHoles()[j] >= 12 && a7 < board.getPlayerHoles()[j])a7 = board.getPlayerHoles()[j];
            if (board.getPlayerHoles()[j] == 0)a5++;

            if (board.getPlayerHoles()[j] == 1 && board.getPlayerHoles()[j] == 2) {
                a3++;
                if (sequence) {
                    SequenceLess3Seeds++;
                    if (SequenceLess3Seeds > a9) a9 = SequenceLess3Seeds;
                }
                sequence = true;
            } else {
                sequence = false;
            }
        }
        if (a3 > 0) a9 += 1;




        int a2 = board.getScore(Board.otherPlayer(MinMaxNode1.player));
        int a4 = 0;
        int a6 = 0;
        int a8 = 0;
        int a10 = 0;

        sequence = false;
        for (int j = Board.NB_HOLES - 1; j >= 0; j--) {
            if (board.getOpponentHoles()[j] >= 12 && a8 < board.getOpponentHoles()[j])a8 = board.getOpponentHoles()[j];
            if (board.getOpponentHoles()[j] == 0)a6++;
            if (board.getOpponentHoles()[j] == 1 && board.getOpponentHoles()[j] == 2) {
                a4++;
                if (sequence) {
                    SequenceLess3SeedsOpponent++;
                    if (SequenceLess3SeedsOpponent > a10)a10 = SequenceLess3SeedsOpponent;
                }
                sequence = true;
            }else{
                sequence = false;
            }
        }
        if (a4 > 0) a10 += 1;

        return a1 - a2 - a3 + a4 + a7 - a8;
    }

    protected static MinMaxNode1 exploreNextNode(MinMaxNode1 node, int depth, double alpha, double beta) {

        MinMaxNode1[] sortedChildren = sortChildrenByScore(node, getChildren(node));
        //
        int childrenRemainingBudget = (int) ((node.remainingBudget / node.numberChildren) * node.detla);

        // just for debug
        if (MinMaxBot1.depthReached < depth) MinMaxBot1.depthReached = depth;
        if (MinMaxBot1.remainingBudgetMin > childrenRemainingBudget)
            MinMaxBot1.remainingBudgetMin = childrenRemainingBudget;


        for (int i = 0; i < sortedChildren.length; i++) {
            if (sortedChildren[i] == null) continue;

            //just for debug
            MinMaxBot1.explorationBudget--;
            MinMaxBot1.exploredNodes++;


            sortedChildren[i].remainingBudget = childrenRemainingBudget;

           //   if (isTerminalNode(sortedChildren[i].board, sortedChildren[i].score) || depth >= MinMaxBot1.MAX_DEPTH) {
            if (isTerminalNode(sortedChildren[i].board, sortedChildren[i].score) || sortedChildren[i].remainingBudget < 6) {
                //node.decision[sortedChildren[i].index] = diffScore(sortedChildren[i].board);
                node.decision[sortedChildren[i].index] = heurisitque2(sortedChildren[i].board);
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





    private static int diffScore(Board board) {
        return board.getScore(MinMaxNode1.player) - board.getScore(Board.otherPlayer(MinMaxNode1.player));
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
