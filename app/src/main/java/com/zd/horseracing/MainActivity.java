package com.zd.horseracing;

import android.content.Intent;
import android.media.MediaPlayer;
import android.view.Window;
import android.view.WindowManager;
import android.view.Gravity;
import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.zd.horseracing.Models.HorseBet;
import com.zd.horseracing.ViewModels.RaceViewModel;

import java.util.ArrayList;
import java.util.Random;

/**
 * The main activity of the horse racing game.  Handles UI interactions, race logic,
 * bet management, and result display.  Uses a {@link RaceViewModel} to manage game state
 * and data across configuration changes.
 */
public class MainActivity extends AppCompatActivity {
    private TextView tvBalance;
    private TextView tvBet;
    private Button btnChooseHorse;
    private Button btnStart;
    private Button btnReset;
    private Button btnAddMoney;
    private SeekBar seekBar1;
    private SeekBar seekBar2;
    private SeekBar seekBar3;
    private SeekBar seekBar4;
    private RaceViewModel viewModel;
    private Handler handler;
    private MediaPlayer bgMusic;

    /**
     * Called when the activity is first created. Initializes the ViewModel, handler,
     * background music, sets up the back button to be disabled, and calls methods to initialize views,
     * set up listeners, and observe the ViewModel.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(RaceViewModel.class);
        // Initialize Handler for UI updates
        handler = new Handler(Looper.getMainLooper());

        // Initialize and start background music
        bgMusic = MediaPlayer.create(this, R.raw.pokemonloop);
        bgMusic.setLooping(true);
        bgMusic.start();

        // Disable back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing, effectively disabling the back button
            }
        });


        initViews();
        setupListeners();
        observeViewModel();
    }

    /**
     * Initializes the UI elements by finding them in the layout.
     * Disables the SeekBars initially as user can't interact with them.
     */
    private void initViews() {
        tvBalance = findViewById(R.id.tvBalance);
        tvBet = findViewById(R.id.tvBet);
        btnChooseHorse = findViewById(R.id.btnChooseHorse);
        btnStart = findViewById(R.id.btnStart);
        btnReset = findViewById(R.id.btnReset);
        btnAddMoney = findViewById(R.id.btnAddMoney);
        seekBar1 = findViewById(R.id.seekBar1);
        seekBar2 = findViewById(R.id.seekBar2);
        seekBar3 = findViewById(R.id.seekBar3);
        seekBar4 = findViewById(R.id.seekBar4);

        // Disable SeekBars to prevent user interaction
        seekBar1.setEnabled(false);
        seekBar2.setEnabled(false);
        seekBar3.setEnabled(false);
        seekBar4.setEnabled(false);
    }

    /**
     * Sets up click listeners for the buttons.
     */
    private void setupListeners() {
        btnChooseHorse.setOnClickListener(v -> showChooseHorseDialog());
        btnStart.setOnClickListener(v -> startRace());
        btnReset.setOnClickListener(v -> {
            resetSeekBars();
            viewModel.resetRace();
        });
        btnAddMoney.setOnClickListener(v -> showAddMoneyDialog());
    }

    /**
     * Observes the LiveData in the ViewModel and updates the UI accordingly.
     */
    private void observeViewModel() {
        // Observe balance changes
        viewModel.getBalance().observe(this, balance ->
                tvBalance.setText(String.valueOf(balance)));

        // Observe total bet changes
        viewModel.getTotalBet().observe(this, totalBet ->
                tvBet.setText(String.valueOf(totalBet)));

        // Observe racing state changes to enable/disable buttons
        viewModel.getIsRacing().observe(this, isRacing -> {
            btnChooseHorse.setEnabled(!isRacing);
            btnStart.setEnabled(!isRacing);
            btnReset.setEnabled(!isRacing);
        });

        // Observe needsReset state changes to enable/disable buttons
        viewModel.getNeedsReset().observe(this, needsReset -> {
            if (needsReset) {
                btnChooseHorse.setEnabled(false);
                btnStart.setEnabled(false);
                btnReset.setEnabled(true);
            }
        });

        // Observe race result changes to display the result dialog
        viewModel.getRaceResult().observe(this, result -> {
            if (result != null) {
                showResultDialog(result);
            }
        });
    }

