package com.example.danielproject_chess;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainLayout;
    private Button createBtn;
    private Button joinBtn;
    private Button resignBtn;
    private EditText userCodeET;
    private TextView newCodeTV;

    private Board b;
    private TextView whiteTV;
    private TextView blackTV;

    private DBManager dbManager;
    private String email;
    private boolean clientIsBlack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcomeUser();
        init();
    }
    @Override
    protected void onStop() {
        super.onStop();

        dbManager.exitGame();
    }
    public void welcomeUser(){
        email = getIntent().getStringExtra("email");
    }
    public void init(){
        dbManager = new DBManager(getApplication());

        valChangeListeners();
    }
    public void clickListeners() {
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
                dbManager.joinGame(email, userCodeET.getText().toString());
                clientIsBlack = true;
            }
        });
    }
    public void valChangeListeners(){
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
                    startBoard();
                } else {//will also trigger once dbManager is initialized
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setContentView(R.layout.activity_main);

                            createBtn = findViewById(R.id.create_btn);
                            joinBtn = findViewById(R.id.join_btn);
                            userCodeET = findViewById(R.id.code_input);
                            newCodeTV = findViewById(R.id.new_game_code);
                            clickListeners();
                        }
                    });
                }
        });
    }
    public void addMoveToDatabase(String move){
        dbManager.addMoveToDatabase(move);
    }
    public void startBoard() {
        setContentView(R.layout.activity_board);

        mainLayout = findViewById(R.id.board);
        b = new Board(this, mainLayout, clientIsBlack);
        whiteTV = findViewById(R.id.white_player);
        blackTV = findViewById(R.id.black_player);

        whiteTV.setText(dbManager.getWhite());
        blackTV.setText(dbManager.getBlack());
    }
    public void endGame(String whoWon, String pointsTo) {
        Toast.makeText(this, whoWon, Toast.LENGTH_SHORT).show();
        dbManager.exitGame();
    }
}