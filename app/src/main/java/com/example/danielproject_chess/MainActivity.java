package com.example.danielproject_chess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainLayout;
    private Button createBtn;
    private Button joinBtn;
    private EditText userCodeET;
    private TextView newCodeTV;

    private Board b;
    private TextView whiteTV;
    private TextView blackTV;

    private String uuid;
    private String email;
    private FirebaseFirestore db;
    private DocumentReference gameRef;
    private boolean gameStarted;
    private boolean clientIsBlack;
    private String lastMove;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcomeUser();
        init();
        clickListeners();
//        createGameAndListener();
//        startBoard();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (gameRef != null) gameRef.get().addOnSuccessListener(snapshot -> {

        });
    }
    public void welcomeUser(){
        email = getIntent().getStringExtra("email");
    }
    public void init(){
        createBtn = findViewById(R.id.create_btn);
        joinBtn = findViewById(R.id.join_btn);
        userCodeET = findViewById(R.id.code_input);
        newCodeTV = findViewById(R.id.new_game_code);
        gameStarted = false;
    }
    public void clickListeners() {
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uuid != null) db.collection("games").document(uuid).delete();//if this is the second time user clicks this button

                db = FirebaseFirestore.getInstance();
                uuid = UUID.randomUUID().toString();
                gameRef = db.collection("games").document("a");

                Map<String, Object> game = new HashMap<>();
                game.put("white", email);
                game.put("black", "");

                gameRef.set(game);
                clientIsBlack = false;

                newCodeTV.setText(uuid);
                Toast.makeText(MainActivity.this, "waiting for player to join", Toast.LENGTH_SHORT).show();

                listenToGame();
            }
        });
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uuid != null) db.collection("games").document(uuid).delete();//if user already created new game

                db = FirebaseFirestore.getInstance();
                uuid = userCodeET.getText().toString();
                gameRef = db.collection("games").document("a");

                gameRef.get().addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {//no game with given uuid
                        Toast.makeText(MainActivity.this, "game not found", Toast.LENGTH_SHORT).show();
                    } else {
                        String black = snapshot.getString("black");

                        if (black == null || black.isEmpty()) {
                            gameRef.update("black", email);
                            clientIsBlack = true;
                        } else {
                            Toast.makeText(MainActivity.this, "game is already full", Toast.LENGTH_SHORT).show();
                        }
                    }

                    gameStarted = true;
                    startBoard();
                    listenToGame();
                });
            }
        });
    }
    private void listenToGame() {
        gameRef.addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || !snapshot.exists()) return;

            String white = (String) snapshot.get("white");
            String black = (String) snapshot.get("black");
            String move = (String) snapshot.get("move");


            if (!(gameStarted || black == null || black.isEmpty())){//check if second player joined or if this check already passed. joining player doesn't have to check this
                gameStarted = true;
                startBoard();
            }

            if (move != null && !move.isEmpty() && !move.equals(lastMove)) {//move check
                lastMove = move;
                b.getMove(move);
            }

            if (gameStarted && (white == null || white.isEmpty() || black == null || black.isEmpty())){//if player leaves in the middle of the game
                endGame("opponent left", clientIsBlack ? "black" : "white");
            }
        });
    }
    public void addMoveToDatabase(String move){
        gameRef.get().addOnSuccessListener(snapshot -> {
            Map<String, Object> update = new HashMap<>();
            update.put("move", move);

            gameRef.update(update);
        });
    }
    public void startBoard() {
        setContentView(R.layout.activity_board);

        mainLayout = findViewById(R.id.board);
        b = new Board(this, mainLayout, clientIsBlack);
        whiteTV = findViewById(R.id.white_player);
        blackTV = findViewById(R.id.black_player);

        gameRef.get().addOnSuccessListener(snapshot -> {
           whiteTV.setText(snapshot.getString("white") + " (" + MainActivity.this.getResources().getConfiguration().locale.getCountry() + ")");
            blackTV.setText(snapshot.getString("black") + " (" + MainActivity.this.getResources().getConfiguration().locale.getCountry() + ")");
        });
    }
    public void endGame(String whoWon, String pointsTo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_main);

                Toast.makeText(MainActivity.this, whoWon, Toast.LENGTH_SHORT).show();
                //db.collection("games").document(uuid).delete();//todo: add points to database
                gameStarted = false;
            }
        });
    }
}