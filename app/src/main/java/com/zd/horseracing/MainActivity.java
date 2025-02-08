package com.zd.horseracing;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CheckBox;
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
    private int betAmount = 0;
    private ArrayList<Integer> selectedHorses;
    private boolean isRacing = false;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedHorses = new ArrayList<>();
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

        for (int horseIndex : selectedHorses) {
            checkBoxes[horseIndex - 1].setChecked(true);
        }

        dialog.setOnDismissListener(dialogInterface -> {
            selectedHorses.clear();
            for (int i = 0; i < checkBoxes.length; i++) {
                if (checkBoxes[i].isChecked()) {
                    selectedHorses.add(i + 1);
                }
            }
            betAmount = selectedHorses.size() * 10;
            updateUI();
        });

        dialog.show();
    }

    private void startRace() {
        if (selectedHorses.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngựa trước", Toast.LENGTH_SHORT).show();
            return;
        }

        if (betAmount > balance) {
            Toast.makeText(this, "Không đủ tiền cược", Toast.LENGTH_SHORT).show();
            return;
        }

        isRacing = true;
        btnChooseHorse.setEnabled(false);
        btnStart.setEnabled(false);
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

        // Tính toán tiền thưởng/phạt
        int totalWinnings = 0;
        int totalLosses = 0;
        StringBuilder resultMessage = new StringBuilder();

        // Kiểm tra ngựa thắng
        if (selectedHorses.contains(winningHorse)) {
            // Thắng x2 tiền cược của con thắng
            int winAmount = 20; // 10 * 2 (tiền cược mỗi con là 10)
            totalWinnings += winAmount;
            resultMessage.append("Ngựa ").append(winningHorse).append(" về nhất! +").append(winAmount).append("đ\n");
        }

        // Tính tiền thua cho các con ngựa đã đặt không về nhất
        for (int horse : selectedHorses) {
            if (horse != winningHorse) {
                // Thua mất 50% tiền cược mỗi con
                int lossAmount = 5; // 50% của 10
                totalLosses += lossAmount;
                resultMessage.append("Ngựa ").append(horse).append(" thua: -").append(lossAmount).append("đ\n");
            }
        }

        // Cập nhật số dư
        int netChange = totalWinnings - totalLosses;
        balance += netChange;

        // Thêm tổng kết vào thông báo
        resultMessage.append("\nTổng cộng: ").append(netChange > 0 ? "+" : "").append(netChange).append("đ");

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

        selectedHorses.clear();
        betAmount = 0;
        isRacing = false;

        btnChooseHorse.setEnabled(true);
        btnStart.setEnabled(true);
        btnReset.setEnabled(true);

        updateUI();
    }

    private void updateUI() {
        tvBalance.setText(String.valueOf(balance));
        tvBet.setText(String.valueOf(betAmount));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}