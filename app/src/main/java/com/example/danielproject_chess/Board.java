package com.example.danielproject_chess;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class Board{
    private Tile [][] tiles;
    private Tile selectedTile;
    private boolean isInCheck;
    private boolean blackTurn;
    private Context c;

    public Board(Board b){
        tiles = new Tile[8][8];
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                tiles[j][i] = new Tile(b.getTiles()[j][i]);
            }
        }
        selectedTile = new Tile(b.getSelectedTile());
        isInCheck = true;
        blackTurn = b.isBlackTurn();
    }
    public Board(Context c, LinearLayout table){
        blackTurn = false;
        isInCheck = false;
        this.c = c;
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

    public void movePiece(Tile target){
        if(selectedTile != null && target.getIsHighlighted() && selectedTile.getIsBlack() ==  blackTurn && (target.getPieceType() == 0 || target.getIsBlack() != selectedTile.getIsBlack())){
            if(!isInCheck || selectedTile.getPieceType() == 6 || moveStopsCheck(target)) {
                target.setPiece(selectedTile.getPieceType(), selectedTile.getIsBlack());
                selectedTile.setPiece(0, true);
                selectedTile = null;
                resetHighlights();
                setBoardAttacks(blackTurn);
                blackTurn = !blackTurn;
                Toast.makeText(c,isInCheck ? "true" : "false", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            selectedTile = target;
            setTileHighlight(target);
        }
    }

    private void setTileHighlight(Tile tile){
        resetHighlights();

        if (tile.getIsBlack() != blackTurn) return;

        int x = tile.getPosX();
        int y = tile.getPosY();
        boolean isBlack = tile.getIsBlack();

        switch (tile.getPieceType()) {
            case 0: return;
            case 1: addPawnMoves(x, y, isBlack,true); break;
            case 2: addKnightMoves(x, y, isBlack,true); break;
            case 3: addBishopMoves(x, y, isBlack,true); break;
            case 4: addRookMoves(x, y, isBlack,true); break;
            case 5: addQueenMoves(x, y, isBlack,true); break;
            case 6: addKingMoves(x, y, isBlack,true); break;
        }
    }//uses setBoardHighlightAndAttack to only highlight the impact of a single piece
    private void setBoardAttacks(boolean byBlack){
        resetAttacks();
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                if (tiles[j][i].getIsBlack() == byBlack)
                    setTileAttacks(byBlack, tiles[j][i]);
            }
        }
        resetHighlights();
    }// uses setBoardHighlightAndAttack to mark all attacked tiles on the board, uses for check and mate detection
    public void setTileAttacks(boolean byBlack, Tile tile){
        int x = tile.getPosX();
        int y = tile.getPosY();
        boolean isBlack = tile.getIsBlack();

        switch (tile.getPieceType()) {
            case 0: return;
            case 1: addPawnMoves(x, y, isBlack,false); break;
            case 2: addKnightMoves(x, y, isBlack,false); break;
            case 3: addBishopMoves(x, y, isBlack,false); break;
            case 4: addRookMoves(x, y, isBlack,false); break;
            case 5: addQueenMoves(x, y, isBlack,false); break;
            case 6: addKingMoves(x, y, isBlack,false); break;
        }
    }

    private void addPawnMoves(int x, int y, boolean isBlack, boolean forHighlight) {
        int dir = isBlack ? -1 : 1;
        int startRow = isBlack ? 6 : 1;

        if (forHighlight) {
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

            if (inBounds(x - 1, y + dir) && tiles[x - 1][y + dir].getPieceType() != 0 && tiles[x - 1][y + dir].getIsBlack() != isBlack) {
                tiles[x - 1][y + dir].setHighlighted(true);
            }
        }
        else{
            if (inBounds(x + 1, y + dir) && (tiles[x + 1][y + dir].getPieceType() == 0 || tiles[x + 1][y + dir].getIsBlack() != isBlack)) {
                tiles[x + 1][y + dir].setAttacked(true);
            }

            if (inBounds(x - 1, y + dir) && (tiles[x - 1][y + dir].getPieceType() == 0 || tiles[x - 1][y + dir].getIsBlack() != isBlack)) {
                tiles[x - 1][y + dir].setAttacked(true);
            }
        }
    }
    private void addKnightMoves(int x, int y, boolean isBlack, boolean forHighlight) {
        int[][] moves = {
                { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 },
                { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 }
        };

        for (int i = 0; i < moves.length; i++) {
            if (inBounds(x + moves[i][0], y + moves[i][1])) {
                highlightIfEnemyOrEmpty(x + moves[i][0], y + moves[i][1], isBlack, forHighlight);
            }
        }
    }
    private void addBishopMoves(int x, int y, boolean isBlack, boolean forHighlight) {
        addSlidingMoves(x, y, isBlack,  1,  1, forHighlight);
        addSlidingMoves(x, y, isBlack,  1, -1, forHighlight);
        addSlidingMoves(x, y, isBlack, -1,  1, forHighlight);
        addSlidingMoves(x, y, isBlack, -1, -1, forHighlight);
    }
    private void addRookMoves(int x, int y, boolean isBlack, boolean forHighlight) {
        addSlidingMoves(x, y, isBlack,  1,  0, forHighlight);
        addSlidingMoves(x, y, isBlack, -1,  0, forHighlight);
        addSlidingMoves(x, y, isBlack,  0,  1, forHighlight);
        addSlidingMoves(x, y, isBlack,  0, -1, forHighlight);
    }
    private void addQueenMoves(int x, int y, boolean isBlack, boolean forHighlight) {
        addBishopMoves(x, y, isBlack, forHighlight);
        addRookMoves(x, y, isBlack, forHighlight);
    }
    private void addKingMoves(int x, int y, boolean isBlack, boolean forHighlight) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;

                if (inBounds(nx, ny) && !(tiles[x][y].getPieceType() == 6 && tiles[nx][ny].getIsAttacked())) {
                    highlightIfEnemyOrEmpty(nx, ny, isBlack, forHighlight);
                }
            }
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }// used to mitigate out of bounds error on tiles array
    private void highlightIfEnemyOrEmpty(int x, int y, boolean isBlack, boolean forHighlight) {
        Tile t = tiles[x][y];
        if (t.getPieceType() == 0 || t.getIsBlack() != isBlack) {
            if (forHighlight)
                t.setHighlighted(true);
            else
                t.setAttacked(true);
        }
    }
    private void addSlidingMoves(int x, int y, boolean isBlack, int dirX, int dirY, boolean forHighlight) {
        int targetX = x + dirX;
        int targetY = y + dirY;

        while (inBounds(targetX, targetY)) {
            Tile t = tiles[targetX][targetY];

            if (t.getPieceType() == 0) {
                if (forHighlight)
                    t.setHighlighted(true);
                else
                    t.setAttacked(true);
            } else {
                if (t.getIsBlack() != isBlack) {
                    if (forHighlight)
                        t.setHighlighted(true);
                    else
                        t.setAttacked(true);
                }
                break; // blocked
            }

            targetX += dirX;
            targetY += dirY;
        }
    } //adds the functionality to determine velocity on a piece and find all its available squares without getting blocked

    private boolean moveStopsCheck(Tile target) {
        if (this.getTiles()[0][0].getImage() == null)//if this is a fake board, used to prevent infinite loop because moveStopsCheck calls movePiece and vice versa
            return false;
        Board temp = new Board(Board.this);
        temp.movePiece(target);
        return !temp.isInCheck();
    }//returns whether the origin and target of the move will result in blocking the check/capturing the attacker

    private void resetHighlights(){
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                tiles[i][j].setHighlighted(false);
            }
        }
    }
    private void resetAttacks(){
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                tiles[i][j].setAttacked(false);
            }
        }
    }

    //getters
    public Tile[][] getTiles() {
        return tiles;
    }
    public boolean isBlackTurn() {
        return blackTurn;
    }
    public boolean isInCheck() {
        return isInCheck;
    }
    public Tile getSelectedTile() {
        return selectedTile;
    }

    //setters
    public void setInCheck(boolean inCheck) {isInCheck = inCheck;}
    public void setSelectedTile(Tile tile) {
        this.selectedTile = tile;
    }

    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }
}
