package com.example.danielproject_chess;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private Button createBtn;
    private Button joinBtn;
    private EditText userCodeET;
    private TextView newCodeTV;

    private LinearLayout mainLayout;
    private Button resignBtn;
    private TextView whiteTV;
    private TextView blackTV;

    private Board b;
    private DBManager dbManager;
    private String email;
    private boolean clientIsBlack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMain();
        welcomeUser();
    }
    @Override
    protected void onStop() {
        super.onStop();

        dbManager.exitGame();
    }

    public void welcomeUser(){
        email = getIntent().getStringExtra("email");
        Toast.makeText(this, "Welcome, " + email, Toast.LENGTH_SHORT).show();
    }

    public void initMain(){
        setContentView(R.layout.activity_main);
        createBtn = findViewById(R.id.create_btn);
        joinBtn = findViewById(R.id.join_btn);
        userCodeET = findViewById(R.id.code_input);
        newCodeTV = findViewById(R.id.new_game_code);
        dbManager = new DBManager(getApplication());

        clickListenersMain();
        valChangeListeners();
    }
    public void initBoard(){
        setContentView(R.layout.activity_board);
        mainLayout = findViewById(R.id.board);
        resignBtn = findViewById(R.id.resign_btn);
        whiteTV = findViewById(R.id.white_player);
        blackTV = findViewById(R.id.black_player);

        b = new Board(this, mainLayout, clientIsBlack);

        whiteTV.setText(dbManager.getWhite());
        blackTV.setText(dbManager.getBlack());

        clickListenersBoard();
    }

    public void clickListenersMain() {
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbManager.startGame(email);
                clientIsBlack = false;
            }
        });
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbManager.joinGame(email, "a"/*userCodeET.getText().toString()*/);
                clientIsBlack = true;
            }
        });
    }
    public void clickListenersBoard() {
        resignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbManager.exitGame();
            }
        });
    }
    public void valChangeListeners(){
        AtomicBoolean firstCall = new AtomicBoolean(true);

        dbManager.getMove().observe(this, move -> {
            if (move != null)
                b.setMove(move);
        });
        dbManager.getUUID().observe(this, uuid -> {
            if (uuid != null && !clientIsBlack)
                newCodeTV.setText(uuid);
        });
        dbManager.getGameStarted().observe(this, gameStarted -> {
            if (gameStarted) {
                initBoard();
            }
            else if (!firstCall.get())
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        firstCall.set(true);
                        initMain();
                    }
                });
            else
                firstCall.set(false);
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