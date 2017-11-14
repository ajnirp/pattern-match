package com.example.rohan.patternmatch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private ArrayList<Integer> mScores;
    private ArrayList<Integer> mParentColors;
    private ArrayList<Integer> mParentValues;
    private ArrayList<Integer> mChildrenColors;
    private ArrayList<Integer> mChildrenValues;
    private int mGameID;

    private int num_columns;

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

    // B
    private void renderUI() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.game_view);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < num_columns; i++) {
            Button button = new Button(this);
            layout.addView(button, layoutParams);
        }
    }

    // R
    private void updateLists() {
        boolean isValid = checkValidity();
        if (!isValid) {
            Toast toast = Toast.makeText(getApplicationContext(), "Invalid configuration", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // B
    private boolean checkValidity() {
        return false;
    }
}
