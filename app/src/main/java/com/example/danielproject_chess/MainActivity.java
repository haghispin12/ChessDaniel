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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainLayout;
    private Board b;
    private DatabaseReference gameRef;
    private String email;
    private boolean clientIsBlack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        launchLogin();
        createGameAndListener();
        startBoard();

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
        gameRef = FirebaseDatabase.getInstance()
                .getReference("games/game1");
        gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                String white = snapshot.child("white").getValue(String.class);
                String black = snapshot.child("black").getValue(String.class);

                if (white == null) {
                    gameRef.child("white").setValue(email);
                    clientIsBlack = false;

                } else if (black == null || black.equals("waiting")) {
                    gameRef.child("black").setValue(email);
                    clientIsBlack = true;

                } else {
                    Toast.makeText(MainActivity.this, "game already has two players", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
        gameRef.child("moves").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                String move = snapshot.getValue(String.class);

                //apply move to board
                b.getMove(move);
            }

            @Override public void onChildChanged(DataSnapshot snapshot, String prev) {}
            @Override public void onChildRemoved(DataSnapshot snapshot) {}
            @Override public void onChildMoved(DataSnapshot snapshot, String prev) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    public void addMoveToDatabase(String move){
        gameRef.child("moves").push().setValue(move);
    }
    public void startBoard() {
        b = new Board(this, mainLayout, clientIsBlack);
    }
}