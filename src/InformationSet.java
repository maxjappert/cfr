/**
 * This class represents an information set.
 * @author Max Jappert
 */
public class InformationSet {
    String key;
    float[] regretSum;
    float[] strategySum;
    float[] strategy;
    float reachProb;
    float reachProbSum;

    /**
     * Initializes the given information set.
     * @param key The key which describes the information set. It consists of the dealt card (J, Q or K) and the
     *            history preceding the information set being visited.
     */
    public InformationSet(String key) {
        this.key = key;
        regretSum = new float[KuhnPokerCFR.numActions];
        strategySum = new float[KuhnPokerCFR.numActions];
        strategy = new float[KuhnPokerCFR.numActions];
        reachProb = 0;
        reachProbSum = 0;

        for (int i = 0; i < KuhnPokerCFR.numActions; i++) {
            strategy[i] = 1.0f / KuhnPokerCFR.numActions;
        }
    }

    /**
     * Computes the next strategy by calling calcStrategy(). The strategySum is updated as in Zinkevic et al. (2007)
     */
    public void nextStrategy() {
        strategySum[0] += reachProb * strategy[0];
        strategySum[1] += reachProb * strategy[1];

        strategy = calcStrategy();

        reachProbSum += reachProb;
        reachProb = 0;
    }

    /**
     * Computes the strategy for the next round by dividing the cumulative regret for each action by the total
     * cumulative regret. If the cumulative regret is 0, the strategy is an equal probability distribution.
     * @return The strategy for the next round.
     */
    public float[] calcStrategy() {

        regretSum[0] = Math.max(regretSum[0], 0);
        regretSum[1] = Math.max(regretSum[1], 0);

        float normalizingValue = 0;

        float[] temp_sum = new float[2];

        for (int i = 0; i < KuhnPokerCFR.numActions; i++) {
            temp_sum[i] = Math.max(regretSum[i], 0);
            normalizingValue += temp_sum[i];
        }

        for (int i = 0; i < KuhnPokerCFR.numActions; i++) {
            if (normalizingValue != 0) {
                strategy[i] = temp_sum[i] / normalizingValue;
            } else {
                strategy[i] = 1.0f / KuhnPokerCFR.numActions;
            }
        }

        return strategy;
    }

    /**
     * The average strategy computed as in Zinkevic et al. (2007)
     * @return The approximated Nash equilibrium for Kuhn poker.
     */
    public float[] getAverageStrategy() {
        float[] averageStrategy = new float[2];

        // Equation as in Zinkevic et al. (2007)
        averageStrategy[0] = strategySum[0] / reachProbSum;
        averageStrategy[1] = strategySum[1] / reachProbSum;

        // Normalize the average strategy
        float totalStrategySum = averageStrategy[0] + averageStrategy[1];
        averageStrategy[0] = averageStrategy[0] / totalStrategySum;
        averageStrategy[1] = averageStrategy[1] / totalStrategySum;

        return averageStrategy;
    }

    public float getProbability(int action, float padding, float epsilon) {
        float[] probability = new float[2];
        if (reachProbSum != 0) {
            probability[0] = strategySum[0] / reachProbSum + 0.2f;
            probability[1] = strategySum[1] / reachProbSum + 0.2f;
        } else {
            probability = strategySum;
        }

        float normalizingSum = probability[0] + probability[1];

        // The padding is used, so that if, in the beginning of the game, the probability for playing an action at this
        // information set is very low, there will still be a realistic chance of the action being chosen. Later on
        // in the game this value of the padding will become negligible, since it doesn't increase together with the strategy sum,
        // therefore becomes increasingly small in comparison.
        if (normalizingSum > 0) {
            probability[0] = (probability[0] + padding) / (normalizingSum + padding);
            probability[1] = (probability[1] + padding) / (normalizingSum + padding);
        } else {
            probability[0] = (1 + padding) / (2 + padding);
            probability[1] = (1 + padding) / (2 + padding);
        }

        return Math.max(probability[action], epsilon);
    }
}
