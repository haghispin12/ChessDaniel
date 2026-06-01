package com.example.danielproject_chess;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

public class BoardActivity extends AppCompatActivity {

    private LinearLayout mainLayout;
    private Button resignBtn;
    private TextView whiteTV;
    private TextView blackTV;

    private Board b;
    private DBManager dbManager;
    private boolean clientIsBlack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        init();
    }
    @Override
    protected void onStop() {
        super.onStop();

        dbManager.exitGame();
    }
    public void init(){
        mainLayout = findViewById(R.id.board);
        whiteTV = findViewById(R.id.white_player);
        blackTV = findViewById(R.id.black_player);

        b = new Board(this, mainLayout, clientIsBlack);
        dbManager = new DBManager(getApplication(), getIntent().getStringExtra("uuid"), getIntent().getBooleanExtra("clientIsBlack", false));
        clientIsBlack = getIntent().getBooleanExtra("clientIsBlack", false);

        whiteTV.setText(dbManager.getWhite());
        blackTV.setText(dbManager.getBlack());

        valChangeListeners();
    }
    public void valChangeListeners(){
        dbManager.getMove().observe(this, move -> {
            if (move != null)
                b.setMove(move);
        });
        dbManager.getGameStarted().observe(this, gameStarted -> {
            if (!gameStarted) {//check if game has really ended, always disregard first call
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(BoardActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }
    public void addMoveToDatabase(String move){
        dbManager.addMoveToDatabase(move);
    }
    public void endGame(String whoWon, String pointsTo) {
        Toast.makeText(this, whoWon, Toast.LENGTH_SHORT).show();
        dbManager.exitGame();
    }
}
