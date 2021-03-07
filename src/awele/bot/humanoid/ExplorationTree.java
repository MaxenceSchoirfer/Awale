package awele.bot.humanoid;

import awele.core.Board;
import awele.core.InvalidBotException;

public class ExplorationTree {

    protected float initialValueDelta;

    protected int[] weightFeatures;

    protected int[] features;
    protected float detla;

    public ExplorationTree() {

    }


    protected void exploreNode(MinMaxNode node, int depth, double alpha, double beta) {
        MinMaxNode[] sortedChildren = getSortedChildreByScore(node);
        int childrenRemainingBudget = (int) ((node.remainingBudget / node.numberChildren) * detla);

        for (int i = 0; i < sortedChildren.length; i++) {
            if (sortedChildren[i] == null) continue;
            sortedChildren[i].remainingBudget = childrenRemainingBudget;

            if (isTerminalNode(sortedChildren[i].board, sortedChildren[i].score) || sortedChildren[i].remainingBudget < 6) {
                node.decision[sortedChildren[i].index] = evaluateNode(sortedChildren[i].board);
            } else {
                exploreNode(sortedChildren[i], depth + 1, alpha, beta);
                node.decision[sortedChildren[i].index] = sortedChildren[i].getEvaluation();
            }

            if (node.minmax(node.decision[sortedChildren[i].index], node.evaluation) != node.evaluation) {
                node.evaluation = node.minmax(node.decision[sortedChildren[i].index], node.evaluation);
                updateCategoryScore(node, sortedChildren, i, sortedChildren[i]);
            }

            if (depth > 0) {
                if (node.alphabeta(node.evaluation, alpha, beta)) {
                    updateCategoryScore(node, sortedChildren, i, sortedChildren[i]);
                    return;
                }
                alpha = node.alpha(node.evaluation, alpha);
                beta = node.beta(node.evaluation, beta);
            }
        }
    }

    protected double evaluateNode(Board board) {
        features = new int[8];
        features[0] = board.getScore(MinMaxNode.player); //score
        features[1] = board.getScore(Board.otherPlayer(MinMaxNode.player)); //score opponent
        /*
        features[2] = 0;//n holes with 1 or 2 seeds
        features[3] = 0;//n holes opponent with 1 or 2 seeds
        features[4] = 0;//n holes with 0 seed
        features[5] = 0;//n holes opponent with 0 seed
        features[6] = 0;//n holes with >= 12 seeds
        features[7] = 0;//n opponent holes with >= 12 seeds
        */


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

        return +(weightFeatures[0] * features[0])
                - (weightFeatures[1] * features[1])
                - (weightFeatures[2] * features[2])
                + (weightFeatures[3] * features[3])
                + (weightFeatures[4] * features[4])
                - (weightFeatures[5] * features[5])
                + (weightFeatures[6] * features[6])
                - (weightFeatures[7] * features[7]);
    }

    protected void updateCategoryScore(MinMaxNode node, MinMaxNode[] sortedChildren, int i, MinMaxNode child) {
        Humanoid.categories[getCategory(node, child)] += i;
        for (int j = 0; j < i; j++) {
            if (sortedChildren[j] == null) continue;
            Humanoid.categories[getCategory(node, sortedChildren[j])] += -1;
        }
    }

    protected boolean isTerminalNode(Board board, int score) {
        return ((score < 0) ||
                (board.getScore(Board.otherPlayer(board.getCurrentPlayer())) >= 25) ||
                (board.getNbSeeds() <= 6));
    }

    protected MinMaxNode[] getChildren(MinMaxNode parent) {
        MinMaxNode[] children = new MinMaxNode[6];
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

    protected MinMaxNode[] getSortedChildreByScore(MinMaxNode parent) {
        MinMaxNode[] children = getChildren(parent);
        for (int i = children.length - 1; i < 1; i++) {
            for (int j = 0; j < i - 1; j++) {
                int catJ1 = getCategory(parent, children[j + 1]);
                int valueJ1 = Humanoid.categories[catJ1];
                int catJ = getCategory(parent, children[j]);
                int valueJ = Humanoid.categories[catJ];
                if (valueJ1 < valueJ) {
                    MinMaxNode temp = children[j + 1];
                    children[j + 1] = children[j];
                    children[j] = temp;
                }
            }
        }
        return children;
    }

    protected int getCategory(MinMaxNode parent, MinMaxNode child) {
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
}
