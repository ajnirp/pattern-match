package com.example.rohan.patternmatch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private ArrayList<Integer> mScores;
    private ArrayList<Integer> mParentColors;
    private ArrayList<Integer> mParentValues;
    private ArrayList<Integer> mChildrenColors;
    private ArrayList<Integer> mChildrenValues;
    private int gameID;

    final private int NUM_COLS = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mParentColors = new ArrayList<Integer>(5);
        mParentValues = new ArrayList<Integer>(5);
        mChildrenColors = new ArrayList<Integer>(20);
        mChildrenValues = new ArrayList<Integer>(20);

        renderUI();
    }

    private void renderUI() {
        ViewGroup layout = (ViewGroup) findViewById(R.id.game_view);
        for (int i = 0; i < NUM_COLS; i++) {
            Button button = new Button(this);
            button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }
}
