package com.example.danielproject_chess;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DBManager extends AndroidViewModel {

    private String uuid;
    private FirebaseFirestore db;
    private DocumentReference gameRef;
    private boolean gameStarted;
    private boolean clientIsBlack;
    private String move;
    private String white;
    private String black;

    private MutableLiveData<String> mutableMove;
    private MutableLiveData<String> mutableUUID;
    private MutableLiveData<Boolean> mutableGameStarted;
    private MutableLiveData<String> mutableWhite;
    private MutableLiveData<String> mutableBlack;



    public DBManager(@NonNull Application application) {
        super(application);

        init();
    }
    private void init(){
        mutableMove = new MutableLiveData<>(move);
        mutableUUID = new MutableLiveData<>(uuid);
        mutableGameStarted = new MutableLiveData<>(gameStarted);
        mutableWhite = new MutableLiveData<>(black);
        mutableBlack = new MutableLiveData<>(white);
    }
    public void startGame(String email){
        if (uuid != null) gameRef.delete();//if this is the second time user clicks this button

        db = FirebaseFirestore.getInstance();
        uuid = "a"; //UUID.randomUUID().toString();
        gameRef = db.collection("games").document(uuid);

        Map<String, Object> game = new HashMap<>();
        game.put("white", email);
        game.put("black", "");

        gameRef.set(game);
        clientIsBlack = false;

        Toast.makeText(getApplication(), "waiting for player to join", Toast.LENGTH_SHORT).show();

        listenToGame();
    }
    public void joinGame(String email){
        if (uuid != null) gameRef.delete();//if user already created new game

        db = FirebaseFirestore.getInstance();
        uuid = "a";//userCodeET.getText().toString();
        gameRef = db.collection("games").document(uuid);

        gameRef.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) {//no game with given uuid
                Toast.makeText(getApplication(), "game not found", Toast.LENGTH_SHORT).show();
            } else {
                String black = snapshot.getString("black");

                if (black == null || black.isEmpty()) {
                    gameRef.update("black", email);
                    clientIsBlack = true;
                } else {
                    Toast.makeText(getApplication(), "game is already full", Toast.LENGTH_SHORT).show();
                }
            }

            gameStarted = true;
            listenToGame();
        });
    }
    public void exitGame(){
        if (gameRef != null){
            gameRef.get().addOnSuccessListener((snapshot) -> {
                gameRef.update(clientIsBlack ? "black" : "white", "");
            });
        }
    }
    private void gameEnded(){
        gameRef.delete();//todo: add points to database
        gameRef = null;
        uuid = null;
        move = null;
    }

    private void listenToGame() {
        gameRef.addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || !snapshot.exists()) return;

            String white = (String) snapshot.get("white");
            String black = (String) snapshot.get("black");
            String move = (String) snapshot.get("move");


            if (!(gameStarted || black == null || black.isEmpty())){//check if second player joined or if this check already passed. joining player doesn't have to check this
                gameStarted = true;
                this.white = white;
                this.black = black;
            }

            if (move != null && !move.isEmpty() && !move.equals(this.move)) {//move check
                this.move = move;
            }

            if (gameStarted && (white == null || white.isEmpty() || black == null || black.isEmpty())){//if player leaves in the middle of the game
                gameStarted = false;
                gameEnded();
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

    public MutableLiveData<String> getMove(){
        return mutableMove;
    }
    public MutableLiveData<String> getUUID() {
        return mutableUUID;
    }
    public MutableLiveData<Boolean> getGameStarted() {
        return mutableGameStarted;
    }
    public MutableLiveData<String> getWhite() {
        return mutableWhite;
    }
    public MutableLiveData<String> getBlack() {
        return mutableBlack;
    }
}
