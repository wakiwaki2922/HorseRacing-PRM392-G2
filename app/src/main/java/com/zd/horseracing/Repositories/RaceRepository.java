package com.zd.horseracing.Repositories;

import com.zd.horseracing.Models.HorseBet;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing race-related data, including the user's balance,
 * current bets, and the total bet amount.  This class acts as a data source,
 * abstracting the data access logic from the ViewModel.
 */
public class RaceRepository {
    private static final int INITIAL_BALANCE = 1000;
    private int balance;
    private List<HorseBet> currentBets;
    private int totalBetAmount;

    /**
     * Constructor for the RaceRepository. Initializes the balance, current bets, and total bet amount.
     */
    public RaceRepository() {
        this.balance = INITIAL_BALANCE;
        this.currentBets = new ArrayList<>();
        this.totalBetAmount = 0;
    }

    /**
     * Gets the current balance.
     *
     * @return The current balance.
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Updates the balance by adding the given amount.
     *
     * @param amount The amount to add to the balance (can be negative for deductions).
     */
    public void updateBalance(int amount) {
        this.balance += amount;
    }

    /**
     * Gets the list of current bets.
     *
     * @return The list of {@link HorseBet} objects representing the current bets.
     */
    public List<HorseBet> getCurrentBets() {
        return currentBets;
    }

    /**
     * Sets the current bets and recalculates the total bet amount.
     *
     * @param bets The list of {@link HorseBet} objects to set as the current bets.
     */
    public void setCurrentBets(List<HorseBet> bets) {
        this.currentBets = bets;
        calculateTotalBetAmount();
    }

    /**
     * Clears the current bets and resets the total bet amount to 0.
     */
    public void clearBets() {
        currentBets.clear();
        totalBetAmount = 0;
    }

    /**
     * Gets the total bet amount.
     *
     * @return The total bet amount.
     */
    public int getTotalBetAmount() {
        return totalBetAmount;
    }

    /**
     * Calculates the total bet amount based on the current bets.  Iterates through the
     * `currentBets` list and sums the bet amounts.
     */
    private void calculateTotalBetAmount() {
        totalBetAmount = 0;
        for (HorseBet bet : currentBets) {
            totalBetAmount += bet.getBetAmount();
        }
    }
}