    /**
     * Shows a dialog for the user to choose horses and place bets.
     */
    private void showChooseHorseDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_choose_horse);

        // Array of CheckBoxes for horse selection
        CheckBox[] checkBoxes = new CheckBox[]{
                dialog.findViewById(R.id.checkBox1),
                dialog.findViewById(R.id.checkBox2),
                dialog.findViewById(R.id.checkBox3),
                dialog.findViewById(R.id.checkBox4)
        };

        // Array of EditTexts for bet amounts
        EditText[] betInputs = new EditText[]{
                dialog.findViewById(R.id.etBet1),
                dialog.findViewById(R.id.etBet2),
                dialog.findViewById(R.id.etBet3),
                dialog.findViewById(R.id.etBet4)
        };

        // Restore previous bet selections and amounts from ViewModel
        for (HorseBet bet : viewModel.getCurrentBets()) {
            checkBoxes[bet.getHorseNumber() - 1].setChecked(true);
            betInputs[bet.getHorseNumber() - 1].setText(String.valueOf(bet.getBetAmount()));
        }

        // Set click listener for the confirm button
        dialog.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            if (validateAndSaveBets(checkBoxes, betInputs)) { // Validate bets before closing dialog
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Validates the bets entered by the user and saves them to the ViewModel.
     *
     * @param checkBoxes The array of CheckBoxes representing horse selections.
     * @param betInputs  The array of EditTexts representing bet amounts.
     * @return True if the bets are valid and saved, false otherwise.
     */
    private boolean validateAndSaveBets(CheckBox[] checkBoxes, EditText[] betInputs) {
        ArrayList<HorseBet> newBets = new ArrayList<>();

        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isChecked()) {
                String betStr = betInputs[i].getText().toString();
                if (betStr.isEmpty()) {
                    Toast.makeText(this, "Please enter a bet amount for horse " + (i + 1),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                try {
                    int betAmount = Integer.parseInt(betStr);
                    if (betAmount <= 0) {
                        Toast.makeText(this, "Bet amount must be greater than 0",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    newBets.add(new HorseBet(i + 1, betAmount));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid bet amount",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        viewModel.setCurrentBets(newBets); // Save the validated bets to the ViewModel
        return true;
    }

    /**
     * Starts the horse race animation and logic.  Plays countdown and race sounds.
     * Sets up the horse animations using {@link AnimationDrawable}.
     */
    private void startRace() {

        if (!viewModel.startRace()) { // Check if the race can start (enough balance)
            return;
        }

        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.countdownfinalcut);
        mediaPlayer.setOnCompletionListener(mp -> { // Set listener for countdown completion
            mp.release();
            // Array of drawable resources for horse animations
            int[] horseDrawableRes = {
                    R.drawable.horse1_animation,
                    R.drawable.horse2_animation,
                    R.drawable.horse3_animation,
                    R.drawable.horse4_animation
            };

            // Array of SeekBars corresponding to each horse
            SeekBar[] seekBars = { seekBar1, seekBar2, seekBar3, seekBar4 };

            // Array to store AnimationDrawable instances
            final AnimationDrawable[] horseAnimations = new AnimationDrawable[seekBars.length];

            // Initialize each SeekBar with its corresponding animation drawable
            for (int i = 0; i < seekBars.length; i++) {
                Drawable drawable = ContextCompat.getDrawable(this, horseDrawableRes[i]);
                if (drawable == null) {
                    // Handle null drawable, e.g., log an error, skip, or use a default drawable.
                    continue;
                }
                seekBars[i].setThumb(drawable);
                AnimationDrawable animation = (AnimationDrawable) seekBars[i].getThumb();
                horseAnimations[i] = animation;
                // Post the start to ensure drawable is ready
                seekBars[i].post(animation::start); // Start the animation
            }

            // Create a single Random instance for performance
            final Random random = new Random();
            MediaPlayer mediaPlayer2 = MediaPlayer.create(this, R.raw.horsefootsteps); // Race sound
            handler.post(new Runnable() { // Use a Handler to update the UI on the main thread
                @Override
                public void run() {
                    if (!viewModel.getIsRacing().getValue()) return;

                    mediaPlayer2.setLooping(true);
                    mediaPlayer2.start();

                    boolean hasWinner = false;

                    for (int i = 0; i < seekBars.length; i++) {
                        int progress = seekBars[i].getProgress() + random.nextInt(3); // Random progress
                        seekBars[i].setProgress(progress);
                        if (progress >= 100) { // Check for winner
                            hasWinner = true;
                            // Stop animations for all horses
                            for (AnimationDrawable anim : horseAnimations) {
                                if (anim.isRunning()) {
                                    anim.stop();
                                }
                            }
                            mediaPlayer2.stop(); // Stop race sound
                            viewModel.handleRaceFinished(i + 1); // Notify ViewModel of the winner
                            break;
                        }
                    }

                    if (!hasWinner) {
                        handler.postDelayed(this, 50); // Continue updating UI after a delay
                    }
                }
            });
        });

        mediaPlayer.start(); // Start countdown sound
    }

    /**
     * Resets the SeekBars to their initial positions and default horse drawables.
     */
    private void resetSeekBars() {
        resetSeekBar(seekBar1, R.drawable.assasin1);
        resetSeekBar(seekBar2, R.drawable.knight_walk_1);
        resetSeekBar(seekBar3, R.drawable.ice_horse);
        resetSeekBar(seekBar4, R.drawable.horse_bend_01);
    }

    /**
     * Resets a single SeekBar to its initial position and sets the specified drawable.
     *
     * @param seekBar     The SeekBar to reset.
     * @param drawableRes The resource ID of the drawable to set as the thumb.
     */
    private void resetSeekBar(SeekBar seekBar, int drawableRes) {
        // Reset SeekBar progress
        seekBar.setProgress(0);
        Drawable thumbDrawable = ContextCompat.getDrawable(this, drawableRes);
        seekBar.setThumb(thumbDrawable); // Set the default thumb drawable
    }

    /**
     * Shows a dialog displaying the race result.  Uses a custom layout (`dialog_result`).
     *
     * @param result The race result string to display.
     */
    private void showResultDialog(String result) {
        // Inflate layout for the dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_result);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            // Set orientation to landscape
            window.getAttributes().screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }

        // Initialize views in the dialog layout
        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvResultMessage = dialog.findViewById(R.id.tvResultMessage);
        TextView tvMoneyChange = dialog.findViewById(R.id.tvMoneyChange);
        Button btnCloseDialog = dialog.findViewById(R.id.btnCloseDialog);
        ImageView imageView = dialog.findViewById(R.id.ivTopImage);

        // Display the result message
        tvResultMessage.setText(result);

        // Get the money change from the ViewModel
        int moneyChange = viewModel.getMoneyChange().getValue();

        if (moneyChange > 0) {
            tvMoneyChange.setText("You won " + moneyChange + "đ");
            tvMoneyChange.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
            //play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.soundwin);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
            });
            mediaPlayer.start();
        } else if (moneyChange < 0) {
            imageView.setImageResource(R.drawable.lose);
            tvMoneyChange.setText("You lost " + moneyChange + "đ");
            tvMoneyChange.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            //play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.soundlose);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
            });
            mediaPlayer.start();
        } else {
            tvMoneyChange.setText("No change in money");
            tvMoneyChange.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
            //play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.soundwin);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
            });
            mediaPlayer.start();
        }

        // Close the dialog when the button is clicked
        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }

    /**
     * Shows a dialog for the user to add money to their balance.
     */
    private void showAddMoneyDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_money);

        EditText etAddMoney = dialog.findViewById(R.id.etAddMoney);
        Button btnConfirmAddMoney = dialog.findViewById(R.id.btnConfirmAddMoney);

        btnConfirmAddMoney.setOnClickListener(v -> {
            String moneyStr = etAddMoney.getText().toString();
            if (moneyStr.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int amountToAdd = Integer.parseInt(moneyStr);
                if (amountToAdd <= 0) {
                    Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the balance in the ViewModel
                viewModel.updateBalance(amountToAdd);
                Toast.makeText(this, "Money added successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /**
     * Called when the activity is destroyed. Removes any pending callbacks from the Handler
     * to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null); // Remove callbacks to prevent leaks
    }
}