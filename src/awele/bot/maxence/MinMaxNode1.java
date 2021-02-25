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
    private static int maxDepth;
    private double evaluation;

    private Board board;
    private int score;
    private int index;


    private double[] decision;


    /**
     * Constructeur...
     *
     * @param board L'état de la grille de jeu
     * @param depth La profondeur du noeud
     * @param alpha Le seuil pour la coupe alpha
     * @param beta  Le seuil pour la coupe beta
     */
    public MinMaxNode1(Board board, int depth, double alpha, double beta, boolean viensDeTriFils) {
        this.board = board;

        if (!viensDeTriFils){
            if (MinMaxBot1.depthMAx < depth) MinMaxBot1.depthMAx = depth;
            MinMaxBot1.depth = depth;
            MinMaxBot1.nodes++;
            MinMaxBot1.budget--;

            this.decision = new double[Board.NB_HOLES];
            this.evaluation = this.worst();
            ArrayList<MinMaxNode1> sortedFils = TriFils(this, getFils(this, depth, alpha, beta));
            for (MinMaxNode1 fils: sortedFils) {
                if (isTerminalNode(fils.board, fils.score) || depth >= MinMaxBot1.MAX_DEPTH) {
                    this.decision[fils.index] = this.diffScore(fils.board);
                } else {
                    MinMaxNode1 child = this.getNextNode(fils.board, depth + 1, alpha, beta, false);
                    this.decision[fils.index] = child.getEvaluation();
                }

                this.evaluation = this.minmax(this.decision[fils.index], this.evaluation);

                if (depth > 0) {
                    if (this.alphabeta(this.evaluation, alpha, beta)) {
                        break;
                    }
                    alpha = this.alpha(this.evaluation, alpha);
                    beta = this.beta(this.evaluation, beta);
                }

            }
        }



//        for (int i = 0; i < Board.NB_HOLES; i++)
//            if (board.getPlayerHoles()[i] != 0) {
//                double[] decision = new double[Board.NB_HOLES];
//                decision[i] = 1;
//                Board copy = (Board) board.clone();
//                try {
//                    int score = copy.playMoveSimulationScore(copy.getCurrentPlayer(), decision);
//                    copy = copy.playMoveSimulationBoard(copy.getCurrentPlayer(), decision);
//
//                    if (isTerminalNode(copy, score) || depth >= MinMaxBot1.MAX_DEPTH) {
//                        this.decision[i] = this.diffScore(copy);
//                    } else {
//                        MinMaxNode1 child = this.getNextNode(copy, depth, alpha, beta);
//                        this.decision[i] = child.getEvaluation();
//                    }
//
//
//                    this.evaluation = this.minmax(this.decision[i], this.evaluation);
//                    /* Coupe alpha-beta */
//                    if (depth > 0) {
//                        if (this.alphabeta(this.evaluation, alpha, beta)) {
//                            break;
//                        }
//                        alpha = this.alpha(this.evaluation, alpha);
//                        beta = this.beta(this.evaluation, beta);
//                    }
//                } catch (InvalidBotException e) {
//                    this.decision[i] = 0;
//                }
//            }
    }


    boolean isTerminalNode(Board board, int score) {
        return ((score < 0) ||
                (board.getScore(Board.otherPlayer(board.getCurrentPlayer())) >= 25) ||
                (board.getNbSeeds() <= 6));
    }

    private ArrayList<MinMaxNode1> getFils(MinMaxNode1 node, int depth, double alpha, double beta) {
        ArrayList<MinMaxNode1> fils = new ArrayList<>();
        this.decision = new double[Board.NB_HOLES];
        for (int i = 0; i < Board.NB_HOLES; i++)
            if (board.getPlayerHoles()[i] != 0) {
                double[] decision = new double[Board.NB_HOLES];
                decision[i] = 1;
                Board copy = (Board) board.clone();
                try {
                    int score = copy.playMoveSimulationScore(copy.getCurrentPlayer(), decision);
                    copy = copy.playMoveSimulationBoard(copy.getCurrentPlayer(), decision);
                    MinMaxNode1 f = this.getNextNode(copy, depth + 1, alpha, beta, true);
                    f.score = score;
                    f.index = i;
                    fils.add(f);
                } catch (InvalidBotException e) {
                    e.printStackTrace();
                }
            }
        return fils;
    }

    private ArrayList<MinMaxNode1> TriFils(MinMaxNode1 parent, ArrayList<MinMaxNode1> fils) {
        HashMap<MinMaxNode1, Integer> scores = new HashMap<>();

        for (MinMaxNode1 f : fils) {
            String cat = f.getCategorie(parent, f);
            int value = 0;
            if (MinMaxBot1.categorie.containsKey(cat)) value = MinMaxBot1.categorie.get(cat);
            scores.put(f, value);
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




//-+------------------------------------------------------------------------------------------------------------


    boolean estTermine(Board board, int score, int depth) {
        return ((score < 0) ||
                (board.getScore(Board.otherPlayer(board.getCurrentPlayer())) >= 25) ||
                (board.getNbSeeds() <= 6 || depth == MinMaxBot1.MAX_DEPTH));
    }


    private String getCategorie(MinMaxNode1 pere, MinMaxNode1 fils) {
        int[][] lastState = new int[2][Board.NB_HOLES];
        int[][] currentState = new int[2][Board.NB_HOLES];

        lastState[0] = pere.board.getPlayerHoles();
        lastState[1] = pere.board.getOpponentHoles();

        currentState[0] = fils.board.getOpponentHoles();
        currentState[1] = fils.board.getPlayerHoles();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < Board.NB_HOLES; j++) {
                if (currentState[i][j] == 0 && lastState[i][j] != 0) {
                    int trouDepart = j;
                    int graineDepart = lastState[i][j];
                    int graineArrive = currentState[i][j];
                    int c = trouDepart + 6 * graineDepart + 6 * 48 * graineArrive;
                    return String.valueOf(c);
                }
            }
        }
        return null;
    }


    private double alphaBetaTriCoup(MinMaxNode1 node, double alpha, double beta, boolean isMax, int depth, Board board, int score) {
        double value;
        if (estTermine(board, score, depth)) return node.getEvaluation();
        if (isMax) {
            value = Double.MIN_VALUE;
            ArrayList<MinMaxNode1> fils = TriFils(node);
            for (int i = 0; i < fils.size(); i++) {
                MinMaxNode1 f = fils.get(i);
                double res = alphaBetaTriCoup(f, alpha, beta, false, depth + 1, board, score);
                if (res > value) {
                    value = res;
                    //updateScore(getCategorie(node, f), i);
                    for (int j = 0; j < i; j++) {
                        MinMaxNode1 fPrime = fils.get(j);
                    //    updateScore(getCategorie(node, fPrime), -1);
                    }
                    return value;
                }
                if (value > alpha) {
                    alpha = value;
                }
            }
        } else {
            value = Double.MAX_VALUE;
            //
        }
        return value;
    }

    private ArrayList<MinMaxNode1> TriFils(MinMaxNode1 node) {
        //ArrayList<MinMaxNode1> fils = fils du node ;
        //  for (MinMaxNode1 f: fils) {
        //    int score =  scoreMinMaxBot1.categorie.get(getCategorie(node,f));
        //  }

        return null;
    }




    /**
     * Pire score pour un joueur
     */
    protected abstract double worst();

    /**
     * Initialisation
     */
    protected static void initialize(Board board, int maxDepth) {
        MinMaxNode1.maxDepth = maxDepth;
        MinMaxNode1.player = board.getCurrentPlayer();
    }

    private int diffScore(Board board) {
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

    /**
     * Retourne un noeud (MinNode ou MaxNode) du niveau suivant
     *
     * @param board L'état de la grille de jeu
     * @param depth La profondeur du noeud
     * @param alpha Le seuil pour la coupe alpha
     * @param beta  Le seuil pour la coupe beta
     * @return Un noeud (MinNode ou MaxNode) du niveau suivant
     */
    protected abstract MinMaxNode1 getNextNode(Board board, int depth, double alpha, double beta, boolean viensDeTriFils );

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
