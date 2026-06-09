package com.example.danielproject_chess;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends AppCompatActivity {


    private DBManager dbManager;
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);//getChildAt() is directional
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();
        startLobby();
        welcomeUser();
    }
    @Override
    protected void onStop() {
        super.onStop();


        dbManager.exitGame();
    }//handles user leaving the app


    private void welcomeUser(){
        email = getIntent().getStringExtra("email");
        Toast.makeText(this, "Welcome, " + email, Toast.LENGTH_SHORT).show();
    }


    private void init(){
        email = getIntent().getStringExtra("email");
        dbManager = new ViewModelProvider(this).get(DBManager.class);


        valChangeListeners();
    }
    private void valChangeListeners(){
        AtomicBoolean firstCall = new AtomicBoolean(true);


        dbManager.getGameStarted().observe(this, gameStarted -> {
            if (gameStarted) {
                startBoard();
                return;
            }
            if (firstCall.getAndSet(false)) {
                return;
            }
            Toast.makeText(MainActivity.this, "game ended", Toast.LENGTH_SHORT).show();
            Intent inn = new Intent(MainActivity.this, MainActivity.class);
            inn.putExtra("email", email);
            startActivity(inn);//to reset activity and view model
        });
    }//observers for online value changes


    private void startLobby(){
        LobbyFragment LF = new LobbyFragment();
        Bundle b = new Bundle();
        b.putString("email", email);
        LF.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, LF).commit();
    }
    private void startBoard(){
        BoardFragment BF = new BoardFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, BF).commit();
    }
}
