package com.zd.horseracing.ViewModels;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.zd.horseracing.Models.HorseBet;
import com.zd.horseracing.Repositories.RaceRepository;
import java.util.List;

public class RaceViewModel extends AndroidViewModel {
    private final RaceRepository repository;
    private final MutableLiveData<Integer> balance = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalBet = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRacing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> needsReset = new MutableLiveData<>(false);
    private final MutableLiveData<String> raceResult = new MutableLiveData<>();
    private final Context context;

    public RaceViewModel(@NonNull Application application) {
        super(application);
        this.context = application.getApplicationContext();
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

    public LiveData<Boolean> getNeedsReset() {
        return needsReset;
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
        if (!canStartRace()) {
            return;
        }

        isRacing.setValue(true);
        needsReset.setValue(false);
        repository.updateBalance(-repository.getTotalBetAmount());
        updateBalanceAndBet();
    }


    public void handleRaceFinished(int winningHorse) {
        isRacing.setValue(false);
        needsReset.setValue(true);
        calculateAndUpdateResults(winningHorse);
    }

    public void resetRace() {
        repository.clearBets();
        isRacing.setValue(false);
        needsReset.setValue(false);
        raceResult.setValue(null);
        updateBalanceAndBet();
    }

    private boolean canStartRace() {
        if (repository.getCurrentBets().isEmpty()) {
            showToast("Bạn chưa đặt cược!");
            return false;
        }

        if (repository.getTotalBetAmount() > repository.getBalance()) {
            showToast("Số dư không đủ!");
            return false;
        }

        if (Boolean.TRUE.equals(isRacing.getValue())) {
            showToast("Cuộc đua đang diễn ra!");
            return false;
        }

        if (Boolean.TRUE.equals(needsReset.getValue())) {
            showToast("Cần đặt lại cuộc đua!");
            return false;
        }

        return true;
    }


    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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