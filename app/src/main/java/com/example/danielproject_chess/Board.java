package com.example.danielproject_chess;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

public class Board {
    private Tile [][] tiles;
    private boolean isInCheck;
    private Context c;

    public Board(Context c, TableLayout table){
        this.c = c;
        //formatting:
        tiles = new Tile[8][8];
        for(int i=0; i<8; i++){
            for(int j=0; j<0; j++){
                tiles[i][j] = new Tile((ImageView) ((TableRow)table.getChildAt(i)).getChildAt(j), i, j);
            }
        }
        //building default chess board: TODO:add chess 960;

    }
}
