package com.example.danielproject_chess;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Board{
    private Tile [][] tiles;
    private Tile selectedTile;
    private boolean isInCheck;
    private boolean blackTurn;
    private boolean isMoveAnalysed;
    private boolean clientIsBlack;
    private OkHttpClient client;
    private Request request;
    private Context c;
    private MainActivity mainActivity;

    public Board(Board b){
        tiles = new Tile[8][8];
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                tiles[j][i] = new Tile(b.getTiles()[j][i], this);
            }
        }
        selectedTile = new Tile(b.getSelectedTile(),this);
        isInCheck = true;
        blackTurn = b.isBlackTurn();
        client = null;
        c = b.getC();
    }
    public Board(MainActivity c, LinearLayout table, boolean clientIsBlack){
        this.clientIsBlack = clientIsBlack;
        blackTurn = false;
        isInCheck = false;
        isMoveAnalysed = false;
        client = new OkHttpClient();
        this.c = c;
        mainActivity = c;
        //formatting:
        tiles = new Tile[8][8];
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                tiles[j][i] = new Tile((ImageView) ((LinearLayout)table.getChildAt(7-i)).getChildAt(7-j), j, i, this);
            }
        }
        selectedTile = null;
        //building default chess board:
        // White pieces
        tiles[0][0].setPiece('r', false);
        tiles[1][0].setPiece('n', false);
        tiles[2][0].setPiece('b', false);
        tiles[3][0].setPiece('q', false);
        tiles[4][0].setPiece('k', false);
        tiles[5][0].setPiece('b', false);
        tiles[6][0].setPiece('n', false);
        tiles[7][0].setPiece('r', false);

        // Black pieces
        tiles[0][7].setPiece('r', true);
        tiles[1][7].setPiece('n', true);
        tiles[2][7].setPiece('b', true);
        tiles[3][7].setPiece('q', true);
        tiles[4][7].setPiece('k', true);
        tiles[5][7].setPiece('b', true);
        tiles[6][7].setPiece('n', true);
        tiles[7][7].setPiece('r', true);

        //Pawns
        for (int x = 0; x < 8; x++) {
            tiles[x][1].setPiece('p', false);
            tiles[x][6].setPiece('p', true);
        }
    }

    public void movePiece(Tile target){
        if (!isMoveAnalysed || clientIsBlack == blackTurn) {
            if (selectedTile != null && target.getIsHighlighted() && selectedTile.getIsBlack() == blackTurn && (target.getPieceType() == '1' || target.getIsBlack() != selectedTile.getIsBlack())) {
                if (!isInCheck || selectedTile.getPieceType() == 'k' || moveStopsCheck(target)) {
                    target.setPiece(selectedTile.getPieceType(), selectedTile.getIsBlack());
                    target.setHasMoved(true);
                    mainActivity.addMoveToDatabase(Integer.toString(selectedTile.getPosX()) + Integer.toString(selectedTile.getPosY()) + Integer.toString(target.getPosX()) + Integer.toString(target.getPosY()));

                    turnResets();
                    setBoardAttacks(blackTurn);

                    selectedTile.setPiece('1', true);
                    selectedTile = null;
                    isCheckmate();
                    blackTurn = !blackTurn;
                }
            } else {
                selectedTile = target;
                setTileHighlight(target);
            }
        }
        else
            Toast.makeText(c, "please wait", Toast.LENGTH_SHORT).show();
    }//todo: promoting pawns
    public void forceMovePiece(Tile target){
        target.setPiece(selectedTile.getPieceType(), selectedTile.getIsBlack());
        selectedTile.setPiece('1',true);
        resetHighlights();
        setBoardAttacks(!blackTurn);
    }
    public void getMove(String move){//format: OriginPosX + OriginPosY + TargetPosX + TargetPosY
        Tile o = tiles[move.charAt(0) - '0'][move.charAt(1) - '0'];
        Tile t = tiles[move.charAt(2) - '0'][move.charAt(3) - '0'];
        if (o.getPieceType() != '1') {//firebase will send back every move client sends, without this check - every moved piece will turn to an empty tile.
            t.setPiece(o.getPieceType(), o.getIsBlack());
            o.setPiece('1', true);
            blackTurn = !blackTurn;
        }
    }

    private void setTileHighlight(Tile tile){
        resetHighlights();

        if (tile.getIsBlack() != blackTurn) return;

        int x = tile.getPosX();
        int y = tile.getPosY();
        boolean isBlack = tile.getIsBlack();

        switch (tile.getPieceType()) {
            case '1': return;
            case 'p': addPawnMoves(x, y, isBlack,true); break;
            case 'n': addKnightMoves(x, y, isBlack,true); break;
            case 'b': addBishopMoves(x, y, isBlack,true); break;
            case 'r': addRookMoves(x, y, isBlack,true); break;
            case 'q': addQueenMoves(x, y, isBlack,true); break;
            case 'k': addKingMoves(x, y, isBlack,true); break;
        }
    }//uses setBoardHighlightAndAttack to only highlight the impact of a single piece
    private void setBoardAttacks(boolean blackTurn){
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                if (tiles[j][i].getIsBlack() == blackTurn)
                    setTileAttacks(tiles[j][i]);
            }
        }
    }//mark all attacked tiles on the board, used for check detection
    public void setTileAttacks(Tile tile){
        int x = tile.getPosX();
        int y = tile.getPosY();
        boolean isBlack = tile.getIsBlack();

        switch (tile.getPieceType()) {
            case '1': return;
            case 'p': addPawnMoves(x, y, isBlack,false); break;
            case 'n': addKnightMoves(x, y, isBlack,false); break;
            case 'b': addBishopMoves(x, y, isBlack,false); break;
            case 'r': addRookMoves(x, y, isBlack,false); break;
            case 'q': addQueenMoves(x, y, isBlack,false); break;
            case 'k': addKingMoves(x, y, isBlack,false); break;
        }
    }

    private void addPawnMoves(int x, int y, boolean isBlack, boolean forHighlight) {
        int dir = isBlack ? -1 : 1;
        int startRow = isBlack ? 6 : 1;


        if (forHighlight) {
            // forward
            if (inBounds(x, y + dir) && tiles[x][y + dir].getPieceType() == '1') {
                tiles[x][y + dir].setHighlighted(true);

                // double move
                if (y == startRow && tiles[x][y + 2 * dir].getPieceType() == '1') {
                    tiles[x][y + 2 * dir].setHighlighted(true);
                }
            }

            // captures
            if (inBounds(x + 1, y + dir) && tiles[x + 1][y + dir].getPieceType() != '1' && tiles[x + 1][y + dir].getIsBlack() != isBlack) {
                tiles[x + 1][y + dir].setHighlighted(true);
            }

            if (inBounds(x - 1, y + dir) && tiles[x - 1][y + dir].getPieceType() != '1' && tiles[x - 1][y + dir].getIsBlack() != isBlack) {
                tiles[x - 1][y + dir].setHighlighted(true);
            }
        }
        else{
            if (inBounds(x + 1, y + dir) && (tiles[x + 1][y + dir].getPieceType() != 'k' || tiles[x + 1][y + dir].getIsBlack() != isBlack)) {
                tiles[x + 1][y + dir].setAttacked(true);
            }

            if (inBounds(x - 1, y + dir) && (tiles[x - 1][y + dir].getPieceType() != 'k' || tiles[x - 1][y + dir].getIsBlack() != isBlack)) {
                tiles[x - 1][y + dir].setAttacked(true);
            }
        }
    }//todo: en-passant
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
        for (int dirX = -1; dirX <= 1; dirX++) {
            for (int dirY = -1; dirY <= 1; dirY++) {
                if (dirX == 0 && dirY == 0) continue;

                int targetX = x + dirX;
                int targetY = y + dirY;

                if (inBounds(targetX, targetY) && !(tiles[x][y].getPieceType() == 6 && tiles[targetX][targetY].getIsAttacked())) {
                    highlightIfEnemyOrEmpty(targetX, targetY, isBlack, forHighlight);
                }
            }
        }
    }//todo:castling

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }// used to mitigate out of bounds error on tiles array
    private void highlightIfEnemyOrEmpty(int x, int y, boolean isBlack, boolean forHighlight) {
        Tile t = tiles[x][y];
        if (forHighlight){
            if (t.getPieceType() == '1' || t.getIsBlack() != isBlack) {
                t.setHighlighted(true);
            }
        }
        else {
            if (t.getPieceType() != 'k' || t.getIsBlack() != isBlack) {
                t.setAttacked(true);
            }
        }
    }
    private void addSlidingMoves(int x, int y, boolean isBlack, int dirX, int dirY, boolean forHighlight) {
        int targetX = x + dirX;
        int targetY = y + dirY;

        while (inBounds(targetX, targetY)) {
            Tile t = tiles[targetX][targetY];

            if (forHighlight){
                if (t.getPieceType() == '1') {
                    t.setHighlighted(true);
                }
                else {
                    if (t.getIsBlack() != isBlack) {
                        t.setHighlighted(true);
                    }
                    break; // blocked
                }
            }
            else {
                if (t.getPieceType() == '1') {
                    t.setAttacked(true);
                }
                else {
                    if (t.getPieceType() != 'k' || t.getIsBlack() != isBlack) {
                        t.setAttacked(true);
                    }
                    break; // blocked
                }
            }


            targetX += dirX;
            targetY += dirY;
        }
    } //adds the functionality to determine velocity on a piece and find all its available squares without getting blocked

    private boolean moveStopsCheck(Tile target) {
        if (this.getTiles()[0][0].getImage() == null)//if this is a fake board, used to prevent infinite loop because moveStopsCheck calls movePiece and vice versa
            return false;
        Board temp = new Board(Board.this);
        Tile Ttarget = temp.getTiles()[target.getPosX()][target.getPosY()];
        temp.forceMovePiece(Ttarget);
        return !temp.isInCheck();
    }//returns whether the origin and target of the move will result in blocking the check/capturing the attacker
    private String createFen(){
        StringBuilder fen = new StringBuilder();
        int space = 1;

        for(int i=7; i>=0; i--){
            for(int j=0; j<8; j++){
                if (tiles[j][i].getPieceType() != '1')
                    if (!tiles[j][i].getIsBlack())
                        fen.append(Character.toUpperCase(tiles[j][i].getPieceType()));
                    else
                        fen.append(tiles[j][i].getPieceType());
                else if (j < 7 && tiles[j+1][i].getPieceType() == '1'){
                    space++;
                }
                else {
                    fen.append(space);
                    space = 1;
                }
            }
            if (i > 0)
                fen.append("/");
        }
        fen.append((blackTurn ? " b" : " w") + " - - 0 0");
        Log.d("fen", fen.toString());
        return fen.toString();
    }// todo: castling and en-passant
    private void isCheckmate() {
        isMoveAnalysed = true;
        Request request = new Request.Builder()
                .url(HttpUrl.parse("https://stockfish.online/api/s/v2.php")
                        .newBuilder()
                        .addQueryParameter("fen", createFen())
                        .addQueryParameter("depth", "15")
                        .build())
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(c, "Error reaching server, check your internet connection.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        if (new JSONObject(response.body().string()).getString("mate").equals("0"))
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(c, "Checkmate!", Toast.LENGTH_SHORT).show();
                            });
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                }
                isMoveAnalysed = false;
            }
        });
    }

    private void turnResets(){
        resetHighlights();
        resetAttacks();
    }
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

    public Context getC() {
        return c;
    }

    //setters
    public void setInCheck(boolean inCheck) {isInCheck = inCheck;}
}
