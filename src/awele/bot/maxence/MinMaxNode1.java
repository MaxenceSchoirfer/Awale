package awele.bot.maxence;

import awele.core.Board;
import awele.core.InvalidBotException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class MinMaxNode1 {

    private static int player;
    private double evaluation;
    private double[] decision;

    private Board board;
    private int score;
    private int index;

    public MinMaxNode1(Board board) {
        this.board = board;
        this.decision = new double[Board.NB_HOLES];
        this.evaluation = this.worst();
    }

    protected static MinMaxNode1 exploreNextNode(MinMaxNode1 node, int depth, double alpha, double beta) {
        if (MinMaxBot1.depthReached < depth) MinMaxBot1.depthReached = depth;
        MinMaxBot1.exploredNodes++;
        MinMaxBot1.explorationBudget--;

        ArrayList<MinMaxNode1> sortedChildren = sortChildrenByScore(node, getChildren(node));
        for (int i = 0; i < sortedChildren.size(); i++) {
            MinMaxNode1 child = sortedChildren.get(i);
            if (isTerminalNode(child.board, child.score) || depth >= MinMaxBot1.MAX_DEPTH) {
                node.decision[child.index] = diffScore(child.board);
            } else {
                exploreNextNode(child, depth + 1, alpha, beta);
                node.decision[child.index] = child.getEvaluation();
            }

            if (node.minmax(node.decision[child.index], node.evaluation) != node.evaluation) {
                node.evaluation = node.minmax(node.decision[child.index], node.evaluation);
                updateCategoryScore(node, sortedChildren, i, child);
            }

            if (depth > 0) {
                if (node.alphabeta(node.evaluation, alpha, beta)) {
                    updateCategoryScore(node, sortedChildren, i, child);
                    break;
                }
                alpha = node.alpha(node.evaluation, alpha);
                beta = node.beta(node.evaluation, beta);
            }

        }
        return node;
    }

    private static void updateCategoryScore(MinMaxNode1 node, ArrayList<MinMaxNode1> sortedChildren, int i, MinMaxNode1 child) {
        updateScore(getCategory(node, child), i);
        for (int j = 0; j < i; j++) {
            MinMaxNode1 predecessorChild = sortedChildren.get(j);
            updateScore(getCategory(node, predecessorChild), -1);
        }
    }

    private static void updateScore(String category, int i) {
        if (MinMaxBot1.categories.containsKey(category)) {
            int value = MinMaxBot1.categories.get(category);
            MinMaxBot1.categories.replace(category, value + i);
        } else {
            MinMaxBot1.categories.put(category, i);
        }
    }

    private static boolean isTerminalNode(Board board, int score) {
        return ((score < 0) ||
                (board.getScore(Board.otherPlayer(board.getCurrentPlayer())) >= 25) ||
                (board.getNbSeeds() <= 6));
    }

    private static ArrayList<MinMaxNode1> getChildren(MinMaxNode1 parent) {
        ArrayList<MinMaxNode1> children = new ArrayList<>();
        parent.decision = new double[Board.NB_HOLES];
        for (int i = 0; i < Board.NB_HOLES; i++)
            if (parent.board.getPlayerHoles()[i] != 0) {
                double[] decision = new double[Board.NB_HOLES];
                decision[i] = 1;
                Board copy = (Board) parent.board.clone();
                try {
                    int score = copy.playMoveSimulationScore(copy.getCurrentPlayer(), decision);
                    copy = copy.playMoveSimulationBoard(copy.getCurrentPlayer(), decision);
                    MinMaxNode1 child = parent.getNextNode(copy);
                    child.score = score;
                    child.index = i;
                    children.add(child);
                } catch (InvalidBotException e) {
                    e.printStackTrace();
                }
            }
        return children;
    }

    private static ArrayList<MinMaxNode1> sortChildrenByScore(MinMaxNode1 parent, ArrayList<MinMaxNode1> children) {
        HashMap<MinMaxNode1, Integer> scores = new HashMap<>();

        for (MinMaxNode1 child : children) {
            String cat = getCategory(parent, child);
            int value = 0;
            if (MinMaxBot1.categories.containsKey(cat)) value = MinMaxBot1.categories.get(cat);
            scores.put(child, value);
        }

        Map<MinMaxNode1, Integer> sortedMap = scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
        return new ArrayList<>(sortedMap.keySet());
    }

    private static String getCategory(MinMaxNode1 parent, MinMaxNode1 child) {
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
                    return String.valueOf(j + 6 * lastState[i][j] + 6 * 48 * currentState[i1][j1]);
                }
            }
        }
        return null;
    }

    /**
     * Pire score pour un joueur
     */
    protected abstract double worst();

    /**
     * Initialisation
     */
    protected static void initialize(Board board) {
        //to-do utile de le faire ici ???
        MinMaxNode1.player = board.getCurrentPlayer();
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
