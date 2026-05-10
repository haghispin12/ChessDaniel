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
    private String uuid;
    private Board b;
    private String email;
    private FirebaseFirestore db;
    private DocumentReference gameRef;
    private boolean clientIsBlack;



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
        if (db != null) db.collection("games").document(uuid).delete();
    }
    public void welcomeUser(){
        email = getIntent().getStringExtra("email");
    }
    public void init(){
        createBtn = findViewById(R.id.create_btn);
        joinBtn = findViewById(R.id.join_btn);
        userCodeET = findViewById(R.id.code_input);
        newCodeTV = findViewById(R.id.new_game_code);
    }
    public void clickListeners() {
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uuid != null) db.collection("games").document(uuid).delete();//if this is the second time user clicks this button

                db = FirebaseFirestore.getInstance();
                uuid = UUID.randomUUID().toString();
                gameRef = db.collection("games").document(uuid);

                Map<String, Object> game = new HashMap<>();
                game.put("white", email);
                game.put("black", "");

                gameRef.set(game);
                clientIsBlack = false;

                newCodeTV.setText(uuid);
                Toast.makeText(MainActivity.this, "waiting for player to join", Toast.LENGTH_SHORT).show();

                gameRef.addSnapshotListener((snapshot, error) -> {//wait for other player to join
                    if (snapshot == null || !snapshot.exists()) return;

                    String black = (String) snapshot.get("black");

                    if (black != null && !black.isEmpty()) {
                        startBoard();
                        listenToGame();
                    }
                });
            }
        });
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uuid != null) db.collection("games").document(uuid).delete();//if user already created new game

                db = FirebaseFirestore.getInstance();
                uuid = userCodeET.getText().toString();
                gameRef = db.collection("games").document(uuid);

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

                    startBoard();
                    listenToGame();
                });
            }
        });
    }
    private void listenToGame() {
        gameRef.addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || !snapshot.exists()) return;

            String move = (String) snapshot.get("move");

            if (move != null && !move.isEmpty()) {
                b.getMove(move);
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
    }
}