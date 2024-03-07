//import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * A class containing all the methods needed for approximating a Nash equilibrium for Kuhn poker.
 * @author Max Jappert
 */
public class KuhnPokerCFR {
    static int numActions = 2;
    static int numCards = 3;
    Map<String, InformationSet> iMap;
    int nIterations;
    boolean mccfr;

    int nodesVisited = 0;
    
//    FileWriter fwc;
//    FileWriter fwb;


    public KuhnPokerCFR(boolean mccfr) throws IOException {
//        File f1 = new File("plots/Krr_check.txt");
//        File f2 = new File("plots/Krr_bet.txt");
//
//        f1.createNewFile();
//        f2.createNewFile();

//        fwc = new FileWriter(f1);
//        fwb = new FileWriter(f2);

        this.mccfr = mccfr;
    }

    /**
     * As the starting point of self-play, this method must be called to start the training (self-play) process.
     */
    public void train(int iterations) throws IOException {
        iMap = new HashMap<>();
        nIterations = iterations;

        for (int i = 0; i < nIterations; i++) {

            // Starts traversing the tree by calling the cfr(...) method on an empty history.
            cfr(iMap, "", -1, -1, 1, 1, 1);

            // For each information set, the strategy for the next round \sigma^T+1 is computed.
            for (Object o : iMap.values().toArray()) {
                InformationSet is = (InformationSet) o;
                is.nextStrategy();

                if (mccfr && i == nIterations / 2) {
                    is.strategySum[0] = 0;
                    is.strategySum[1] = 0;
                }

//                if (is.key.equals("K rr")) {
//                    fwc.write(is.strategy[0] + "\n");
//                    fwb.write(is.strategy[1] + "\n");
//                }
            }
        }
//        fwc.flush();
//        fwb.flush();
//
//        fwb.close();
//        fwc.close();
    }

    /**
     * Method to traverse the tree by being called recursively.
     * @param iMap The map containing the information sets as values. The keys are strings, containing information regarding
     *             the dealt card and the history preceding the set.
     * @param history The history representing the current point within the game tree.
     * @param card1 Player 1's card.
     * @param card2 Player 2's card.
     * @param pr1 Player 1's contribution to the reach probability of the currently visited node.
     * @param pr2 Player 2's contribution to the reach probability of the currently visited node.
     * @param prC The chance node's contribution to the reach probability of the currently visited node.
     * @return Utility of the information set, i.e. how profitable it is to have visited this set.
     */
    private float cfr(Map<String, InformationSet> iMap, String history, int card1, int card2, float pr1, float pr2, float prC) {
        nodesVisited++;

        if (isChanceNode(history)) {
            return chanceUtil(iMap);
        }

        if (isTerminal(history)) {
            return terminalUtil(history, card1, card2);
        }

        int n = history.length();
        boolean isPlayer1 = n % 2 == 0;

        InformationSet infoSet = getInfoSet(iMap, isPlayer1 ? card1 : card2, history);

        float[] strategy = infoSet.strategy;

        // If monte carlo cfr is activated, then the regret is only calculated for one player at a time, in an alternating fashion.
        // This condition checks if mccfr is activated and if the regret should therefore be calculated for a player.
        if (isPlayer1) {
            infoSet.reachProb += pr1;
        } else {
            infoSet.reachProb += pr2;
        }

        float[] actionUtils = new float[numActions];

        for (int i = 0; i < numActions; i++) {

            float probability;

            // If we're using monte carlo sampling, then we only traverse a given branch with a certain probability,
            // i.e. we only sample the tree instead of fully traversing it. If we don't use sampling, then we traverse
            // the entire tree for every iteration, i.e. we traverse a given branch with a probability of 1.
            if (mccfr) {
                probability = infoSet.getProbability(i, 0, 0.05f);
            } else {
                probability = 1;
            }

            if (new Random().nextFloat() < probability) {
                String nextHistory = history + new char[]{'c', 'b'}[i];

                if (isPlayer1) {
                    actionUtils[i] = -1 * cfr(iMap, nextHistory, card1, card2, pr1 * strategy[i], pr2, prC);
                } else {
                    actionUtils[i] = -1 * cfr(iMap, nextHistory, card1, card2, pr1, pr2 * strategy[i], prC);
                }
            } else {
                actionUtils[i] = 0;
            }
        }

        float util = actionUtils[0] * strategy[0] + actionUtils[1] * strategy[1];
        float[] regrets = new float[2];
        regrets[0] = actionUtils[0] - util;
        regrets[1] = actionUtils[1] - util;

        if (isPlayer1) {
            infoSet.regretSum[0] += Math.max(pr2 * prC * regrets[0], 0);
            infoSet.regretSum[1] += Math.max(pr2 * prC * regrets[1], 0);
            infoSet.calcStrategy();
        } else {
            infoSet.regretSum[0] += Math.max(pr1 * prC * regrets[0], 0);
            infoSet.regretSum[1] += Math.max(pr1 * prC * regrets[1], 0);
        }

        return util;
    }

