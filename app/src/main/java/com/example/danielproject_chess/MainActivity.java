package com.example.danielproject_chess;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainTL;
    private Board b;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        startBoard();

    }
    public void init(){
        mainTL = findViewById(R.id.board);
    }
    public void startBoard() {
        b = new Board(this, mainTL);
    }
}