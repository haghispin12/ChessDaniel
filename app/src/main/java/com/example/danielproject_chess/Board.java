package com.example.danielproject_chess;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

public class Board{
    private Tile [][] tiles;
    private Tile selectedTile;
    private boolean isInCheck;
    private boolean blackTurn;
    private Context c;

    public Board(Context c, LinearLayout table){
        this.c = c;
        blackTurn = false;
        isInCheck = false;
        //formatting:
        tiles = new Tile[8][8];
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                tiles[j][i] = new Tile((ImageView) ((LinearLayout)table.getChildAt(7-i)).getChildAt(7-j), j, i, this);
            }
        }
        selectedTile = null;
        //building default chess board: TODO:add chess 960
        // White pieces
        tiles[0][0].setPiece(4, false);
        tiles[1][0].setPiece(2, false);
        tiles[2][0].setPiece(3, false);
        tiles[3][0].setPiece(6, false);
        tiles[4][0].setPiece(5, false);
        tiles[5][0].setPiece(3, false);
        tiles[6][0].setPiece(2, false);
        tiles[7][0].setPiece(4, false);

        // Black pieces
        tiles[0][7].setPiece(4, true);
        tiles[1][7].setPiece(2, true);
        tiles[2][7].setPiece(3, true);
        tiles[3][7].setPiece(6, true);
        tiles[4][7].setPiece(5, true);
        tiles[5][7].setPiece(3, true);
        tiles[6][7].setPiece(2, true);
        tiles[7][7].setPiece(4, true);

        //Pawns
        for (int x = 0; x < 8; x++) {
            tiles[x][1].setPiece(1, false);
            tiles[x][6].setPiece(1, true);
        }
    }

    public Tile[][] getTiles() {
        return tiles;
    }


    public void movePiece(Tile target){
        if(selectedTile != null && target.getIsHighlighted() && selectedTile.getIsBlack() ==  blackTurn &&(target.getPieceType() == 0 || target.getIsBlack() != selectedTile.getIsBlack())){
            target.setPiece(selectedTile.getPieceType(), selectedTile.getIsBlack());
            selectedTile.setPiece(0, true);
            selectedTile = null;
            resetHighlights();
            setBoardAttacks(blackTurn);
            Log.d("TAG", tiles[1][2].getIsAttacked() ? "true" : "false");
            blackTurn = !blackTurn;
        }
        else {
            selectedTile = target;
            setBoardHighlight(target);
        }
    }
    public void setBoardHighlightAndAttack(Tile tile){
        if (tile.getIsBlack() != blackTurn) return;

        int x = tile.getPosX();
        int y = tile.getPosY();
        boolean isBlack = tile.getIsBlack();

        switch (tile.getPieceType()) {
            case 0: return;
            case 1: addPawnMoves(x, y, isBlack); break;
            case 2: addKnightMoves(x, y, isBlack); break;
            case 3: addBishopMoves(x, y, isBlack); break;
            case 4: addRookMoves(x, y, isBlack); break;
            case 5: addQueenMoves(x, y, isBlack); break;
            case 6: addKingMoves(x, y, isBlack); break;
        }
    }//turns both isAttacked and isHighlighted to true

    public void setBoardHighlight(Tile tile){
        resetHighlights();
        setBoardHighlightAndAttack(tile);
        resetAttacks();
    }//uses setBoardHighlightAndAttack to only highlight the impact of a single piece
    public void setBoardAttacks(boolean byBlack){
        resetAttacks();
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                if (tiles[j][i].getIsBlack() == byBlack)
                    setBoardHighlightAndAttack(tiles[j][i]);
            }
        }
        resetHighlights();
    }// uses setBoardHighlightAndAttack to mark all attacked tiles on the board, uses for check and mate detection

    private void addPawnMoves(int x, int y, boolean isBlack) {
        int dir = isBlack ? -1 : 1;
        int startRow = isBlack ? 6 : 1;

        // forward
        if (inBounds(x, y + dir) && tiles[x][y + dir].getPieceType() == 0) {
            tiles[x][y + dir].setHighlighted(true);

            // double move
            if (y == startRow && tiles[x][y + 2 * dir].getPieceType() == 0) {
                tiles[x][y + 2 * dir].setHighlighted(true);
            }
        }

        // captures
        if (inBounds(x + 1, y + dir) && tiles[x + 1][y + dir].getPieceType() != 0 && tiles[x + 1][y + dir].getIsBlack() != isBlack) {
            tiles[x + 1][y + dir].setHighlighted(true);
        }
        else if (inBounds(x + 1, y + dir))
            tiles[x + 1][y + dir].setAttacked(true);

        if (inBounds(x - 1, y + dir) && tiles[x - 1][y + dir].getPieceType() != 0 && tiles[x - 1][y + dir].getIsBlack() != isBlack) {
            tiles[x - 1][y + dir].setHighlighted(true);
        }
        else if (inBounds(x - 1, y + dir))
            tiles[x - 1][y + dir].setAttacked(true);
    }
    private void addKnightMoves(int x, int y, boolean isBlack) {
        int[][] moves = {
                { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 },
                { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 }
        };

        for (int i = 0; i < moves.length; i++) {
            if (inBounds(x + moves[i][0], y + moves[i][1])) {
                highlightIfEnemyOrEmpty(x + moves[i][0], y + moves[i][1], isBlack);
            }
        }
    }
    private void addBishopMoves(int x, int y, boolean isBlack) {
        addSlidingMoves(x, y, isBlack,  1,  1);
        addSlidingMoves(x, y, isBlack,  1, -1);
        addSlidingMoves(x, y, isBlack, -1,  1);
        addSlidingMoves(x, y, isBlack, -1, -1);
    }
    private void addRookMoves(int x, int y, boolean isBlack) {
        addSlidingMoves(x, y, isBlack,  1,  0);
        addSlidingMoves(x, y, isBlack, -1,  0);
        addSlidingMoves(x, y, isBlack,  0,  1);
        addSlidingMoves(x, y, isBlack,  0, -1);
    }
    private void addQueenMoves(int x, int y, boolean isBlack) {
        addBishopMoves(x, y, isBlack);
        addRookMoves(x, y, isBlack);
    }
    private void addKingMoves(int x, int y, boolean isBlack) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;

                if (inBounds(nx, ny) && !(tiles[x][y].getPieceType() == 6 && tiles[nx][ny].getIsAttacked())) {
                    highlightIfEnemyOrEmpty(nx, ny, isBlack);
                }
            }
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }// used to mitigate out of bounds error on tiles array
    private void highlightIfEnemyOrEmpty(int x, int y, boolean isBlack) {
        Tile t = tiles[x][y];
        if (t.getPieceType() == 0 || t.getIsBlack() != isBlack) {
            t.setHighlighted(true);
            t.setAttacked(true);
        }
    }
    private void addSlidingMoves(int x, int y, boolean isBlack, int dirX, int dirY) {
        int targetX = x + dirX;
        int targetY = y + dirY;

        while (inBounds(targetX, targetY)) {
            Tile t = tiles[targetX][targetY];

            if (t.getPieceType() == 0) {
                t.setHighlighted(true);
                t.setAttacked(true);
            } else {
                if (t.getIsBlack() != isBlack) {
                    t.setHighlighted(true);
                    t.setAttacked(true);
                }
                break; // blocked
            }

            targetX += dirX;
            targetY += dirY;
        }
    } //adds the functionality to determine velocity on a piece and find all its available squares without getting blocked

    private Tile findKing(boolean isBlack) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Tile t = tiles[x][y];
                if (t.getPieceType() == 6 && t.getIsBlack() == isBlack) {
                    return t;
                }
            }
        }
        return null;
    }//returns location of king, used for check and mate detection

    public void resetHighlights(){
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                tiles[i][j].setHighlighted(false);
            }
        }
    }
    public void resetAttacks(){
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                tiles[i][j].setAttacked(false);
            }
        }
    }
}
