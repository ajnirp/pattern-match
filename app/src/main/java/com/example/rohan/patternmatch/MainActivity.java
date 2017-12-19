package com.example.rohan.patternmatch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
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
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("tag", "onCreate");
        parseAndSetupDatabase();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private static boolean databaseExists(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    private void parseAndSetupDatabase() {
//        if (databaseExists(this, "Games.db")) {
//            Log.v("tag", "parseAndSetupDatabase: Games.db already exists, early return");
//            return;
//        }

        Log.v("tag", "beginning");
        db = openOrCreateDatabase("Games.db", Context.MODE_PRIVATE, null);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("createTables.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                db.execSQL(line);
            }
            Log.v("tag", "createTables");
        } catch (Exception e) {
            Log.v("tag", e.toString());
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("populateChildren.txt")))) {
            ArrayList<String> lines = new ArrayList<String>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            try {
                db.beginTransaction();
                for (int i = 0; i < lines.size(); i++) {
                    ContentValues vals = new ContentValues();
                    vals.put("cid", i);
                    vals.put("kmer", lines.get(i));
                    db.insert("Children", null, vals);
                }
                db.setTransactionSuccessful();
                Log.v("tag", "populateChildren");
            } catch (Exception e) {
                Log.v("tag", e.toString());
            }
            finally {
                db.endTransaction();
            }
        } catch (Exception e) {}

        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("populateGame.txt")))) {
            ArrayList<String> lines = new ArrayList<String>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            try {
                db.beginTransaction();
                for (int i = 0; i < lines.size(); i++) {
                    String[] split = lines.get(i).split(" ");
                    int gid = Integer.valueOf(split[0]);
                    int cid = Integer.valueOf(split[1]);
                    int level = Integer.valueOf(split[2]);
                    Log.v("tag", Integer.toString(gid));
                    ContentValues vals = new ContentValues();
                    vals.put("gid", gid);
                    vals.put("cid", cid);
                    vals.put("level", level);
                    db.insert("Games", null, vals);
                }
                db.setTransactionSuccessful();
                Log.v("tag", "populateGame");
            } catch (Exception e) {
                Log.v("tag", e.toString());
            }
            finally {
                db.endTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        DialogFragment frag = new StartTCGameDialogFragment();
        frag.show(getFragmentManager(), "new game");
    }

    public void howToPlay(View v) {
        Intent intent = new Intent(this, HowToPlayActivity.class);
        startActivity(intent);
    }
}
