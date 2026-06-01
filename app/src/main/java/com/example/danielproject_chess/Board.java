package com.example.danielproject_chess;

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
    private Context c;
    private MainActivity mainActivity;

    public Board(Board b){
        tiles = new Tile[8][8];
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                tiles[j][i] = new Tile(b.getTiles()[j][i], this);
            }
        }
        isInCheck = b.isInCheck;
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
        if (!isMoveAnalysed && clientIsBlack == blackTurn) {
            if (selectedTile != null && target.getIsHighlighted() && (target.getPieceType() == Tile.EMPTY || target.getIsBlack() != selectedTile.getIsBlack())) {
                mainActivity.addMoveToDatabase(Integer.toString(selectedTile.getPosX()) + Integer.toString(selectedTile.getPosY()) + Integer.toString(target.getPosX()) + Integer.toString(target.getPosY()));
                selectedTile = null;
                //setMove() will handle the rest
            } else {
                selectedTile = target;
                setTileHighlight(target);
            }
        }
        else
            Toast.makeText(c, "please wait", Toast.LENGTH_SHORT).show();
    }//todo: promoting pawns
    public void setMove(@NonNull String move){
        Tile o = tiles[move.charAt(0) - '0'][move.charAt(1) - '0'];
        Tile t = tiles[move.charAt(2) - '0'][move.charAt(3) - '0'];

        if (enPassant(o, t)){
            t.setPiece(o.getPieceType(), o.getIsBlack());
            tiles[t.getPosX()][o.getPosY()].setPiece(Tile.EMPTY, true);
            o.setPiece(Tile.EMPTY, true);
        }
        else if (castle(o, t)) {
            t.setPiece(o.getPieceType(), o.getIsBlack());
            o.setPiece(Tile.EMPTY, true);

            if (t.getPosX() == 2){// queen-side castle
                tiles[3][t.getPosY()].setPiece(Tile.ROOK, t.getIsBlack());
                tiles[0][t.getPosY()].setPiece(Tile.EMPTY, true);
            }
            else {
                tiles[5][t.getPosY()].setPiece(Tile.ROOK, t.getIsBlack());
                tiles[7][t.getPosY()].setPiece(Tile.EMPTY, true);
            }
        }
        else {
            t.setPiece(o.getPieceType(), o.getIsBlack());
            o.setPiece(Tile.EMPTY, true);
        }
            t.setNumOfMoves(o.getNumOfMoves() + 1);

            turnResets();
            setBoardAttacks(blackTurn);
            blackTurn = !blackTurn;
            isCheckmate();
    }//format: OriginPosX + OriginPosY + TargetPosX + TargetPosY
    private boolean enPassant(Tile o, Tile t){
        return t.getPieceType() == Tile.EMPTY && o.getPieceType() == Tile.PAWN && t.getPosX() != o.getPosX(); //assuming passed all other tests to get here
    }
    private boolean castle(Tile o, Tile t){
        return o.getPieceType() == Tile.KING && Math.abs(o.getPosX() - t.getPosX()) == 2; //assuming passed all other tests to get here
    }

    private void setTileHighlight(@NonNull Tile tile){
        resetHighlights();

        if (tile.getIsBlack() != blackTurn) return;

        int x = tile.getPosX();
        int y = tile.getPosY();
        boolean isBlack = tile.getIsBlack();

        switch (tile.getPieceType()) {
            case Tile.EMPTY: return;
            case Tile.PAWN: addPawnMoves(x, y, isBlack,true); break;
            case Tile.KNIGHT: addKnightMoves(x, y, isBlack,true); break;
            case Tile.BISHOP: addBishopMoves(x, y, isBlack,true); break;
            case Tile.ROOK: addRookMoves(x, y, isBlack,true); break;
            case Tile.QUEEN: addQueenMoves(x, y, isBlack,true); break;
            case Tile.KING: addKingMoves(x, y, isBlack,true); break;
        }
    }//uses setBoardHighlightAndAttack to only highlight the impact of a single piece
    private void setBoardAttacks(boolean forBlack){
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                if (tiles[j][i].getIsBlack() == forBlack)
                    setTileAttacks(tiles[j][i]);
            }
        }
    }//mark all attacked tiles on the board, used for check detection
    public void setTileAttacks(@NonNull Tile tile){
        int x = tile.getPosX();
        int y = tile.getPosY();
        boolean isBlack = tile.getIsBlack();

        switch (tile.getPieceType()) {
            case Tile.EMPTY: return;
            case Tile.PAWN: addPawnMoves(x, y, isBlack,false); break;
            case Tile.KNIGHT: addKnightMoves(x, y, isBlack,false); break;
            case Tile.BISHOP: addBishopMoves(x, y, isBlack,false); break;
            case Tile.ROOK: addRookMoves(x, y, isBlack,false); break;
            case Tile.QUEEN: addQueenMoves(x, y, isBlack,false); break;
            case Tile.KING: addKingMoves(x, y, isBlack,false); break;
        }
    }

    private void addPawnMoves(int x, int y, boolean isBlack, boolean forHighlight) {
        int dir = isBlack ? -1 : 1;
        int startRow = isBlack ? 6 : 1;


        if (forHighlight) {
            // forward
            if (inBounds(x, y + dir) && tiles[x][y + dir].getPieceType() == Tile.EMPTY) {
                if(moveStopsCheck(tiles[x][y], tiles[x][y + dir]))//double move check has to execute even when first move fails
                    tiles[x][y + dir].setHighlighted(true);

                // double move
                if (y == startRow && tiles[x][y + 2 * dir].getPieceType() == Tile.EMPTY && moveStopsCheck(tiles[x][y], tiles[x][y + 2*dir])) {
                    tiles[x][y + 2 * dir].setHighlighted(true);
                }
            }

            // captures
            if (inBounds(x + 1, y + dir) && tiles[x + 1][y + dir].getPieceType() != Tile.EMPTY && tiles[x + 1][y + dir].getIsBlack() != isBlack && moveStopsCheck(tiles[x][y], tiles[x + 1][y + dir])) {
                tiles[x + 1][y + dir].setHighlighted(true);
            }

            if (inBounds(x - 1, y + dir) && tiles[x - 1][y + dir].getPieceType() != Tile.EMPTY && tiles[x - 1][y + dir].getIsBlack() != isBlack && moveStopsCheck(tiles[x][y], tiles[x - 1][y + dir])) {
                tiles[x - 1][y + dir].setHighlighted(true);
            }

            // en-passant
            if (y == (isBlack ? 3 : 4) && inBounds(x + 1, y + dir) && tiles[x + 1][y + dir].getPieceType() == Tile.EMPTY && tiles[x + 1][y].getNumOfMoves() == 1 && tiles[x + 1][y].getPieceType() == Tile.PAWN){
                tiles[x + 1][y + dir].setHighlighted(true);
            }
            if (y == (isBlack ? 3 : 4) && inBounds(x - 1, y + dir) && tiles[x - 1][y + dir].getPieceType() == Tile.EMPTY && tiles[x - 1][y].getNumOfMoves() == 1 && tiles[x - 1][y].getPieceType() == Tile.PAWN){
                tiles[x - 1][y + dir].setHighlighted(true);
            }
        }
        else{
            if (inBounds(x + 1, y + dir) && (tiles[x + 1][y + dir].getPieceType() != Tile.KING || tiles[x + 1][y + dir].getIsBlack() != isBlack)) {
                tiles[x + 1][y + dir].setAttacked(true);
            }

            if (inBounds(x - 1, y + dir) && (tiles[x - 1][y + dir].getPieceType() != Tile.KING || tiles[x - 1][y + dir].getIsBlack() != isBlack)) {
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
            if (inBounds(x + moves[i][0], y + moves[i][1]) && moveStopsCheck(tiles[x][y], tiles[x + moves[i][0]][y + moves[i][1]])) {
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

                if (inBounds(targetX, targetY) && (!tiles[targetX][targetY].getIsAttacked() || moveStopsCheck(tiles[x][y], tiles[targetX][targetY]))) {
                    highlightIfEnemyOrEmpty(targetX, targetY, isBlack, forHighlight);
                }
            }
        }
        if (tiles[x][y].getNumOfMoves() == 0 && !tiles[x][y].getIsAttacked()
                && tiles[1][y].getPieceType() == Tile.EMPTY
                && tiles[2][y].getPieceType() == Tile.EMPTY && !tiles[2][y].getIsAttacked()
                && tiles[3][y].getPieceType() == Tile.EMPTY && !tiles[3][y].getIsAttacked()
                && tiles[0][y].getPieceType() == Tile.ROOK && tiles[0][y].getNumOfMoves() == 0){
            tiles[2][y].setHighlighted(true);
        }
        if (tiles[x][y].getNumOfMoves() == 0 && !tiles[x][y].getIsAttacked()
                && tiles[5][y].getPieceType() == Tile.EMPTY && !tiles[1][y].getIsAttacked()
                && tiles[6][y].getPieceType() == Tile.EMPTY && !tiles[2][y].getIsAttacked()
                && tiles[7][y].getPieceType() == Tile.ROOK && tiles[7][y].getNumOfMoves() == 0){
            tiles[6][y].setHighlighted(true);
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }// used to mitigate out of bounds error on tiles array
    private void highlightIfEnemyOrEmpty(int x, int y, boolean isBlack, boolean forHighlight) {
        Tile t = tiles[x][y];
        if (forHighlight){
            if (t.getPieceType() == Tile.EMPTY || t.getIsBlack() != isBlack) {
                t.setHighlighted(true);
            }
        }
        else {
            if (t.getPieceType() == Tile.EMPTY || t.getIsBlack() != isBlack) {
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
                if ((t.getPieceType() == Tile.EMPTY || t.getIsBlack() != isBlack) && moveStopsCheck(tiles[x][y], tiles[targetX][targetY])) {
                    t.setHighlighted(true);
                }
                if (t.getPieceType() != Tile.EMPTY) {
                    break; // blocked
                }
            }
            else {
                if (t.getPieceType() == Tile.EMPTY || t.getIsBlack() != isBlack){
                    t.setAttacked(true);
                }
                if (!(t.getPieceType() == Tile.EMPTY || (t.getPieceType() == Tile.KING && t.getIsBlack() != isBlack))) {
                    break; // blocked
                }
            }


            targetX += dirX;
            targetY += dirY;
        }
    } //adds the functionality to determine velocity on a piece and find all its available squares without getting blocked

    private boolean moveStopsCheck(Tile origin, Tile target) {
        if (tiles[0][0].getImage() == null) return false; //return false for the check called by the secondary board, because a king move can't cause a new check.
        Board temp = new Board(Board.this);
        Tile Torigin = temp.getTiles()[origin.getPosX()][origin.getPosY()];
        Tile Ttarget = temp.getTiles()[target.getPosX()][target.getPosY()];
        Ttarget.setPiece(Torigin.getPieceType(), Torigin.getIsBlack());
        Torigin.setPiece(Tile.EMPTY, true);
        temp.resetAttacks();
        temp.setBoardAttacks(!blackTurn);
        return !temp.isInCheck();
    }//returns whether the origin and target of the move will make the king not in check
    private String createFen(){
        StringBuilder fen = new StringBuilder();
        int space = 1;

        for(int i=7; i>=0; i--){
            for(int j=0; j<8; j++){
                if (tiles[j][i].getPieceType() != Tile.EMPTY)
                    if (!tiles[j][i].getIsBlack())
                        fen.append(Character.toUpperCase(tiles[j][i].getPieceType()));
                    else
                        fen.append(tiles[j][i].getPieceType());
                else if (j < 7 && tiles[j+1][i].getPieceType() == Tile.EMPTY){
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
        fen.append((blackTurn ? " b" : " w") + " - ");//turn + castling rights

        boolean noPassant = true;
        for (int i=0; i<8; i++){
            if (!blackTurn && tiles[i][2].getPieceType() == Tile.EMPTY && tiles[i][3].getPieceType() == Tile.PAWN){
                fen.append('a' + i);
                fen.append('3');
                noPassant = false;
                break;//only one en-passant can be present at a time
            }
            if (blackTurn && tiles[i][5].getPieceType() == Tile.EMPTY && tiles[i][4].getPieceType() == Tile.PAWN){
                fen.append('a' + i);
                fen.append('6');
                noPassant = false;
                break;//only one en-passant can be present at a time
            }//todo: check that turn detection is correct for en passant
        }
        if (noPassant)
            fen.append("-");
        fen.append(" 0 0");//tie counters
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
                        if (new JSONObject(response.body().string()).getString("mate").equals("0")) {
                            if (isInCheck)
                                mainActivity.endGame(blackTurn ? "black won" : "white won", blackTurn ? "black" : "white");
                            else mainActivity.endGame("tie", null);
                        }
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
        resetEnPassant();
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
    private void resetEnPassant(){
        for(int i=0; i<8; i++){
            if (tiles[i][3].getPieceType() == Tile.PAWN)
                tiles[i][3].setNumOfMoves(2);//will never be 1
            if (tiles[i][4].getPieceType() == Tile.PAWN)
                tiles[i][4].setNumOfMoves(2);//will never be 1
        }
    }//en passant only lasts for that move alone

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
    public Context getC() {
        return c;
    }

    //setters
    public void setInCheck(boolean inCheck) {isInCheck = inCheck;}
}
