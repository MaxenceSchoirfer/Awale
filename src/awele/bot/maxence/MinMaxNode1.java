package awele.bot.maxence;

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

    /*
    protected static MinMaxNode1 exploreNextNode2(MinMaxNode1 node, int depth, double alpha, double beta) {
        if (MinMaxBot1.depthReached < depth) MinMaxBot1.depthReached = depth;

        //  MinMaxNode1[] sortedChildren = sortChildrenByScore(node, getChildren(node));
        MinMaxNode1[] sortedChildren = getChildren(node);
        //    int childrenRemainingBudget = (node.remainingBudget / node.numberChildren) * node.detla;
        //  if (MinMaxBot1.remainingBudgetMin > childrenRemainingBudget) MinMaxBot1.remainingBudgetMin = childrenRemainingBudget;
        for (MinMaxNode1 childNode : sortedChildren) {
            if (childNode == null) continue;
            MinMaxBot1.exploredNodes++;
            //        childNode.remainingBudget = childrenRemainingBudget;

            if (isTerminalNode(childNode.board, childNode.score) || depth == MinMaxBot1.MAX_DEPTH) {
                //     if (isTerminalNode(childNode.board, childNode.score) || childNode.remainingBudget < 6) {
                node.decision[childNode.index] = diffScore(childNode.board);
            } else {
                childNode = exploreNextNode2(childNode, depth + 1, alpha, beta);
                node.decision[childNode.index] = childNode.getEvaluation();
            }

            //if (node.minmax(node.decision[childNode.index], node.evaluation) != node.evaluation) {
            if (node == MinMaxBot1.rootNode) {
                //     System.out.println("");
            }
            node.evaluation = node.minmax(node.decision[childNode.index], node.evaluation);
            //updateCategoryScore(node, sortedChildren, childNode.index, childNode);
            //}

            if (depth > 0) {
                if (node.alphabeta(node.evaluation, alpha, beta)) {
                    //updateCategoryScore(node, sortedChildren, i, sortedChildren[i]);
                    return node;
                }
                alpha = node.alpha(node.evaluation, alpha);
                beta = node.beta(node.evaluation, beta);
            }
        }
        return node;
    }
*/

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

              if (isTerminalNode(sortedChildren[i].board, sortedChildren[i].score) || depth >= MinMaxBot1.MAX_DEPTH) {
        //    if (isTerminalNode(sortedChildren[i].board, sortedChildren[i].score) || sortedChildren[i].remainingBudget < 6) {
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

    /*
    private static int getNombreTrouPourCapture(boolean self, int nombreGraine, Board board) {
        int[] holes;
        int count = 0;
        if (self) holes = board.getPlayerHoles();
        else holes = board.getOpponentHoles();
        for (int i = 0; i < Board.NB_HOLES; i++) {
            if (holes[i] == nombreGraine)
        }
    }

    private static double heuristique2(Board board) {

        double pNombreTrouAdversairePourCapture2Graines = 0.8;
        double NombreTrouAdversairePourCapture2Graines = 0.8;
        double a1 = NombreTrouAdversairePourCapture2Graines * pNombreTrouAdversairePourCapture2Graines;

        double pNombreTrouAdversairePourCapture3Graines = 1;
        double NombreTrouAdversairePourCapture3Graines = 1;
        double a2 = pNombreTrouAdversairePourCapture3Graines * NombreTrouAdversairePourCapture3Graines;

        double pNombreTrouPourCapture2Graines = 0.06;
        double NombreTrouPourCapture2Graines = 0.06;
        double a3 = pNombreTrouPourCapture2Graines * NombreTrouPourCapture2Graines;

        double pNombreTrouPourCapture3Graines = 0;
        double NombreTrouPourCapture3Graines = 0;
        double a4 = pNombreTrouPourCapture3Graines * NombreTrouPourCapture3Graines;


        double pNombreTrouAdversaireAvecAssezDeGrainePourVenirAutreCote = 0.87;
        double NombreTrouAdversaireAvecAssezDeGrainePourVenirAutreCote = 0.87;
        double a5 = pNombreTrouAdversaireAvecAssezDeGrainePourVenirAutreCote * NombreTrouAdversaireAvecAssezDeGrainePourVenirAutreCote;


        double pNombreTrouAvecAssezDeGrainePourVenirAutreCote = 0.60;
        double NombreTrouAvecAssezDeGrainePourVenirAutreCote = 0.60;
        double a6 = pNombreTrouAvecAssezDeGrainePourVenirAutreCote * NombreTrouAvecAssezDeGrainePourVenirAutreCote;

        double pNombredeKrouAdversaire = 0;
        double NombredeKrouAdversaire = 0;
        double a7 = pNombredeKrouAdversaire * NombredeKrouAdversaire;


        double pNombreddeKrou = 0.2;
        double NombreddeKrou = 0.2;
        double a8 = pNombreddeKrou * NombreddeKrou;

        double pScoreActuelAdversaire = 0.73;
        double ScoreActuelAdversaire = 0.73;
        double a9 = pScoreActuelAdversaire * ScoreActuelAdversaire;


        double pScoreActuel = 0.93;
        double ScoreActuel = 0.93;
        double a10 = pScoreActuel * ScoreActuel;


        double pTrouVideAdversaire = 0;
        double TrouVideAdversaire = 0;
        double a11 = pTrouVideAdversaire * TrouVideAdversaire;

        double pTrouVide = 0.80;
        double TrouVide = 0.80;
        double a12 = pTrouVide * TrouVide;

        return a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8 + a9 + a10 + a11 + a12;
    }

*/


    private static int heurisitque2(Board board) {
        int scorePlayer = board.getScore(MinMaxNode1.player);
        int scoreOpponent = board.getScore(Board.otherPlayer(MinMaxNode1.player));

        int holesLess3Seeds = 0;
        int maxSequenceLess3Seeds = 0;
        int SequenceLess3Seeds = 0;

        boolean sequence = false;
        for (int j = 0; j < Board.NB_HOLES; j++) {
            if (board.getPlayerHoles()[j] < 3 && board.getPlayerHoles()[j] >0) {
                holesLess3Seeds++;
                if (sequence) {
                    SequenceLess3Seeds++;
                    if (SequenceLess3Seeds > maxSequenceLess3Seeds) maxSequenceLess3Seeds = SequenceLess3Seeds;
                }
                sequence = true;
            } else {
                sequence = false;
            }
        }
        if (holesLess3Seeds > 0) maxSequenceLess3Seeds += 1;

        int holesLess3SeedsOpponent = 0;
        int maxSequenceLess3SeedsOpponent = 0;
        int SequenceLess3SeedsOpponent = 0;

        sequence = false;

        for (int j = Board.NB_HOLES - 1; j >= 0; j--) {
            if (board.getOpponentHoles()[j] < 3 && board.getOpponentHoles()[j] > 0) {
                holesLess3SeedsOpponent++;
                if (sequence) {
                    SequenceLess3SeedsOpponent++;
                    if (SequenceLess3SeedsOpponent > maxSequenceLess3SeedsOpponent)maxSequenceLess3SeedsOpponent = SequenceLess3SeedsOpponent;
                }
                sequence = true;
            }else{
                sequence = false;
            }
        }
        if (SequenceLess3SeedsOpponent > 0) maxSequenceLess3SeedsOpponent += 1;

        // return scorePlayer + holesLess3Seeds + maxSequenceLess3Seeds - scoreOpponent;
      //  return scorePlayer - scoreOpponent - holesLess3Seeds - maxSequenceLess3Seeds;
       return scorePlayer - scoreOpponent - holesLess3Seeds - maxSequenceLess3Seeds + holesLess3SeedsOpponent + maxSequenceLess3SeedsOpponent;
        //      return scorePlayer - scoreOpponent;


    }

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
