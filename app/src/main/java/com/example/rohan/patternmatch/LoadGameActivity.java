package com.example.rohan.patternmatch;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class LoadGameActivity extends AppCompatActivity {

    public LoadGameActivity mThisCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_game);

        mThisCopy = this;

        LinearLayout ll = (LinearLayout) findViewById(R.id.load_game_activity);

        // TODO: load all the game IDs from the saved games database
        // For each game ID, create a button

        SQLiteDatabase db = openOrCreateDatabase("Games.db", Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("SELECT gid FROM SavedGames", null);
        c.moveToFirst();

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < c.getCount(); i++) {
            final String gameID = c.getString(0);
            Log.v("tag", "Found game ID: " + gameID);

            // Create button
            Button btn = new Button(this);
            btn.setText(gameID);
            btn.setLayoutParams(lp);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mThisCopy, GameActivity.class);
                    intent.putExtra("GAME_ID", Integer.valueOf(gameID));
                    startActivity(intent);
                }
            });

            c.moveToNext();
        }
    }
}