    /**
     * If the history is an empty string, then the given node is trivially a chance node
     * @param history The history descibing the node
     * @return Is the node with the given history a chance node?
     */
    private boolean isChanceNode(String history) {
        return history.equals("");
    }

    /**
     * This method is called when the traversal starts at the chance nodes. It starts the traversal of all information
     * sets by calling the cfr function for each decision node on the level below it.
     * @param iMap The map of all the information sets
     * @return The average payoff from choosing all six decision nodes.
     */
    private float chanceUtil(Map<String, InformationSet> iMap) {
        float expectedValue = 0;
        int nPossibilities = 6;
        for(int i = 0; i < numCards; i++) {
            for (int j = 0; j < numCards; j++) {
                if (i != j) {
                    expectedValue += cfr(iMap, "rr", i, j, 1, 1, 1.0f/nPossibilities);
                }
            }
        }
        return expectedValue / nPossibilities;
    }

    /**
     * Checks if the given history is a terminal node. This is done by hard coding the history describing each terminal node.
     * @param history History whose being a terminal node is decided.
     * @return True if the given history is a terminal node.
     */
    public static boolean isTerminal(String history) {
        return history.equals("rrcc") || history.equals("rrcbc") || history.equals("rrcbb") || history.equals("rrbc") || history.equals("rrbb");
    }

    /**
     * Method to determine the payoff at the given terminal node.
     * @param history Describes the f
     * @param card1 Player 1's card
     * @param card2 Player 2's card
     * @return Payoff
     */
    public static int terminalUtil(String history, int card1, int card2) {
        int n = history.length();
        int cardPlayer = n % 2 == 0 ? card1 : card2;
        int cardOpponent = n % 2 == 0 ? card2 : card1;

        // one player wins, because they bet and the opponent checked
        if (history.equals("rrcbc") || history.equals("rrbc")) {
            return 1;
        // showdown where none have bet
        } else if (history.equals("rrcc")) {
            return cardPlayer > cardOpponent ? 1 : -1;
        }

        // showdown where both have bet
        assert(history.equals("rrcbb") || history.equals("rrbb"));

        return cardPlayer > cardOpponent ? 2 : -2;
    }

    /**
     * @param card Integer in {0, 1, 2} describing one of three cards.
     * @return The String representing the letter representing the card.
     */
    public String cardString(int card) {
        if (card == 0) {
            return "J";
        } else if (card == 1) {
            return "Q";
        } else if (card == 2) {
            return "K";
        } else {
            System.out.println("Error: Illegal card number");
            return "";
        }
    }

    /**
     *
     * @param infoMap The map with all the information sets as values and the strings representing those information
     *                sets as keys.
     * @param card Card which, together with the history, describes the information sets.
     * @param history History which, together with a given card, describes an information set.
     * @return The information set object which is described by the given history and card.
     */
    InformationSet getInfoSet(Map<String, InformationSet> infoMap, int card, String history) {
        String key = cardString(card) + " " + history;

        if (!infoMap.containsKey(key)) {
            InformationSet infoSet = new InformationSet(key);
            infoMap.put(key, infoSet);
            return infoSet;
        }

        return infoMap.get(key);
    }

    /**
     * This method simply prints the approximated Nash equilibrium in a nicely formatted way.
     * As can be seen in my paper, the approximated Nash equilibrium consists of a strategy set
     * with the average strategy for each player.
     */
    public void printStrategies() {
        System.out.println("######################################\n");

        System.out.println(nodesVisited + " nodes visited.");

        System.out.println("The following is the approximated Nash equilibrium for Kuhn Poker after N = " + nIterations + " iterations.\n" +
                "The rows each represent an information set, described by the dealt card with the history preceding the information set,\n" +
                "while the columns represent one of the two possible actions, bet and check. The entries denote the probability of\n" +
                "playing the given action at the given information set.\n");

        System.out.println("Player 1 strategy:");
        System.out.println("\n         Check  Bet");

        for (InformationSet is : iMap.values()) {
            // the information set involves a choice by player 1 if the length of the string is
            // divisible by 2

            float[] s = is.getAverageStrategy();

            if (is.key.length() % 2 == 0) {

                System.out.print(is.key);

                // This block simply manages the formatting
                if (is.key.length() == 4) {
                    System.out.printf(":   [%1.2f", s[0]);
                    System.out.print(String.format(", %1.2f]", is.strategy[1]) + "\n");
                } else if (is.key.length() == 6) {
                    System.out.printf(": [%1.2f", s[0]);
                    System.out.print(String.format(", %1.2f]", is.strategy[1]) + "\n");
                } else {
                    System.out.println("Format error");
                }
            }
        }

        System.out.println("\nPlayer 2 strategy:");
        System.out.println("\n         Check  Bet");
        for (InformationSet is : iMap.values()) {

            float[] s = is.getAverageStrategy();

            // the information set involves a choice by player 2 if the length of the string is
            // divisible by 2
            if (is.key.length() % 2 != 0) {
                System.out.print(is.key);
                System.out.printf(":  [%1.2f", s[0]);
                System.out.print(String.format(", %1.2f]", s[1]) + "\n");

            }
        }
        System.out.println("\n######################################\n\n");

    }
}
