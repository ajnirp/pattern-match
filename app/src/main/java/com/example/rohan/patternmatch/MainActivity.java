package com.example.rohan.patternmatch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void newGame(View v) {
        DialogFragment frag = new StartNewGameDialogFragment();
        frag.show(getFragmentManager(), "new game");
    }

    public void timeChallenge(View v) {
        // TODO
    }

    public void howToPlay(View v) {
        // TODO
    }
}
