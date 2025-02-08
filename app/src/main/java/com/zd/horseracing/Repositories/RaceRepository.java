package com.zd.horseracing.Repositories;

import com.zd.horseracing.Models.HorseBet;
import java.util.ArrayList;
import java.util.List;

public class RaceRepository {
    private static final int INITIAL_BALANCE = 1000;
    private int balance;
    private List<HorseBet> currentBets;
    private int totalBetAmount;

    public RaceRepository() {
        this.balance = INITIAL_BALANCE;
        this.currentBets = new ArrayList<>();
        this.totalBetAmount = 0;
    }

    public int getBalance() {
        return balance;
    }

    public void updateBalance(int amount) {
        this.balance += amount;
    }

    public List<HorseBet> getCurrentBets() {
        return currentBets;
    }

    public void setCurrentBets(List<HorseBet> bets) {
        this.currentBets = bets;
        calculateTotalBetAmount();
    }

    public void clearBets() {
        currentBets.clear();
        totalBetAmount = 0;
    }

    public int getTotalBetAmount() {
        return totalBetAmount;
    }

    private void calculateTotalBetAmount() {
        totalBetAmount = 0;
        for (HorseBet bet : currentBets) {
            totalBetAmount += bet.getBetAmount();
        }
    }
}