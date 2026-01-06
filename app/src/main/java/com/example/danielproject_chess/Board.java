package com.example.danielproject_chess;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

public class Board {
    private Tile [][] tiles;
    private Tile selectedTile;
    private boolean isInCheck;
    private Context c;

    public Board(Context c, TableLayout table){
        this.c = c;
        //formatting:
        tiles = new Tile[8][8];
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                tiles[j][i] = new Tile((ImageView) ((TableRow)table.getChildAt(7-i)).getChildAt(7-j), 7-j, 7-i);
            }
        }
        selectedTile = null;
        //building default chess board: TODO:add chess 960

        tiles[0][2].setPiece(1,R.drawable.w_pawn);
//        // White pieces
//        tiles[0][0].setPiece(4, R.drawable.w_rook);
//        tiles[1][0].setPiece(2, R.drawable.w_knight);
//        tiles[2][0].setPiece(3, R.drawable.w_bishop);
//        tiles[3][0].setPiece(5, R.drawable.w_queen);
//        tiles[4][0].setPiece(6, R.drawable.w_king);
//        tiles[5][0].setPiece(3, R.drawable.w_bishop);
//        tiles[6][0].setPiece(2, R.drawable.w_knight);
//        tiles[7][0].setPiece(4, R.drawable.w_rook);
//
//        // Black pieces
//        tiles[0][7].setPiece(4, R.drawable.b_rook);
//        tiles[1][7].setPiece(2, R.drawable.b_knight);
//        tiles[2][7].setPiece(3, R.drawable.b_bishop);
//        tiles[3][7].setPiece(5, R.drawable.b_queen);
//        tiles[4][7].setPiece(6, R.drawable.b_king);
//        tiles[5][7].setPiece(3, R.drawable.b_bishop);
//        tiles[6][7].setPiece(2, R.drawable.b_knight);
//        tiles[7][7].setPiece(4, R.drawable.b_rook);
//
//        //Pawns
//        for (int x = 0; x < 8; x++) {
//            tiles[x][1].setPiece(1, R.drawable.w_pawn);
//            tiles[x][6].setPiece(1, R.drawable.b_pawn);
//        }
    }
}
