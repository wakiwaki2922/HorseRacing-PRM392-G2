package com.zd.horseracing;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class InstructionActivity extends AppCompatActivity {
    Button btnNext;
    MediaPlayer bgMusic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instruction_layout);
        bgMusic = MediaPlayer.create(this, R.raw.guildlinebeat);
        bgMusic.setLooping(true);
        bgMusic.start();

        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(view -> {
            Intent intent = new Intent(InstructionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        bgMusic.stop();
        super.onDestroy();
    }
}
