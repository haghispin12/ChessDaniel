package com.example.danielproject_chess;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

public class Board {
    private Tile [][] tiles;
    private Tile selectedTile;
    private boolean isInCheck;
    private Context c;

    public Board(Context c, LinearLayout table){
        this.c = c;
        //formatting:
        tiles = new Tile[8][8];
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                tiles[j][i] = new Tile((ImageView) ((LinearLayout)table.getChildAt(7-i)).getChildAt(7-j), 7-j, 7-i, this);
            }
        }
        selectedTile = null;
        //building default chess board: TODO:add chess 960
        // White pieces
        tiles[0][0].setPiece(4, false);
        tiles[1][0].setPiece(2, false);
        tiles[2][0].setPiece(3, false);
        tiles[3][0].setPiece(5, false);
        tiles[4][0].setPiece(6, false);
        tiles[5][0].setPiece(3, false);
        tiles[6][0].setPiece(2, false);
        tiles[7][0].setPiece(4, false);

        // Black pieces
        tiles[0][7].setPiece(4, true);
        tiles[1][7].setPiece(2, true);
        tiles[2][7].setPiece(3, true);
        tiles[3][7].setPiece(5, true);
        tiles[4][7].setPiece(6, true);
        tiles[5][7].setPiece(3, true);
        tiles[6][7].setPiece(2, true);
        tiles[7][7].setPiece(4, true);

        //Pawns
        for (int x = 0; x < 8; x++) {
            tiles[x][1].setPiece(1, false);
            tiles[x][6].setPiece(1, true);
        }
    }
    public void movePiece(Tile tile){
        if(selectedTile != null && selectedTile.getIsHighlighted() && (tile.getPieceType() == 0 || tile.getIsBlack() != selectedTile.getIsBlack())){
            tile.setPiece(selectedTile.getPieceType(), selectedTile.getIsBlack());
            selectedTile.setPiece(0, true);
            selectedTile = null;
        }
        else {
            selectedTile = tile;
            setBoardHighlight(tile);
        }
    }
    public void setBoardHighlight(Tile tile){
        int pieceType = tile.getPieceType();
        int posX = tile.getPosX();
        int posY = tile.getPosY();
        switch (pieceType){
            case 1:
                if (tile.getIsBlack()){
                    if (posY == 6){
                        tiles[posX][5].setHighlighted(true);
                        tiles[posX][4].setHighlighted(true);
                    }
                    else
                        tiles[posX][posY-1].setHighlighted(true);
                }
                else{
                    if (posY == 1){
                        tiles[posX][2].setHighlighted(true);
                        tiles[posX][3].setHighlighted(true);
                    }
                    else
                        tiles[posX][posY+1].setHighlighted(true);
                }
                break;
            case 2:
                if (posX + 2 < 8) {
                    if (posY + 1 < 8)
                        tiles[posX + 2][posY + 1].setHighlighted(true);
                    if (posY - 1 >= 0)
                        tiles[posX + 2][posY - 1].setHighlighted(true);
                }
                if (posX - 2 >= 0){
                    if (posY + 1 < 8)
                        tiles[posX - 2][posY + 1].setHighlighted(true);
                    if (posY - 1 >= 0)
                        tiles[posX - 2][posY - 1].setHighlighted(true);
                }
                if (posY + 2 < 8){
                    if (posX + 1 < 8)
                        tiles[posX+1][posY+2].setHighlighted(true);
                    if (posX - 1 >= 0)
                        tiles[posX-1][posY+2].setHighlighted(true);
                }
                if (posY - 2 >= 0){
                    if (posX + 1 < 8)
                        tiles[posX+1][posY-2].setHighlighted(true);
                    if (posX - 1 >= 0)
                        tiles[posX-1][posY-2].setHighlighted(true);
                }
                break;
            case 3:
                for (int i=1; i<8; i++){
                    if (posX + i < 8 && posY + i < 8)
                        tiles[posX+i][posY+i].setHighlighted(true);
                    if (posX + i < 8 && posY - i >= 0)
                        tiles[posX+i][posY-i].setHighlighted(true);
                    if (posX - i >= 0 && posY + i < 8)
                        tiles[posX-i][posY+i].setHighlighted(true);
                    if (posX - i >= 0 && posY - i >= 0)
                        tiles[posX-i][posY-i].setHighlighted(true);
                }
                break;
            case 4:
                for (int i=0; i<8; i++){
                    if (posX != i)
                        tiles[i][posY].setHighlighted(true);
                    if (posY != i)
                        tiles[posX][i].setHighlighted(true);
                }
                break;
            case 5:
                for (int i=0; i<8; i++){
                    if (posX != i)
                        tiles[i][posY].setHighlighted(true);
                    if (posY != i)
                        tiles[posX][i].setHighlighted(true);
                    if (posX + i < 8 && posY + i < 8)
                        tiles[posX+i][posY+i].setHighlighted(true);
                    if (posX + i < 8 && posY - i >= 0)
                        tiles[posX+i][posY-i].setHighlighted(true);
                    if (posX - i >= 0 && posY + i < 8)
                        tiles[posX-i][posY+i].setHighlighted(true);
                    if (posX - i >= 0 && posY - i >= 0)
                        tiles[posX-i][posY-i].setHighlighted(true);
                }
                break;
            case 6:
                if (posX + 1 < 8) {
                    if (posY + 1 < 8)
                        tiles[posX + 1][posY + 1].setHighlighted(true);
                    if (posY - 1 >= 0)
                        tiles[posX + 1][posY - 1].setHighlighted(true);
                }
                if (posX - 1 >= 0){
                    if (posY + 1 < 8)
                        tiles[posX - 1][posY + 1].setHighlighted(true);
                    if (posY - 1 >= 0)
                        tiles[posX - 1][posY - 1].setHighlighted(true);
                }
                if (posX + 1 < 8)
                    tiles[posX+1][posY].setHighlighted(true);
                if (posX - 1 >= 0)
                    tiles[posX-1][posY].setHighlighted(true);
                if (posY + 1 < 8)
                    tiles[posX][posY+1].setHighlighted(true);
                if (posY - 1 >= 0)
                    tiles[posX][posY-1].setHighlighted(true);
                break;
        }
    }
}
