package com.example.danielproject_chess;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BoardFragment extends Fragment {

    private LinearLayout mainLayout;
    private Button resignBtn;
    private TextView whiteTV;
    private TextView blackTV;

    private Board b;
    private DBManager dbManager;
    private String email;
    private boolean clientIsBlack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_board, container, false);
        init(v);
        return v;
    }

    public void init(View v){
        mainLayout = v.findViewById(R.id.board);
        resignBtn = v.findViewById(R.id.resign_btn);
        whiteTV = v.findViewById(R.id.white_player);
        blackTV = v.findViewById(R.id.black_player);
        dbManager = new ViewModelProvider(requireActivity()).get(DBManager.class);
        clientIsBlack = dbManager.getClientIsBlack();
        email = getArguments().getString("email");

        b = new Board(this, mainLayout, clientIsBlack);

        whiteTV.setText(dbManager.getWhite());
        blackTV.setText(dbManager.getBlack());

        clickListenersBoard();
        valChangeListeners();
    }
    public void clickListenersBoard() {
        resignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbManager.exitGame();//game had to have been started
            }
        });
    }
    public void valChangeListeners(){
        dbManager.getMove().observe(getActivity(), move -> {
            if (move != null)
                b.setMove(move);
        });
    }


}