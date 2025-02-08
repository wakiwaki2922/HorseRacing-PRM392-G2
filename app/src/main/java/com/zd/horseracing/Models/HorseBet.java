package com.zd.horseracing.Models;

public class HorseBet {
    private int horseNumber;
    private int betAmount;

    public HorseBet(int horseNumber, int betAmount) {
        this.horseNumber = horseNumber;
        this.betAmount = betAmount;
    }

    public int getHorseNumber() {
        return horseNumber;
    }

    public int getBetAmount() {
        return betAmount;
    }
}