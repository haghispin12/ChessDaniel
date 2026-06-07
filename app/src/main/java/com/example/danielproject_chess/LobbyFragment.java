package com.example.danielproject_chess;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

public class LobbyFragment extends Fragment {

    private Button createBtn;
    private Button joinBtn;
    private EditText userCodeET;
    private TextView newCodeTV;

    private DBManager dbManager;
    private String email;
    private boolean clientIsBlack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lobby, container, false);
        init(v);
        return v;
    }

    public void init(View v){
        createBtn = v.findViewById(R.id.create_btn);
        joinBtn = v.findViewById(R.id.join_btn);
        userCodeET = v.findViewById(R.id.code_input);
        newCodeTV = v.findViewById(R.id.new_game_code);
        dbManager = new ViewModelProvider(requireActivity()).get(DBManager.class);
        email = getArguments().getString("email");

        clickListenersMain();
        valChangeListeners();
    }
    public void clickListenersMain() {
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clientIsBlack = false;
                dbManager.startGame(email);
            }
        });
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clientIsBlack = true;
                dbManager.joinGame(email, "a"/*userCodeET.getText().toString()*/);
            }
        });
    }
    public void valChangeListeners(){
        dbManager.getUUID().observe(getActivity(), uuid -> {
            if (uuid != null && !clientIsBlack)
                newCodeTV.setText(uuid);
        });
    }
}