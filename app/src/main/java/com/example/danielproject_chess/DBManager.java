package com.example.danielproject_chess;


import android.app.Application;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class DBManager extends AndroidViewModel {


    private String code;
    private FirebaseFirestore db;
    private DocumentReference gameRef;
    private boolean gameStarted;
    private boolean clientIsBlack;
    private String move;
    private String white;
    private String black;


    private MutableLiveData<String> mutableMove;
    private MutableLiveData<Boolean> mutableGameStarted;


    public DBManager(@NonNull Application application) {
        super(application);


        init();
    }
    private void init(){
        mutableMove = new MutableLiveData<>(move);
        mutableGameStarted = new MutableLiveData<>(gameStarted);
    }


    public void startGame(String email, String userCode){
        if (code != null) gameRef.delete();//if this is the second time user clicks this button


        db = FirebaseFirestore.getInstance();


        db.collection("games").get().addOnSuccessListener(snapshot -> {
            boolean nameExists = false;
            for(DocumentSnapshot g : snapshot.getDocuments()){
                if (g.getId().equals(userCode))//check if the code the user entered was used in a different room
                    nameExists = true;
            }
            if (nameExists){
                Toast.makeText(getApplication(), "room name already taken", Toast.LENGTH_SHORT).show();
            }
            else {
                code = userCode;
                gameRef = db.collection("games").document(code);


                Map<String, Object> game = new HashMap<>();
                game.put("white", email);
                game.put("black", "");


                gameRef.set(game);
                clientIsBlack = false;


                Toast.makeText(getApplication(), "waiting for player to join", Toast.LENGTH_SHORT).show();


                listenToGame();
            }
        });
    }
    public void joinGame(String email, String userUuid){
        if (code != null) gameRef.delete();//if user already created new game
        if (Objects.equals(userUuid, code)) return;


        db = FirebaseFirestore.getInstance();
        code = userUuid;
        gameRef = db.collection("games").document(code);


        gameRef.get().addOnSuccessListener(snapshot -> {


            if (!snapshot.exists()) {//no game with given code
                Toast.makeText(getApplication(), "game not found", Toast.LENGTH_SHORT).show();
            } else {//found game
                String black = snapshot.getString("black");


                if (black == null || black.isEmpty()) {//if there is no value for black. white has to have a value
                    gameRef.update("black", email);
                    clientIsBlack = true;
                } else {
                    Toast.makeText(getApplication(), "game is already full", Toast.LENGTH_SHORT).show();
                }
            }


            listenToGame();
        });
    }
    public void exitGame(){
        if (gameRef != null){
            gameRef.get().addOnSuccessListener((snapshot) -> {
                gameRef.update(clientIsBlack ? "black" : "white", "");
            });
        }
    }//handles deleting game room after game ends


    private void listenToGame() {
        gameRef.addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || !snapshot.exists()) return;


            String white = (String) snapshot.get("white");
            String black = (String) snapshot.get("black");
            String move = (String) snapshot.get("move");




            if (!gameStarted && black != null && !black.isEmpty()){//check if second player joined.
                setGameStarted(true);
                this.white = white;
                this.black = black;
            }


            if (move != null && !move.isEmpty() && !move.equals(this.move)) {//move check
                setMove(move);
            }


            if (gameStarted && (white == null || white.isEmpty() || black == null || black.isEmpty())){//if player leaves in the middle of the game
                if (gameRef != null)
                    gameRef.delete();
                setGameStarted(false);
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
    public MutableLiveData<Boolean> getGameStarted() {
        return mutableGameStarted;
    }
    public String getWhite(){
        return white;
    }
    public String getBlack(){
        return black;
    }
    public boolean getClientIsBlack(){
        return clientIsBlack;
    }


    private void setMove(String move) {
        this.move = move;
        mutableMove.setValue(move);
    }
    private void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
        mutableGameStarted.postValue(gameStarted);
    }
}
