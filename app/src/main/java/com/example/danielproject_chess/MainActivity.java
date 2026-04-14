package com.example.danielproject_chess;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainLayout;
    private Board b;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        launchLogin();
        startBoard();

    }
    public void init(){
        mainLayout = findViewById(R.id.board);
    }
    public void launchLogin(){

    }
    public void startBoard() {
        b = new Board(this, mainLayout);
    }
}