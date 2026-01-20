package com.example.danielproject_chess;

import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class Tile {
    private ImageView image;
    private int pieceType;//0=non  1=pawn  2=knight  3=bishop  4=rook  5=queen  6=king
    private boolean isHighlighted;
    private boolean isAttacked;
    private boolean isBlack;
    private int positionX;
    private int positionY;
    private Board b;

    public Tile(Tile tile){
        image = null;
        positionX = tile.getPosX();
        positionY = tile.getPosY();
        isHighlighted = tile.getIsHighlighted();
        isAttacked = tile.getIsAttacked();
        b = tile.getB();
    }
    public Tile(ImageView image, int x, int y, Board b){
        this.image = image;
        clickListener();
        positionX = x;
        positionY = y;
        isHighlighted = false;
        isAttacked = false;
        this.b = b;
    }

    public ImageView getImage() {
        return image;
    }
    public int getPieceType() {
        return pieceType;
    }
    public boolean getIsBlack(){return isBlack;}
    public boolean getIsHighlighted() {
        return isHighlighted;
    }
    public boolean getIsAttacked() {
        return isAttacked;
    }

    public Board getB() {
        return b;
    }

    public int getPosX(){return positionX;}
    public int getPosY(){return positionY;}

    public void setImage(ImageView image) {
        this.image = image;
    }
    public void setPieceType(int pieceType) {
        this.pieceType = pieceType;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public void setAttacked(boolean attacked) {
        isAttacked = attacked;
        if (image != null)
            image.setBackgroundColor(Color.argb(attacked ? 80 : 0, 200, 200, 0));
        if (pieceType == 6)
            b.setInCheck(attacked);
    }

    public void setPiece(int pieceType, boolean isBlack){
        this.pieceType = pieceType;
        this.isBlack = isBlack;
        if (image == null)//if this is a duplicated version of a tile, which is used to manage check blocking and threat capturing
            return;
        if(isBlack)
            switch (pieceType){
                case 0:
                    image.setImageBitmap(null);
                    break;
                case 1:
                    image.setImageResource(R.drawable.b_pawn);
                    break;
                case 2:
                    image.setImageResource(R.drawable.b_knight);
                    break;
                case 3:
                    image.setImageResource(R.drawable.b_bishop);
                    break;
                case 4:
                    image.setImageResource(R.drawable.b_rook);
                    break;
                case 5:
                    image.setImageResource(R.drawable.b_queen);
                    break;
                case 6:
                    image.setImageResource(R.drawable.b_king);
                    break;
            }
        else
            switch (pieceType){
                case 0:
                    image.setImageBitmap(null);
                    break;
                case 1:
                    image.setImageResource(R.drawable.w_pawn);
                    break;
                case 2:
                    image.setImageResource(R.drawable.w_knight);
                    break;
                case 3:
                    image.setImageResource(R.drawable.w_bishop);
                    break;
                case 4:
                    image.setImageResource(R.drawable.w_rook);
                    break;
                case 5:
                    image.setImageResource(R.drawable.w_queen);
                    break;
                case 6:
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
    public void toggleHighlight(){
        isHighlighted = !isHighlighted;
    }

    @NonNull
    @Override
    public String toString(){
        return "image:"+image.toString()+" piece id:"+pieceType;
    }
}
