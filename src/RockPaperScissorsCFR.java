import java.util.Random;

/**
 * @author Max Jappert
 */
public class RockPaperScissorsCFR {
    final int numActions = 3;
    int[] possibleActions = new int[numActions];
    // This is basically the reward matrix
    int[][] actionMatrix = new int[numActions][numActions];

    // These should be initialized with 0s
    int[] regretSum = new int[numActions];
    float[] strategySum = new float[numActions];

    int[] opRegretSum = new int[numActions];
    float[] opStrategySum = new float[numActions];

    int nIterations;

    /**
     * Initializes the payoff matrix.
     * @return A 2D-array representing the following matrix:
     * 0  -1  1
     * 1  0  -1
     * -1  1  0
     */
    private int[][] defineActionUtilityMatrix() {
        int[][] m = new int[3][3];

        m[0][0] = 0;
        m[1][1] = 0;
        m[2][2] = 0;
        m[0][1] = -1;
        m[0][2] = 1;
        m[1][0] = 1;
        m[1][2] = -1;
        m[2][0] = -1;
        m[2][1] = 1;

        return m;
    }

    /**
     * Chooses an action (rock, paper of scissors) depending on the strategy. The strategy is represented by a
     * array of probabilities summing to 1.
     * @param possibilities Self explanatory.
     * @param p The probability array. Needs to sum to 1 and have an entry for each possibility.
     * @return Returns one of the entries from the array possibilities with a probability given by the array p.
     */
    private int choice(int[] possibilities, float[] p) {
        assert(possibilities.length == p.length);

        float r = new Random().nextFloat();

        int result = -1;

        if (r < p[0]) {
            result = possibilities[0];
        } else if (r >= p[0] && r < p[0] + p[1]) {
            result = possibilities[1];
        } else if (r >= p[0] + p[1]) {
            result = possibilities[2];
        }

        assert(result != -1);

        return result;

    }

    public RockPaperScissorsCFR() {
        for (int i = 0; i < numActions; i++) {
            possibleActions[i] = i;
        }

        actionMatrix = defineActionUtilityMatrix();
    }

    /**
     *
     * @param regret Total regret for each action.
     * @return An array of probabilities summing to 1.
     */
    public float[] getStrategy(int[] regret) {
        int normalizingValue = 0;
        for (int i = 0; i < numActions; i++) {
            if (regret[i] < 0) {
                regret[i] = 0;
            }

            normalizingValue += regret[i];
        }

        float[] strategy = new float[numActions];

        for (int i = 0; i < numActions; i++) {
            if (normalizingValue > 0) {
                strategy[i] = (float) regret[i] / normalizingValue;
            } else {
                strategy[i] = 1.0f / numActions;
            }
        }

        return strategy;
    }

    /**
     *
     * @param strategy The strategy sum from which the average strategy should be computed.
     * @return The average strategy which corresponds to the approximated Nash equilibrium.
     */
    public float[] getAverageStrategy(float[] strategy) {
        float[] averageStrategy = new float[numActions];
        float normalizingValue = 0;

        for (int i = 0; i < numActions; i++) {
            normalizingValue += strategy[i];
        }

        for (int i = 0; i < numActions; i++) {
            if (normalizingValue > 0) {
                averageStrategy[i] = strategy[i] / normalizingValue;
            } else {
                averageStrategy[i] = 1.0f / numActions;
            }
        }

        return averageStrategy;
    }

    /**
     * @param strategy The given strategy.
     * @return A action chosen considering the given strategy.
     */
    public int getAction(float[] strategy) {
        return choice(possibleActions, strategy);
    }

    /**
     * Computes the payoff given an action set.
     * @param myAction
     * @param opponentAction
     * @return The payoff given an action set.
     */
    public int getReward(int myAction, int opponentAction) {
        return actionMatrix[myAction][opponentAction];
    }

    /**
     * The method where the self-play takes place. This is done by iteratively repeating the steps presented in
     * my paper.
     * @param iterations The number of desired iterations.
     */
    public void train(int iterations) {
        nIterations = iterations;
        for (int i = 0; i < nIterations; i++) {
            float[] myStrategy = getStrategy(regretSum);
            float[] opStrategy = getStrategy(opRegretSum);

            strategySum[0] += myStrategy[0];
            strategySum[1] += myStrategy[1];
            strategySum[2] += myStrategy[2];

            opStrategySum[0] += opStrategy[0];
            opStrategySum[1] += opStrategy[1];
            opStrategySum[2] += opStrategy[2];

            int myAction = getAction(myStrategy);
            int opAction = getAction(opStrategy);

            int myReward = getReward(myAction, opAction);
            int opReward = getReward(opAction, myAction);

            for (int action = 0; action < numActions; action++) {
                int myRegret = getReward(action, opAction) - myReward;
                int opRegret = getReward(action, myAction) - opReward;

                regretSum[action] += myRegret;
                opRegretSum[action] += opRegret;
            }
        }
    }

    public void printStrategies() {
        float myTotalStrategySum = strategySum[0] + strategySum[1] + strategySum[2];

        float[] averageStrategy = new float[3];

        averageStrategy[0] = strategySum[0] / myTotalStrategySum;
        averageStrategy[1] = strategySum[1] / myTotalStrategySum;
        averageStrategy[2] = strategySum[2] / myTotalStrategySum;

        System.out.println("######################################\n");
        System.out.println("The following probabilities for playing each action denote the approximated Nash equilibrium for rock-paper-scissors after N = " + nIterations + " training iterations:\n");

        System.out.println("Rock:     " + averageStrategy[0]);
        System.out.println("Paper:    " + averageStrategy[1]);
        System.out.println("Scissors: " + averageStrategy[2]);

        System.out.println("\n######################################\n\n");

    }
}
