package com.zd.horseracing.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zd.horseracing.Models.HorseBet;
import com.zd.horseracing.Repositories.RaceRepository;
import java.util.List;

public class RaceViewModel extends ViewModel {
    private final RaceRepository repository;
    private final MutableLiveData<Integer> balance = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalBet = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRacing = new MutableLiveData<>(false);
    private final MutableLiveData<String> raceResult = new MutableLiveData<>();

    public RaceViewModel() {
        repository = new RaceRepository();
        updateBalanceAndBet();
    }

    public LiveData<Integer> getBalance() {
        return balance;
    }

    public LiveData<Integer> getTotalBet() {
        return totalBet;
    }

    public LiveData<Boolean> getIsRacing() {
        return isRacing;
    }

    public LiveData<String> getRaceResult() {
        return raceResult;
    }

    public void setCurrentBets(List<HorseBet> bets) {
        repository.setCurrentBets(bets);
        updateBalanceAndBet();
    }

    public List<HorseBet> getCurrentBets() {
        return repository.getCurrentBets();
    }

    public void startRace() {
        if (canStartRace()) {
            isRacing.setValue(true);
            repository.updateBalance(-repository.getTotalBetAmount());
            updateBalanceAndBet();
        }
    }

    public void handleRaceFinished(int winningHorse) {
        isRacing.setValue(false);
        calculateAndUpdateResults(winningHorse);
    }

    public void resetRace() {
        repository.clearBets();
        isRacing.setValue(false);
        raceResult.setValue(null);
        updateBalanceAndBet();
    }

    private boolean canStartRace() {
        return !repository.getCurrentBets().isEmpty()
                && repository.getTotalBetAmount() <= repository.getBalance();
    }

    private void calculateAndUpdateResults(int winningHorse) {
        int totalWinnings = 0;
        StringBuilder resultMessage = new StringBuilder();

        for (HorseBet bet : repository.getCurrentBets()) {
            if (bet.getHorseNumber() == winningHorse) {
                int winAmount = bet.getBetAmount() * 2;
                totalWinnings += winAmount;
                resultMessage.append("Ngựa ").append(bet.getHorseNumber())
                        .append(" về nhất! +").append(winAmount).append("đ\n");
            } else {
                resultMessage.append("Ngựa ").append(bet.getHorseNumber())
                        .append(" thua\n");
            }
        }

        repository.updateBalance(totalWinnings);
        resultMessage.append("\nTổng cộng: ").append(totalWinnings > 0 ? "+" : "")
                .append(totalWinnings - repository.getTotalBetAmount()).append("đ");

        raceResult.setValue(resultMessage.toString());
        updateBalanceAndBet();
    }

    private void updateBalanceAndBet() {
        balance.setValue(repository.getBalance());
        totalBet.setValue(repository.getTotalBetAmount());
    }
}