package com.example.danielproject_chess;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;

public class Tile {
    private ImageView image;
    private char pieceType;//1=empty p=pawn n=knight b=bishop q=queen k=king
    private boolean isHighlighted;
    private boolean isAttacked;
    private boolean isBlack;
    private boolean hasMoved;
    private int positionX;
    private int positionY;
    private Board b;

    public Tile(Tile tile, Board b){
        image = null;
        pieceType = tile.getPieceType();
        positionX = tile.getPosX();
        positionY = tile.getPosY();
        isHighlighted = tile.getIsHighlighted();
        isAttacked = tile.getIsAttacked();
        isBlack = tile.getIsBlack();
        hasMoved = tile.getHasMoved();
        this.b = b;
    }
    public Tile(ImageView image, int x, int y, Board b){
        this.image = image;
        clickListener();
        pieceType = '1';
        positionX = x;
        positionY = y;
        isHighlighted = false;
        isAttacked = false;
        isBlack = false;
        hasMoved = false;
        this.b = b;
    }

    public ImageView getImage() {
        return image;
    }
    public char getPieceType() {
        return pieceType;
    }
    public boolean getIsBlack(){return isBlack;}
    public boolean getIsHighlighted() {
        return isHighlighted;
    }
    public boolean getIsAttacked() {
        return isAttacked;
    }
    public boolean getHasMoved() {
        return hasMoved;
    }
    public Board getB() {
        return b;
    }
    public int getPosX(){return positionX;}
    public int getPosY(){return positionY;}

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
        if (image != null)
            image.setBackgroundColor(Color.argb(highlighted ? 80 : 0, 200, 200, 0));
    }
    public void setAttacked(boolean attacked) {
        isAttacked = attacked;
//        if (image != null)
//            image.setBackgroundColor(Color.argb(attacked ? 80 : 0, 200, 200, 0));
        if (pieceType == 6)
            b.setInCheck(attacked);
    }
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
    public void setPiece(char pieceType, boolean isBlack){
        this.pieceType = pieceType;
        this.isBlack = isBlack;
        if (image == null)//if this is a duplicated version of a tile, which is used to manage check blocking and threat capturing
            return;
        if(isBlack)
            switch (pieceType){
                case '1':
                    image.setImageBitmap(null);
                    break;
                case 'p':
                    image.setImageResource(R.drawable.b_pawn);
                    break;
                case 'n':
                    image.setImageResource(R.drawable.b_knight);
                    break;
                case 'b':
                    image.setImageResource(R.drawable.b_bishop);
                    break;
                case 'r':
                    image.setImageResource(R.drawable.b_rook);
                    break;
                case 'q':
                    image.setImageResource(R.drawable.b_queen);
                    break;
                case 'k':
                    image.setImageResource(R.drawable.b_king);
                    break;
            }
        else
            switch (pieceType){
                case 'e':
                    image.setImageBitmap(null);
                    break;
                case 'p':
                    image.setImageResource(R.drawable.w_pawn);
                    break;
                case 'n':
                    image.setImageResource(R.drawable.w_knight);
                    break;
                case 'b':
                    image.setImageResource(R.drawable.w_bishop);
                    break;
                case 'r':
                    image.setImageResource(R.drawable.w_rook);
                    break;
                case 'q':
                    image.setImageResource(R.drawable.w_queen);
                    break;
                case 'k':
                    image.setImageResource(R.drawable.w_king);
                    break;
            }
    }

    public void clickListener(){
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.movePiece(Tile.this);
            }
        });
    }
    @NonNull
    @Override
    public String toString(){
        if (image != null)
            return "image:"+image.toString()+" piece id:"+pieceType+" color:"+(isBlack? "black" : "white");
        else
            return "this is a piece used for block detection."+" piece id:"+pieceType+" color:"+(isBlack? "black" : "white");
    }
}
