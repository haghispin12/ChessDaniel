package com.example.danielproject_chess;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainLayout;
    private Board b;
    private String email;
    private FirebaseFirestore db;
    private DocumentReference gameRef;

    private boolean clientIsBlack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        launchLogin();
        startBoard();
        createGameAndListener();

    }
    public void init(){
        mainLayout = findViewById(R.id.board);

    }
    public void launchLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        ActivityResultLauncher<Intent> loginListener = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                email = getIntent().getStringExtra("email");
            }
        });
        loginListener.launch(intent);
    }

    public void createGameAndListener(){
        db = FirebaseFirestore.getInstance();
        gameRef = db.collection("games").document("game1");

        gameRef.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) {
                // CREATE GAME (you are white)
                Map<String, Object> game = new HashMap<>();
                game.put("white", email);
                game.put("black", "");

                gameRef.set(game);
                clientIsBlack = false;

            } else {
                String black = snapshot.getString("black");

                if (black == null || black.isEmpty()) {
                    // JOIN AS BLACK
                    gameRef.update("black", email);
                    clientIsBlack = true;
                } else {
                    Toast.makeText(this, "the game is full", Toast.LENGTH_SHORT).show();
                }
            }


        });
        listenToGame();
    }
    private void listenToGame() {

        gameRef.addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || !snapshot.exists()) return;

            String move = (String) snapshot.get("move");

            if (move != null) {
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
        b = new Board(this, mainLayout, clientIsBlack);
    }
}