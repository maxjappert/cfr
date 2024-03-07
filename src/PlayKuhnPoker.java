//import java.io.File;
//import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Class which deals with playing Kuhn poker as a game, either between a human agent and an AI or between two AIs
 */
public class PlayKuhnPoker {
    Map<String, InformationSet> iMapTrained;
    Map<String, InformationSet> iMapUntrained;
    int card;
    int opCard;

    char cardChar;
    char opCardChar;

    String history;

    float totalPayoffP1;

    boolean p1starts;

    boolean p1bet;
    boolean p2bet;

    boolean interactive;

    int p1wins = 0;

//    FileWriter fw;

    public PlayKuhnPoker(Map<String, InformationSet> iMap, boolean interactive) throws IOException {
        this.iMapTrained = iMap;
        this.interactive = interactive;

        totalPayoffP1 = 0;

        p1starts = true;

        iMapUntrained = new HashMap<>();
//
//        File file = new File("plots/winRate.txt");
//
//        file.createNewFile();
//
//        fw = new FileWriter(file);

        for (InformationSet iset : this.iMapTrained.values()) {
            iMapUntrained.put(iset.key, new InformationSet(iset.key));
        }
    }

    /**
     * Start playing the game.
     * @param nRounds Amount of rounds to be played.
     */
    public void play(int nRounds) throws IOException {
        for (int i = 1; i <= nRounds; i++) {
            System.out.println("Round " + i + ", player " + (p1starts ? 1 : 2) + " starts:");
            totalPayoffP1 += playRound();

//            fw.write(totalPayoffP1 / i + "\n");
        }

//        fw.flush();
//        fw.close();

        System.out.println("Total payoff for P1: " + totalPayoffP1);
        System.out.println("Total wins for P1: " + p1wins + "/" + nRounds);
        System.out.printf("Win rate for P1: %.2f%c \n\n", (float)p1wins / nRounds * 100, '%');
    }

    /**
     * Play a single round.
     * @return Player 1's payoff in this round.
     */
    private float playRound() throws IOException {

        // First the cards are "dealt". The are represented both as strings and as integers.
        card = -1;
        opCard = -1;

        while (card == opCard) {
            card = new Random().nextInt(3);
            opCard = new Random().nextInt(3);
        }

        if (card == 0) {
            cardChar = 'J';
        } else if (card == 1) {
            cardChar = 'Q';
        } else if (card == 2) {
            cardChar = 'K';
        }

        if (opCard == 0) {
            opCardChar = 'J';
        } else if (opCard == 1) {
            opCardChar = 'Q';
        } else if (opCard == 2) {
            opCardChar = 'K';
        }

        System.out.println("\nYou are dealt a " + cardChar);
        // For debugging the opponents card can also be printed.
        //System.out.println("The opponent is dealt a " + opCardChar);

        history = "rr";

        // calls the first move
        float payoff = firstMove(interactive);

        System.out.println("Round finished");
        System.out.println("\n##################################");

        return payoff;
    }

    /**
     * Coordinates the first move.
     * @param interactive Will the round be played between a human and an AI?
     * @return Player 1's payoff.
     */
    private float firstMove(boolean interactive) {
        p1bet = false;
        p2bet = false;

        if (interactive) {
            if (p1starts) {
                p1starts = false;
                return p1Move();
            } else {
                p1starts = true;
                return aiMove(false, true);
            }
        } else {
            if (p1starts) {
                p1starts = false;
                return aiMove(true, true);
            } else {
                p1starts = true;
                return aiMove(false, false);
            }
        }
    }

    /**
     * The human player makes a move.
     * @return Player 1's payoff.
     */
    private float p1Move() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Check or bet? [c/b] ");

        String move = sc.next();

        if (move.equalsIgnoreCase("c")) {
            history = history + "c";
            System.out.println("You have checked.");
        } else if (move.equalsIgnoreCase("b")) {
            history = history + "b";
            System.out.println("You have bet.");
            p1bet = true;
        } else {
            System.out.println("The input should be either 'c' or 'b'.");
            p1Move();
        }

        if (KuhnPokerCFR.isTerminal(history)) {
            return calculatePayoff();
        }

        return aiMove(false, true);
    }

    /**
     * The AI makes a move.
     * @param p1 Is the AI player 1?
     * @param trained Is the AI trained?
     * @return Player 1's payoff.
     */
    private float aiMove(boolean p1, boolean trained) {

        InformationSet currentSet;
        if (trained) {
            currentSet = iMapTrained.get(opCardChar + " " + history);
        } else {
            currentSet = iMapUntrained.get(opCardChar + " " + history);
        }

        float[] strategy = currentSet.getAverageStrategy();

        if (strategy[0] < 0 && strategy[1] > 1) {
            System.out.println("Error, error!!");
        } else if (strategy[0] <= new Random().nextFloat()) {
            history = history + "c";
            System.out.println("Your opponent has checked");
        } else if (strategy[1] <= 1) {
            history = history + "b";
            System.out.println("Your opponent has bet");
            if (p1) {
                p1bet = true;
            } else {
                p2bet = true;
            }
        }

        if (KuhnPokerCFR.isTerminal(history)) {
            return calculatePayoff();
        }

        if (interactive) {
            return p1Move();
        } else if (trained) {
            return aiMove(!p1, false);
        } else {
            return aiMove(!p1, true);
        }
    }

    /**
     * Calculates player 1's payoff given the history.
     * @return Player 1's payoff.
     */
    float calculatePayoff() {
        boolean showdown = false;

        if (history.endsWith("cc") || history.endsWith("bb")) {
            System.out.println("Showdown! Player 1 has " + cardChar + " and player 2 has " + opCardChar + ", so the" +
                    " round goes to player " + (card < opCard ? 2 : 1));
            showdown = true;
        }

        float payoff = 0;

        if (showdown && p1bet && p2bet) {
            payoff = (card > opCard) ? 2 : -2;
        } else if (showdown && !p1bet && !p2bet) {
            payoff = (card > opCard) ? 1 : -1;
        } else if (p1bet && !p2bet) {
            payoff = 1;
        } else if (!p1bet && p2bet) {
            payoff = -1;
        } else {
            System.out.println("Something went wrong when calculating the payoffs.");
        }

        if (payoff > 0) {
            p1wins++;
        }

        System.out.println("Player 1 has payoff " + payoff);
        System.out.println("Player 2 has payoff " + -payoff);

        return payoff;
    }
}
