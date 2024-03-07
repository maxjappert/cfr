import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {


        System.out.println("""
                Welcome to my programming project on poker.
                There are many things you can do with this program.
                Please choose one of them from the following list:
                """);

        while(true) {

            System.out.print("""
                    [0] Approximate the Nash equilibrium for rock-paper-scissors.
                    [1] Approximate the Nash equilibrium for Kuhn poker.
                    [2] Approximate the Nash equilibrium for Kuhn poker using Monte Carlo sampling.
                    [3] Compare CFR and MCCFR in terms of computational speed.
                    [4] Compute the average payoff the computed optimal strategy achieves against a untrained opponent AI.
                    [5] Play Kuhn poker against an AI playing with the computed optimal strategy.
                    \s""");

            Scanner sc = new Scanner(System.in);

            int input = sc.nextInt();

            executeAction(input);

            System.out.println("Anthing else?");
        }

    }

    private static void executeAction (int input) throws IOException {
        Scanner sc = new Scanner(System.in);

        if (!(input >= 0 && input <= 5)) {
            executeAction(input);
        }

        if (input == 0) {
            RockPaperScissorsCFR rps = new RockPaperScissorsCFR();

            rps.train(100000);

            rps.printStrategies();
        } else if (input == 1) {
            KuhnPokerCFR kp = new KuhnPokerCFR(false);

            kp.train(10000);

            kp.printStrategies();
        } else if (input == 2) {
            KuhnPokerCFR kp = new KuhnPokerCFR(true);

            kp.train(2000);

            kp.printStrategies();
        } else if (input == 3) {
            long cfrStartTime = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                KuhnPokerCFR kp = new KuhnPokerCFR(false);

                kp.train(1000);
            }
            long cfrEndTime = System.currentTimeMillis();

            long mccfrStartTime = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                KuhnPokerCFR mckp = new KuhnPokerCFR(true);

                mckp.train(1000);
            }
            long mccfrEndTime = System.currentTimeMillis();

            System.out.println("CFR average time until convergence:   " + ((cfrEndTime - cfrStartTime) / 100) + "ms");

            System.out.println("MCCFR average time until convergence: " + ((mccfrEndTime - mccfrStartTime) / 100) + "ms");
        } else if (input == 4) {
            System.out.print("How many rounds should the two AIs play against each other? ");

            int rounds = sc.nextInt();

            KuhnPokerCFR kp = new KuhnPokerCFR(false);
            kp.train(10000);

            PlayKuhnPoker pkp = new PlayKuhnPoker(kp.iMap, false);

            pkp.play(rounds);
        } else if (input == 5) {
            KuhnPokerCFR kp = new KuhnPokerCFR(false);
            kp.train(10000);

            PlayKuhnPoker pkp = new PlayKuhnPoker(kp.iMap, true);

            pkp.play(10000);
        }
    }
}

