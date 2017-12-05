package com.example.rohan.patternmatch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        parseAndSetupDatabase();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private static boolean databaseExists(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    private void parseAndSetupDatabase() {
        if (databaseExists(this, "Games.db")) {
            Log.v("tag", "parseAndSetupDatabase: Games.db already exists, early return");
            return;
        }

        SQLiteDatabase db = openOrCreateDatabase("Games.db", Context.MODE_PRIVATE, null);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("DBScript.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                db.execSQL(line);
            }
        } catch (Exception e) {
            Log.v("tag", "parseAndSetupDatabase: failed to find or parse Games.txt");
        }
    }

    public void newGame(View v) {
        DialogFragment frag = new StartNewGameDialogFragment();
        frag.show(getFragmentManager(), "new game");
    }

    public void loadGame(View v) {
        Intent intent = new Intent(this, LoadGameActivity.class);
        startActivity(intent);
    }

    public void timeChallenge(View v) {
        // TODO
    }

    public void howToPlay(View v) {
        // TODO
    }
}
