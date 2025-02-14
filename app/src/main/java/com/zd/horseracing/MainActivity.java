package com.zd.horseracing;
import android.media.MediaPlayer;
import android.view.Window;
import android.view.WindowManager;
import android.view.Gravity;
import android.app.Dialog;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.zd.horseracing.Models.HorseBet;
import com.zd.horseracing.ViewModels.RaceViewModel;

import java.util.ArrayList;
import java.util.Random;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lock the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        viewModel = new ViewModelProvider(this).get(RaceViewModel.class);
        handler = new Handler(Looper.getMainLooper());

        initViews();
        setupListeners();
        observeViewModel();
    }

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

        seekBar1.setEnabled(false);
        seekBar2.setEnabled(false);
        seekBar3.setEnabled(false);
        seekBar4.setEnabled(false);
    }

    private void setupListeners() {
        btnChooseHorse.setOnClickListener(v -> showChooseHorseDialog());
        btnStart.setOnClickListener(v -> startRace());
        btnReset.setOnClickListener(v -> {
            resetSeekBars();
            viewModel.resetRace();
        });
        btnAddMoney.setOnClickListener(v -> showAddMoneyDialog());
    }

    private void observeViewModel() {
        viewModel.getBalance().observe(this, balance ->
                tvBalance.setText(String.valueOf(balance)));

        viewModel.getTotalBet().observe(this, totalBet ->
                tvBet.setText(String.valueOf(totalBet)));

        viewModel.getIsRacing().observe(this, isRacing -> {
            btnChooseHorse.setEnabled(!isRacing);
            btnStart.setEnabled(!isRacing);
            btnReset.setEnabled(!isRacing);
        });

        viewModel.getNeedsReset().observe(this, needsReset -> {
            if (needsReset) {
                btnChooseHorse.setEnabled(false);
                btnStart.setEnabled(false);
                btnReset.setEnabled(true);
            }
        });

        viewModel.getRaceResult().observe(this, result -> {
            if (result != null) {
                showResultDialog(result);
            }
        });
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

        // Restore previous selections
        for (HorseBet bet : viewModel.getCurrentBets()) {
            checkBoxes[bet.getHorseNumber() - 1].setChecked(true);
            betInputs[bet.getHorseNumber() - 1].setText(String.valueOf(bet.getBetAmount()));
        }

        dialog.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            if (validateAndSaveBets(checkBoxes, betInputs)) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean validateAndSaveBets(CheckBox[] checkBoxes, EditText[] betInputs) {
        ArrayList<HorseBet> newBets = new ArrayList<>();

        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isChecked()) {
                String betStr = betInputs[i].getText().toString();
                if (betStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập số tiền cược cho ngựa " + (i + 1),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                try {
                    int betAmount = Integer.parseInt(betStr);
                    if (betAmount <= 0) {
                        Toast.makeText(this, "Số tiền cược phải lớn hơn 0",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    newBets.add(new HorseBet(i + 1, betAmount));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Số tiền không hợp lệ",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        viewModel.setCurrentBets(newBets);
        return true;
    }

    private void startRace() {
        viewModel.startRace();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!viewModel.getIsRacing().getValue()) return;

                boolean hasWinner = false;
                SeekBar[] seekBars = {seekBar1, seekBar2, seekBar3, seekBar4};

                for (int i = 0; i < seekBars.length; i++) {
                    int progress = seekBars[i].getProgress() + new Random().nextInt(3);
                    seekBars[i].setProgress(progress);
                    if (progress >= 100) {
                        hasWinner = true;
                        viewModel.handleRaceFinished(i + 1);
                        break;
                    }
                }

                if (!hasWinner) {
                    handler.postDelayed(this, 50);
                }
            }
        });
    }

    private void resetSeekBars() {
        seekBar1.setProgress(0);
        seekBar2.setProgress(0);
        seekBar3.setProgress(0);
        seekBar4.setProgress(0);
    }

    private void showResultDialog(String result) {
        // Inflate layout cho dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_result);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            // Set orientation to landscape
            window.getAttributes().screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }

        // Ánh xạ các view trong layout
        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvResultMessage = dialog.findViewById(R.id.tvResultMessage);
        TextView tvMoneyChange = dialog.findViewById(R.id.tvMoneyChange);
        Button btnCloseDialog = dialog.findViewById(R.id.btnCloseDialog);
        ImageView imageView = dialog.findViewById(R.id.ivTopImage);

        // Hiển thị thông tin kết quả
        tvResultMessage.setText(result);

        // Lấy số tiền thay đổi từ ViewModel
        int moneyChange = viewModel.getMoneyChange().getValue();

        if (moneyChange > 0) {
            tvMoneyChange.setText("Bạn đã thắng + $" + moneyChange);
            tvMoneyChange.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            //play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.soundwin);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
            });
            mediaPlayer.start();
        } else if (moneyChange < 0) {
            imageView.setImageResource(R.drawable.lose);
            tvMoneyChange.setText("Bạn đã thua - $" + moneyChange);
            tvMoneyChange.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            //play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.soundlose);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
            });
            mediaPlayer.start();
        } else {
            tvMoneyChange.setText("Không có thay đổi về tiền");
            tvMoneyChange.setTextColor(getResources().getColor(android.R.color.darker_gray));
            //play sound effect
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.soundwin);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
            });
            mediaPlayer.start();
        }

        // Đóng dialog khi nhấn nút
        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        // Hiển thị dialog
        dialog.show();
    }

    private void showAddMoneyDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_money);

        EditText etAddMoney = dialog.findViewById(R.id.etAddMoney);
        Button btnConfirmAddMoney = dialog.findViewById(R.id.btnConfirmAddMoney);

        btnConfirmAddMoney.setOnClickListener(v -> {
            String moneyStr = etAddMoney.getText().toString();
            if (moneyStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int amountToAdd = Integer.parseInt(moneyStr);
                if (amountToAdd <= 0) {
                    Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Cập nhật số dư trong ViewModel
                viewModel.updateBalance(amountToAdd);
                Toast.makeText(this, "Nạp tiền thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}