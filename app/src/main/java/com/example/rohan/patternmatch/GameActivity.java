package com.example.rohan.patternmatch;

import android.app.DialogFragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements TCTimeUpDialogFragment.OnCompleteListener{
    private final int mNumParents = 5;
    // A = circle, C = diamond
    private final int[] mButtonUnselectedDrawables = {R.drawable.c1, R.drawable.c2, R.drawable.c3, R.drawable.c4,
            R.drawable.c5, R.drawable.d1, R.drawable.d2, R.drawable.d3, R.drawable.d4,
            R.drawable.d5};
    private final int[] mButtonSelectedDrawables = {R.drawable.c1s, R.drawable.c2s, R.drawable.c3s, R.drawable.c4s,
            R.drawable.c5s, R.drawable.d1s, R.drawable.d2s, R.drawable.d3s, R.drawable.d4s,
            R.drawable.d5s};
    private Random mRNG = new Random();

    private SQLiteDatabase mDB;

    private int mPlayerLevel;
    private int mGameID;
    private int mNumColumns;
    private int mNumChildren;

    private ArrayList<Integer> mParentColors;
    private ArrayList<String> mParentValues;

    private ArrayList<ArrayList<Integer>> mChildrenColors;
    private ArrayList<String> mChildrenValues;

    private ArrayList<ArrayList<String>> mMoveHistory;

    private ArrayList<Integer> mParentButtonIDs;
    private ArrayList<Integer> mChildButtonIDs;
    private ArrayList<Integer> mIndividualScoreIDs;

    private ArrayList<Integer> mScores;
    private int mCumulativeScore;
    private int mHighScore;

    private int startPos = -1;
    private int endPos = -1;
    private int fromChild = -1;
    private int toParent = -1;

    private boolean mChronometerRunning;
    private Chronometer mChronometer;

    private boolean timeUp = false;
    private String mTimeUpAnswer;

    private boolean mDisplayHighScoreToast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mDB = openOrCreateDatabase("Games.db", Context.MODE_PRIVATE, null);

        Cursor c = mDB.rawQuery("SELECT * from Miscellaneous where item = 'games played'", null);
        if(c.getCount() > 0) {
            c.moveToFirst();
            int gamesPlayed = Integer.valueOf(c.getString(1));
            gamesPlayed++;
            mDB.execSQL("UPDATE Miscellaneous SET value = '" + String.valueOf(gamesPlayed) + "' where item = 'games played'");
        } else {
            mDB.execSQL("INSERT into Miscellaneous VALUES('games played', '1')");
        }

        setPlayerLevelForNewGame();

        int gameID = -1;
        boolean timeChallenge = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            gameID = extras.getInt("GAME_ID");
            timeChallenge = extras.getBoolean("TIME_CHALLENGE");
        }

        if (gameID == -1) {
            setIDForNewGame();
        } else {
            mGameID = gameID;
        }
        TextView gID = (TextView) findViewById(R.id.gameID);
        gID.setText(String.valueOf(mGameID));

        setHighScoreForGame();


        setupChildrenAndParents();
        mChildButtonIDs = new ArrayList<Integer>();
        mParentButtonIDs = new ArrayList<Integer>();
        mIndividualScoreIDs = new ArrayList<Integer>();

        if (gameID != -1) {
            loadParentsFromDB();
        }

        scoreModel();
        generateButtons();
        if (timeChallenge) {
            setChronometerTC();
        } else {
            mChronometer = (Chronometer) findViewById(R.id.chrono);
            mChronometer.start();
            mChronometerRunning = true;
        }
    }

    private void setChronometerTC() {
        Button playPause = (Button) findViewById(R.id.playpause);
        playPause.setEnabled(false);
        Cursor c = mDB.rawQuery("SELECT time from LevelStats where level = " + String.valueOf(mPlayerLevel), null);
        c.moveToFirst();
        long timeInMS = c.getInt(0)*60*1000;
        mChronometer = (Chronometer) findViewById(R.id.chrono);
        mChronometer.setCountDown(true);
        long newBase = SystemClock.elapsedRealtime() + timeInMS;
        mChronometer.setBase(newBase);
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer cArg) {
                String timeString = cArg.getText().toString().substring(6);
                if(!timeUp) {
                    long secondsCounted = convertTimeStringToSeconds(timeString);
                    if (secondsCounted == 0) {
                        timeUp = true;
                        DialogFragment frag = new TCTimeUpDialogFragment();
                        frag.show(getFragmentManager(), "new game");
                    }
                } else {
                    if(mTimeUpAnswer == "yes") {
                        cArg.stop();
                        saveGame(null);
                        backToMain(null);
                    } else if(mTimeUpAnswer == "no") {
                        cArg.stop();
                        backToMain(null);
                    }
                }

            }
        });
        mChronometer.start();
        mChronometerRunning = true;
    }

    public void onAnswer(String answer) {
        mTimeUpAnswer = answer;
    }

    private void setPlayerLevelForNewGame() {
        Cursor c = mDB.rawQuery("SELECT * from Miscellaneous where item = 'games played'", null);
        c.moveToFirst();
        int gamesPlayed = Integer.valueOf(c.getString(1));
        Log.v("Tag: ", "Game: " + String.valueOf(gamesPlayed));
//        mPlayerLevel = gamesPlayed/50 + 1;
        mPlayerLevel = 21;

        c = mDB.rawQuery("SELECT children, columns from LevelStats where level = " + String.valueOf(mPlayerLevel), null);
        c.moveToFirst();
        mNumChildren = c.getInt(0);
        mNumColumns = c.getInt(1);
    }

    private void setIDForNewGame() {
        String gidsQuery = "SELECT DISTINCT gid FROM Games where level = " + String.valueOf(mPlayerLevel);
        Cursor c = mDB.rawQuery(gidsQuery, null);
        c.moveToFirst();
        int pickedGame = mRNG.nextInt(c.getCount());
        int ctr = 0;
        while (ctr < pickedGame) {
            c.moveToNext();
            ctr++;
        }
        mGameID = c.getInt(0);
    }

    private void setHighScoreForGame() {
        String hsQuery = "SELECT hs FROM HighScores where gid = " + String.valueOf(mGameID);
        Cursor c = mDB.rawQuery(hsQuery, null);
        if(c.getCount() > 0) {
            c.moveToFirst();
            mHighScore = c.getInt(0);
        } else {
            mHighScore = mNumChildren*(mNumColumns-1);
        }
    }

    private void setupChildrenAndParents() {
        mChildrenValues = new ArrayList<String>();
        mMoveHistory = new ArrayList<ArrayList<String>>();
        String cidQuery = "SELECT cid from Games where gid = " + String.valueOf(mGameID);
        Cursor c = mDB.rawQuery(cidQuery, null);
        c.moveToFirst();
        // Log.v("tag", DatabaseUtils.dumpCursorToString(c));
        int ctr = 0;
        String childQuery = "SELECT kmer from Children where cid = ";
        while(ctr < mNumChildren) {
            int currentCID = c.getInt(0);
            Cursor tempC = mDB.rawQuery(childQuery + String.valueOf(currentCID), null);
            tempC.moveToFirst();
            mChildrenValues.add(tempC.getString(0));
            ctr++;
            c.moveToNext();
        }

        mParentValues = new ArrayList<String>();
        mParentColors = new ArrayList<Integer>();
        mChildrenColors = new ArrayList<ArrayList<Integer>>();
        String[] defP = {"", ""};
        for (int i = 0; i < mNumChildren; i++) {
            if (i < mNumParents) {
                if (i < 2) {
                    for (int j = 0; j < mNumColumns; j++) {
                        if ((i+j) % 2 == 0) {
                            defP[i] += 'A';
                        } else {
                            defP[i] += 'C';
                        }
                    }
                    mParentValues.add(defP[i]);
                } else {
                    mParentValues.add(mChildrenValues.get(i));
                }
                mParentColors.add(i);
            }
            mChildrenColors.add(new ArrayList<Integer>());
        }
    }

    private void loadParentsFromDB() {
        String parentsQuery = "SELECT * from SavedGames where gid = " + String.valueOf(mGameID);
        Cursor c = mDB.rawQuery(parentsQuery, null);
        c.moveToFirst();
        for(int i=0; i < mNumParents; i++) {
            mParentValues.set(i, c.getString(i+1));
        }
    }

    private void scoreModel() {
        mScores = new ArrayList<Integer>();

        for(int i = 0; i < mChildrenValues.size(); i++) {
            mScores.add(0);
            String currChild = mChildrenValues.get(i);
            ArrayList<Integer> currChildColors = mChildrenColors.get(i);
            while(currChildColors.size() < currChild.length()) {
                currChildColors.add(-1);
            }
            int doneSoFar = 0;
            while(doneSoFar < currChild.length()) {
                if(doneSoFar > 0) {
                    mScores.set(i, mScores.get(i)+1);
                }

                int maxMatch = 0;
                int maxParent = 0;
                for (int j = 0; j < mParentValues.size(); j++) {
                    String possParent = mParentValues.get(j);
                    int k = doneSoFar;
                    while (k < currChild.length()) {
                        if (currChild.charAt(k) == possParent.charAt(k)) {
                            k++;
                        } else {
                            break;
                        }
                    }
                    if (k - doneSoFar > maxMatch) {
                        maxMatch = k - doneSoFar;
                        maxParent = j;
                    }
                }

                for(int j = doneSoFar; j < doneSoFar+maxMatch; j++) {
                    if (currChildColors.get(j) != mParentColors.get(maxParent)) {
                        currChildColors.set(j, mParentColors.get(maxParent));
                        if(mChildButtonIDs.size() > i*mNumColumns+j)
                            setButtonIcon((Button)findViewById(mChildButtonIDs.get(i*mNumColumns+j)), currChild.charAt(j), mParentColors.get(maxParent), false);
                    }
                }
                doneSoFar += maxMatch;
            }
        }

        mCumulativeScore = 0;
        for(int i = 0; i < mScores.size();i++) {
            mCumulativeScore += mScores.get(i);
            if(mIndividualScoreIDs.size() > i)
                ((TextView)findViewById(mIndividualScoreIDs.get(i))).setText(String.valueOf(mScores.get(i)));
        }

        if(mCumulativeScore < mHighScore) {
            mHighScore = mCumulativeScore;
            if (mDisplayHighScoreToast) {
                Toast toast = Toast.makeText(getApplicationContext(), "New high score!!", Toast.LENGTH_SHORT);
                toast.show();
            }
            if (!mDisplayHighScoreToast) {
                mDisplayHighScoreToast = true;
            }
        }
        TextView highScore = (TextView) findViewById(R.id.highScore);
        highScore.setText(String.valueOf(Math.min(mCumulativeScore, mHighScore)));
        TextView score = (TextView) findViewById(R.id.score);
        score.setText(String.valueOf(mCumulativeScore));
    }

    private void generateButtons() {
        GridLayout glParent = (GridLayout) findViewById(R.id.parents);
        glParent.setColumnCount(mNumColumns);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(64,64);
        params.setMargins(4,4,4,4);

        for (int i = 0; i < mNumParents; i++) {
            String parent = mParentValues.get(i);
            int color = mParentColors.get(i);
            for (int j = 0; j < mNumColumns; j++) {
                Button btn = new Button(this);
                int currentID = View.generateViewId();
                mParentButtonIDs.add(currentID);
                btn.setId(currentID);
                btn.setLayoutParams(params);
                setButtonIcon(btn, parent.charAt(j), color, false);
                setParentClickListener(btn);
                glParent.addView(btn);
            }
        }

        GridLayout glChildren = (GridLayout) findViewById(R.id.children);
        glChildren.setColumnCount(mNumColumns+1);
        for (int i = 0; i < mNumChildren; i++) {
            String child = mChildrenValues.get(i);
            ArrayList<Integer> childColors = mChildrenColors.get(i);
            for (int j = 0; j < mNumColumns; j++) {
                Button btn = new Button(this);
                int currentID = View.generateViewId();
                mChildButtonIDs.add(currentID);
                btn.setId(currentID);
                btn.setLayoutParams(params);
                setButtonIcon(btn, child.charAt(j), childColors.get(j), false);
                setChildClickListener(btn);
                glChildren.addView(btn);
            }
            TextView scoreView = new TextView(this);
            scoreView.setText(String.valueOf(mScores.get(i)));
            int currentID = View.generateViewId();
            mIndividualScoreIDs.add(currentID);
            scoreView.setId(currentID);
            glChildren.addView(scoreView);
        }
    }

    private void setParentClickListener(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (startPos == -1 || endPos == -1 || fromChild == -1) {
                    return;
                } else {
                    toParent = mParentButtonIDs.indexOf(v.getId())/mNumColumns;
                    String sourceChild = mChildrenValues.get(fromChild);
                    String destParent = mParentValues.get(toParent);
                    String newParent = destParent.substring(0, startPos) + sourceChild.substring(startPos,endPos+1);
                    if (endPos+1 < destParent.length()) {
                        newParent += destParent.substring(endPos+1);
                    }

                    boolean isValid = checkValidity(newParent);
                    if (!isValid) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Invalid configuration", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        ArrayList<String> oldParents = new ArrayList<String>();
                        for(int i = 0; i < mNumParents; i++) {
                            oldParents.add(mParentValues.get(i));
                        }

                        mMoveHistory.add(oldParents);
                        mParentValues.set(toParent, newParent);
                        scoreModel();
                        updateButtons(oldParents);
                    }
                    setButtonIcon((Button)findViewById(mChildButtonIDs.get(fromChild*mNumColumns+startPos)), sourceChild.charAt(startPos), mChildrenColors.get(fromChild).get(startPos), false);
                    setButtonIcon((Button)findViewById(mChildButtonIDs.get(fromChild*mNumColumns+endPos)), sourceChild.charAt(endPos),  mChildrenColors.get(fromChild).get(endPos), false);
                    startPos = -1;
                    endPos = -1;
                    fromChild = -1;
                }
            }
        });
    }

    private void setChildClickListener(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int buttonIndex = mChildButtonIDs.indexOf(v.getId());
                if(startPos == -1 || startPos != -1 && endPos != -1) {
                    if (endPos != -1) {
                        setButtonIcon((Button)findViewById(mChildButtonIDs.get(fromChild*mNumColumns+startPos)), mChildrenValues.get(fromChild).charAt(startPos), mChildrenColors.get(fromChild).get(startPos), false);
                        setButtonIcon((Button)findViewById(mChildButtonIDs.get(fromChild*mNumColumns+endPos)), mChildrenValues.get(fromChild).charAt(endPos), mChildrenColors.get(fromChild).get(endPos), false);
                        endPos = -1;
                    }
                    fromChild = buttonIndex/mNumColumns;
                    startPos = buttonIndex % mNumColumns;
                    setButtonIcon((Button)findViewById(v.getId()), mChildrenValues.get(fromChild).charAt(startPos), mChildrenColors.get(fromChild).get(startPos), true);
                } else if(endPos == -1) {
                    int newChild = buttonIndex/mNumColumns;
                    if (newChild == fromChild) {
                        if((buttonIndex % mNumColumns) < startPos) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Invalid configuration", Toast.LENGTH_SHORT);
                            toast.show();
                            setButtonIcon((Button)findViewById(mChildButtonIDs.get(fromChild*mNumColumns+startPos)), mChildrenValues.get(fromChild).charAt(startPos), mChildrenColors.get(fromChild).get(startPos), false);
                            startPos = -1;
                            endPos = -1;
                            fromChild = -1;
                        } else {
                            endPos = buttonIndex % mNumColumns;
                            setButtonIcon((Button)findViewById(v.getId()), mChildrenValues.get(fromChild).charAt(endPos), mChildrenColors.get(fromChild).get(endPos), true);
                        }
                    } else {
                        setButtonIcon((Button)findViewById(mChildButtonIDs.get(fromChild*mNumColumns+startPos)), mChildrenValues.get(fromChild).charAt(startPos), mChildrenColors.get(fromChild).get(startPos), false);
                        fromChild = buttonIndex/mNumColumns;
                        startPos = buttonIndex % mNumColumns;
                        setButtonIcon((Button)findViewById(v.getId()), mChildrenValues.get(fromChild).charAt(startPos), mChildrenColors.get(fromChild).get(startPos), true);
                    }
                }
            }
        });
    }

    private void setButtonIcon(Button btn, char value, int color, boolean selected) {
        int resource;
        if(selected) {
            resource = mButtonSelectedDrawables[(value == 'A' ? 0 : 1)*mNumParents + color];
        } else {
            resource = mButtonUnselectedDrawables[(value == 'A' ? 0 : 1)*mNumParents + color];
        }
        btn.setBackgroundResource(resource);
    }

    private void updateHighscores() {
        if (mHighScore > mCumulativeScore) {
            mDB.execSQL("UPDATE HighScores SET hs = " + String.valueOf(mCumulativeScore) + " WHERE gid = " + String.valueOf(mGameID));
        }
    }

    public void backToMain(View v) {
        updateHighscores();
        super.onBackPressed();
    }

    public void undoMove(View v) {
        if(mMoveHistory.size() > 0) {
            ArrayList<String> oldParents = new ArrayList<String>();
            for(int i = 0; i < mNumParents; i++) {
                oldParents.add(mParentValues.get(i));
            }
            mParentValues = mMoveHistory.get(mMoveHistory.size()-1);
            mMoveHistory.remove(mMoveHistory.size()-1);
            scoreModel();
            updateButtons(oldParents);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "No previous moves found.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void resetGame(View v) {
        ArrayList<String> oldParents = new ArrayList<String>();
        for(int i = 0; i < mNumParents; i++) {
            oldParents.add(mParentValues.get(i));
        }
        if (startPos != -1) {
            setButtonIcon((Button)findViewById(mChildButtonIDs.get(fromChild*mNumColumns+startPos)), mChildrenValues.get(fromChild).charAt(startPos), mChildrenColors.get(fromChild).get(startPos), false);
            if (endPos != -1) {
                setButtonIcon((Button)findViewById(mChildButtonIDs.get(fromChild*mNumColumns+endPos)), mChildrenValues.get(fromChild).charAt(endPos), mChildrenColors.get(fromChild).get(endPos), false);
            }
        }

        setupChildrenAndParents();
        scoreModel();
        updateButtons(oldParents);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        Toast toast = Toast.makeText(getApplicationContext(), "Game reset!!", Toast.LENGTH_SHORT);
        toast.show();
    }

    private boolean checkValidity(String newParent) {
        for(int i = 0; i < newParent.length(); i++) {
            boolean aPresent = false;
            boolean cPresent = false;
            for(int j = 0; j < mNumParents; j++) {
                char charCheck;
                if (j == toParent) {
                    charCheck = newParent.charAt(i);
                } else {
                    charCheck = mParentValues.get(j).charAt(i);
                }

                if(charCheck == 'A') {
                    aPresent = true;
                } else if(charCheck == 'C') {
                    cPresent = true;
                }
                if (aPresent && cPresent)
                    break;
            }

            if(!aPresent) {
                for(int j = 0; j < mNumChildren; j++) {
                    if(mChildrenValues.get(j).charAt(i) == 'A')
                        return false;
                }
            }
            if(!cPresent) {
                for(int j = 0; j < mNumChildren; j++) {
                    if(mChildrenValues.get(j).charAt(i) == 'C')
                        return false;
                }
            }
        }
        return true;
    }

    private long convertTimeStringToSeconds(String s) {
        String[] segments = s.split(":");
        long multiplier = 1;
        long result = 0;
        for (int i = segments.length-1; i >= 0; i--) {
            long segmentVal = Integer.valueOf(segments[i]);
            segmentVal *= multiplier;
            multiplier *= 60;
            result += segmentVal;
        }
        return result;
    }

    public void toggle(View v) {
        if (mChronometerRunning) {
            mChronometer.stop();
        } else {
            long elapsedTime = SystemClock.elapsedRealtime();
            String timeString = mChronometer.getText().toString().substring(6);
            long secondsCounted = convertTimeStringToSeconds(timeString);
            long base = elapsedTime - secondsCounted*1000;
            mChronometer.setBase(base);
            mChronometer.start();
        }
        mChronometerRunning = !mChronometerRunning;
    }

    public void saveGame(View v) {
        String deleteQuery = "DELETE FROM SavedGames WHERE gid = " + String.valueOf(mGameID);
        mDB.execSQL(deleteQuery);

        String insertQuery = "INSERT INTO SavedGames VALUES (" + String.valueOf(mGameID);
        for(int i=0; i < mNumParents; i++) {
            insertQuery += ",'" + mParentValues.get(i) + "'";
        }
        insertQuery += ")";
        mDB.execSQL(insertQuery);
        Toast toast = Toast.makeText(getApplicationContext(), "Game saved!!", Toast.LENGTH_SHORT);
        toast.show();
    }

    private void updateButtons(ArrayList<String> oldParents) {
        for(int i = 0; i < oldParents.size(); i++) {
            if(!oldParents.get(i).equalsIgnoreCase(mParentValues.get(i))) {
                String oldP = oldParents.get(i);
                String newP = mParentValues.get(i);
                for(int j = 0; j < oldP.length(); j++) {
                    if(oldP.charAt(j) != newP.charAt(j)) {
                        setButtonIcon((Button)findViewById(mParentButtonIDs.get(i*mNumColumns+j)), newP.charAt(j), mParentColors.get(i), false);
                    }
                }
            }
        }
    }
}
