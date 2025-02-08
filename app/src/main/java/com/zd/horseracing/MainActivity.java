package com.zd.horseracing;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView tvBalance;
    private TextView tvBet;
    private Button btnChooseHorse;
    private Button btnStart;
    private Button btnReset;
    private SeekBar seekBar1;
    private SeekBar seekBar2;
    private SeekBar seekBar3;
    private SeekBar seekBar4;

    private int balance = 1000;
    private boolean isRacing = false;
    private Handler handler;

    private static class HorseBet {
        int horseNumber;
        int betAmount;

        HorseBet(int horseNumber, int betAmount) {
            this.horseNumber = horseNumber;
            this.betAmount = betAmount;
        }
    }

    private ArrayList<HorseBet> selectedHorseBets;
    private int totalBetAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedHorseBets = new ArrayList<>();
        handler = new Handler(Looper.getMainLooper());

        initViews();
        setupListeners();
        updateUI();
    }

    private void initViews() {
        tvBalance = findViewById(R.id.tvBalance);
        tvBet = findViewById(R.id.tvBet);
        btnChooseHorse = findViewById(R.id.btnChooseHorse);
        btnStart = findViewById(R.id.btnStart);
        btnReset = findViewById(R.id.btnReset);
        seekBar1 = findViewById(R.id.seekBar1);
        seekBar2 = findViewById(R.id.seekBar2);
        seekBar3 = findViewById(R.id.seekBar3);
        seekBar4 = findViewById(R.id.seekBar4);

        seekBar1.setEnabled(false);
        seekBar2.setEnabled(false);
        seekBar3.setEnabled(false);
        seekBar4.setEnabled(false);
    }

    private void setupListeners() {
        btnChooseHorse.setOnClickListener(v -> showChooseHorseDialog());
        btnStart.setOnClickListener(v -> startRace());
        btnReset.setOnClickListener(v -> resetRace());
    }

    private void showChooseHorseDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_choose_horse);

        CheckBox[] checkBoxes = new CheckBox[]{
                dialog.findViewById(R.id.checkBox1),
                dialog.findViewById(R.id.checkBox2),
                dialog.findViewById(R.id.checkBox3),
                dialog.findViewById(R.id.checkBox4)
        };

        EditText[] betInputs = new EditText[]{
                dialog.findViewById(R.id.etBet1),
                dialog.findViewById(R.id.etBet2),
                dialog.findViewById(R.id.etBet3),
                dialog.findViewById(R.id.etBet4)
        };

        // Restore previous selections and bets
        for (HorseBet bet : selectedHorseBets) {
            checkBoxes[bet.horseNumber - 1].setChecked(true);
            betInputs[bet.horseNumber - 1].setText(String.valueOf(bet.betAmount));
        }

        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> {
            // Validate and save bets
            selectedHorseBets.clear();
            totalBetAmount = 0;

            for (int i = 0; i < checkBoxes.length; i++) {
                if (checkBoxes[i].isChecked()) {
                    String betStr = betInputs[i].getText().toString();
                    if (betStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập số tiền cược cho ngựa " + (i + 1), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int betAmount = Integer.parseInt(betStr);
                    if (betAmount <= 0) {
                        Toast.makeText(this, "Số tiền cược phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedHorseBets.add(new HorseBet(i + 1, betAmount));
                    totalBetAmount += betAmount;
                }
            }

            if (totalBetAmount > balance) {
                Toast.makeText(this, "Không đủ tiền cược", Toast.LENGTH_SHORT).show();
                selectedHorseBets.clear();
                totalBetAmount = 0;
                return;
            }

            dialog.dismiss();
            updateUI();
        });

        dialog.show();
    }

    private void startRace() {
        if (selectedHorseBets.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngựa trước", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalBetAmount > balance) {
            Toast.makeText(this, "Không đủ tiền cược", Toast.LENGTH_SHORT).show();
            return;
        }

        isRacing = true;
        btnChooseHorse.setEnabled(false);
        btnStart.setEnabled(false);
        balance -= totalBetAmount; // Trừ tiền cược trước khi đua
        updateUI();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isRacing) return;

                boolean hasWinner = false;
                SeekBar[] seekBars = {seekBar1, seekBar2, seekBar3, seekBar4};

                for (int i = 0; i < seekBars.length; i++) {
                    int progress = seekBars[i].getProgress() + new Random().nextInt(3);
                    seekBars[i].setProgress(progress);
                    if (progress >= 100) {
                        hasWinner = true;
                        handleWinner(i + 1);
                        break;
                    }
                }

                if (!hasWinner) {
                    handler.postDelayed(this, 50);
                }
            }
        });
    }

    private void handleWinner(int winningHorse) {
        isRacing = false;

        int totalWinnings = 0;
        int totalLosses = 0;
        StringBuilder resultMessage = new StringBuilder();

        // Check winning horse
        for (HorseBet bet : selectedHorseBets) {
            if (bet.horseNumber == winningHorse) {
                // Win 2x the bet amount
                int winAmount = bet.betAmount * 2;
                totalWinnings += winAmount;
                resultMessage.append("Ngựa ").append(bet.horseNumber)
                        .append(" về nhất! +").append(winAmount).append("đ\n");
            } else {
                // Lose 50% of the bet amount
                int lossAmount = bet.betAmount / 2;
                totalLosses += lossAmount;
                resultMessage.append("Ngựa ").append(bet.horseNumber)
                        .append(" thua: -").append(lossAmount).append("đ\n");
            }
        }

        // Update balance
        int netChange = totalWinnings - totalLosses;
        balance += (totalWinnings); // Cộng tiền thắng vào (tiền thua đã trừ lúc bắt đầu đua)

        resultMessage.append("\nTổng cộng: ").append(netChange > 0 ? "+" : "")
                .append(netChange).append("đ");

        new AlertDialog.Builder(this)
                .setTitle("Kết quả")
                .setMessage(resultMessage.toString())
                .setPositiveButton("OK", (dialog, which) -> {
                    btnReset.setEnabled(true);
                    updateUI();
                })
                .show();
    }

    private void resetRace() {
        seekBar1.setProgress(0);
        seekBar2.setProgress(0);
        seekBar3.setProgress(0);
        seekBar4.setProgress(0);

        selectedHorseBets.clear();
        totalBetAmount = 0;
        isRacing = false;

        btnChooseHorse.setEnabled(true);
        btnStart.setEnabled(true);
        btnReset.setEnabled(true);

        updateUI();
    }

    private void updateUI() {
        tvBalance.setText(String.valueOf(balance));
        tvBet.setText(String.valueOf(totalBetAmount));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}