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

/**
 * ViewModel for managing the race data and logic.  Interacts with the {@link RaceRepository}
 * to persist data and provides LiveData to observe changes in the UI.
 */
public class RaceViewModel extends AndroidViewModel {
    private final RaceRepository repository;
    private final MutableLiveData<Integer> balance = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalBet = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRacing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> needsReset = new MutableLiveData<>(false);
    private final MutableLiveData<String> raceResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> moneyChange = new MutableLiveData<>(0);
    private final Context context;

    /**
     * Constructor for the RaceViewModel.
     *
     * @param application The application instance.
     */
    public RaceViewModel(@NonNull Application application) {
        super(application);
        this.context = application.getApplicationContext();
        repository = new RaceRepository();
        updateBalanceAndBet(); // Initialize balance and bet from repository
    }

    /**
     * Gets the LiveData for the user's balance.
     *
     * @return LiveData containing the user's balance.
     */
    public LiveData<Integer> getBalance() {
        return balance;
    }

    /**
     * Gets the LiveData for the total bet amount.
     *
     * @return LiveData containing the total bet amount.
     */
    public LiveData<Integer> getTotalBet() {
        return totalBet;
    }

    /**
     * Gets the LiveData indicating whether a race is currently in progress.
     *
     * @return LiveData containing a boolean indicating if a race is running.
     */
    public LiveData<Boolean> getIsRacing() {
        return isRacing;
    }

    /**
     * Gets the LiveData indicating whether the race needs to be reset.
     *
     * @return LiveData containing a boolean indicating if a reset is needed.
     */
    public LiveData<Boolean> getNeedsReset() {
        return needsReset;
    }

    /**
     * Gets the LiveData for the race result string.
     *
     * @return LiveData containing the race result string.
     */
    public LiveData<String> getRaceResult() {
        return raceResult;
    }

    /**
     * Sets the current bets.  Updates the total bet amount in the UI.
     *
     * @param bets The list of {@link HorseBet} objects representing the user's bets.
     */
    public void setCurrentBets(List<HorseBet> bets) {
        repository.setCurrentBets(bets);
        updateBalanceAndBet();
    }

    /**
     * Gets the LiveData representing the change in the user's money after a race.
     * @return LiveData containing the money change.
     */
    public LiveData<Integer> getMoneyChange() {
        return moneyChange;
    }

    /**
     * Sets value for money change.
     * @param change The money change.
     */
    private void setMoneyChange(int change) {
        moneyChange.setValue(change);
    }
    /**
     * Gets the current bets placed by the user.
     *
     * @return The list of {@link HorseBet} objects.
     */
    public List<HorseBet> getCurrentBets() {
        return repository.getCurrentBets();
    }

    /**
     * Starts the race.  Checks if the race can start (sufficient balance, bets placed, etc.),
     * updates the racing state, deducts the total bet amount from the balance,
     * and updates the UI.
     *
     * @return True if the race was successfully started, false otherwise.
     */
    public boolean startRace() {
        if (!canStartRace()) {
            return false; // Race cannot start
        }

        isRacing.setValue(true); // Set racing state to true
        needsReset.setValue(false); // Reset the needsReset flag
        repository.updateBalance(-repository.getTotalBetAmount()); // Deduct bet amount
        updateBalanceAndBet(); // Update UI
        return true;
    }


    /**
     * Handles the race finish event.  Updates the racing state, sets the needsReset flag,
     * and calculates the results.
     *
     * @param winningHorse The number of the winning horse (1-based index).
     */
    public void handleRaceFinished(int winningHorse) {
        isRacing.setValue(false); // Set racing state to false
        needsReset.setValue(true); // Set needsReset flag to true
        calculateAndUpdateResults(winningHorse); // Calculate and update results
    }

    /**
     * Resets the race state.  Clears bets, resets racing and needsReset flags,
     * clears the race result, and updates the UI.
     */
    public void resetRace() {
        repository.clearBets(); // Clear all bets
        isRacing.setValue(false); // Reset racing state
        needsReset.setValue(false); // Reset needsReset flag
        raceResult.setValue(null); // Clear race result
        updateBalanceAndBet(); // Update UI
    }

    /**
     * Checks if the race can start based on several conditions: bets placed, sufficient balance,
     * race not already running, and no reset needed.  Displays appropriate Toast messages
     * if conditions are not met.
     *
     * @return True if the race can start, false otherwise.
     */
    private boolean canStartRace() {
        if (repository.getCurrentBets().isEmpty()) {
            showToast("You haven't placed any bets!");
            return false;
        }

        if (repository.getTotalBetAmount() > repository.getBalance()) {
            showToast("Insufficient balance!");
            return false;
        }

        if (Boolean.TRUE.equals(isRacing.getValue())) {
            showToast("The race is already in progress!");
            return false;
        }

        if (Boolean.TRUE.equals(needsReset.getValue())) {
            showToast("Need to reset the race!");
            return false;
        }

        return true;
    }


    /**
     * Displays a Toast message.
     *
     * @param message The message to display.
     */
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    /**
     * Calculates the winnings based on the winning horse and updates the user's balance
     * *only if* the user has net winnings. Constructs a detailed result message string.
     * The user's balance was already decremented by the total bet amount at the start
     * of the race. This method adds winnings back to the balance *only if* the total
     * winnings exceed the initial total bet amount. It calculates and displays the net
     * change in the user's balance (winnings - initial bet).
     *
     * @param winningHorse The number of the winning horse (1-based index).
     */
    private void calculateAndUpdateResults(int winningHorse) {
        int totalBetAmount = repository.getTotalBetAmount();
        int totalWinnings = 0;
        StringBuilder resultMessage = new StringBuilder();

        // Calculate winnings (if any)
        for (HorseBet bet : repository.getCurrentBets()) {
            if (bet.getHorseNumber() == winningHorse) {
                int winAmount = bet.getBetAmount() * 2; // Double the bet amount for a win
                totalWinnings += winAmount;
                resultMessage.append("Horse ").append(bet.getHorseNumber())
                        .append(" won! +").append(winAmount).append("đ\n");
            } else {
                resultMessage.append("Horse ").append(bet.getHorseNumber())
                        .append(" lost\n");
            }
        }

        // Calculate the actual money change (including losses)
        int actualMoneyChange = totalWinnings - totalBetAmount;

        // Only update the balance if there are net winnings
        if (totalWinnings > 0) {
            repository.updateBalance(totalWinnings);
        }

        // Update moneyChange with the actual change
        setMoneyChange(actualMoneyChange);

        // Update the result message
        resultMessage.append("\nTotal: ")
                .append(actualMoneyChange >= 0 ? "+" : "")
                .append(actualMoneyChange).append("đ");

        raceResult.setValue(resultMessage.toString());
        updateBalanceAndBet();
    }

    /**
     * Updates the balance and total bet LiveData objects with the current values from the repository.
     */
    private void updateBalanceAndBet() {
        balance.setValue(repository.getBalance());
        totalBet.setValue(repository.getTotalBetAmount());
    }

    /**
     * Updates the user's balance by the given amount.  Delegates to the repository and
     * then updates the LiveData.
     * @param amount The amount to add to the balance (can be negative for deductions).
     */
    public void updateBalance(int amount) {
        repository.updateBalance(amount);
        updateBalanceAndBet();
    }
}