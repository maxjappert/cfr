# Programming Project
#### Seminar: Theory and Algorithms of Puzzles and Games
#### Subject: Poker
#### Author: Max Jappert

This project centers around an implementation of Counterfactual Regret Minimization (CFR),
an algorithm which computes an approximated Nash equilibrium for sequential imperfect-information
games like Poker.

This program includes implementations for rock-paper-scissors and Kuhn poker, the latter being a 
toy game which includes the basic concepts of poker.

On starting the program, you'll be greeted with the following prompt: 

```
Welcome to my programming project on poker. 
There are many things you can do with this program.
Please choose one of them from the following list:

[0] Approximate the Nash equilibrium for rock-paper-scissors.
[1] Approximate the Nash equilibrium for Kuhn poker.
[2] Approximate the Nash equilibrium for Kuhn poker using Monte Carlo sampling.
[3] Compare CFR and MCCFR in terms of computational speed.
[4] Compute the average payoff the computed optimal strategy achieves against an untrained opponent AI.
[5] Play Kuhn poker against an AI playing with the computed optimal strategy.
```

An option can be chosen by typing in the corresponding ID (e.g., 3) and pressing enter.

## Options

Option [0] computes the Nash equilibrium for rock-paper-scissors
using CFR and then prints the resulting strategy to console. Since
a strategy is a probability distribution over the set of possible
actions at a given decision point, and r-p-s only consists of one decision
point, the approximated Nash equilibrium consists of three floating point
numbers which sum to 1. Each of the floating point numbers corresponds to
one of the three possible actions.

Option [1] computes the Nash equilibrium for Kuhn poker. Since Kuhn poker
is a sequential game, there are multiple decision points, each corresponding to
exactly one information set. The computed strategy is printed to screen as
a table, the columns denoting the two possible actions (check, bet) and the 
rows denoting the information sets at which the actions can be chosen.
the information sets are uniquely denoted by a string consisting of the card which is
dealt and the history which proceeds the set being visited. The floating point
values mark the computed probabilities of playing the action at the information
set, whereby the numbers in each row sum to 1.

Option [2] does the same as option [1], yet it uses an optimized version of CFR,
namely Monte Carlo CFR, which doesn't traverse the entire game tree per iteration
and rather samples paths. It computes a slightly different strategy, yet Nash equilibria
are not unique, so this is not problematic.

Option [3] compares the computational speed of the CFR and its optimized counterpart.
It does this by training both implementations 100 times for 1000 iterations each, and
comparing how long this takes for both. 

Option [4] has an AI playing with the computed optimal strategy (player 1) play against an
AI playing with an untrained strategy (player 2). The user is then asked how many rounds they
should play against each other. The output consists of the total payoff
achieved by player 1 (i.e., the AI playing with the optimal strategy),
as well as the total amount of wins with the corresponding win rate in %.

And finally, option [5] lets the user play Kuhn poker against an AI playing with the computed
optimal strategy.

# Implementation

The project was implemented in Java. The code is well documented, so feel free to have a look.