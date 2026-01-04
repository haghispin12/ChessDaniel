package com.example.danielproject_chess;

import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class Tile {
    private ImageView image;
    private int pieceType;//0=non  1=pawn  2=knight  3=bishop  4=rook  5=queen  6=king
    private boolean isHighlighted;
    private int positionX;
    private int positionY;

    public Tile(ImageView image, int x, int y){
        this.image = image;
        positionX = x;
        positionY = y;
        isHighlighted = false;
    }

    public ImageView getImage() {
        return image;
    }
    public int getPieceType() {
        return pieceType;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }
    public void setPieceType(int pieceType) {
        this.pieceType = pieceType;
    }

    public void setPiece(Uri uri, int pieceType){
        this.pieceType = pieceType;
        image.setImageURI(uri);
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
