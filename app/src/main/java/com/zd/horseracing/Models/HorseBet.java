package com.zd.horseracing.Models;

/**
 * Represents a bet placed on a specific horse.  Contains the horse number and the bet amount.
 * This is a simple data class (POJO - Plain Old Java Object).
 */
public class HorseBet {
    private int horseNumber;
    private int betAmount;

    /**
     * Constructor for the HorseBet class.
     *
     * @param horseNumber The number of the horse the bet is placed on (1-based index).
     * @param betAmount   The amount of the bet.
     */
    public HorseBet(int horseNumber, int betAmount) {
        this.horseNumber = horseNumber;
        this.betAmount = betAmount;
    }

    /**
     * Gets the horse number.
     *
     * @return The horse number.
     */
    public int getHorseNumber() {
        return horseNumber;
    }

    /**
     * Gets the bet amount.
     *
     * @return The bet amount.
     */
    public int getBetAmount() {
        return betAmount;
    }
}