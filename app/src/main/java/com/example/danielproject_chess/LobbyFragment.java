package com.example.danielproject_chess;


import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;




public class LobbyFragment extends Fragment {


    private Button createBtn;
    private Button joinBtn;
    private EditText userCodeET;
    private DBManager dbManager;
    private String email;


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
        dbManager = new ViewModelProvider(requireActivity()).get(DBManager.class);
        email = getArguments().getString("email");


        clickListenersMain();
    }
    public void clickListenersMain() {
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!userCodeET.getText().toString().isEmpty())
                    dbManager.startGame(email, userCodeET.getText().toString());//open new room named this code as this email
                else
                    Toast.makeText(getContext(), "Please enter a valid game code to start the game with", Toast.LENGTH_SHORT).show();
            }
        });
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!userCodeET.getText().toString().isEmpty())
                    dbManager.joinGame(email, userCodeET.getText().toString());//join room named this code as this email
                else
                    Toast.makeText(getContext(), "Please enter a valid game code to start the game with", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
