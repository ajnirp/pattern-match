package com.example.rohan.patternmatch;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
	private final int mNumParents = 5;
	// A = circle, C = diamond
    private final int[] resources = {R.drawable.c1, R.drawable.c2, R.drawable.c3, R.drawable.c4,
                R.drawable.c5, R.drawable.d1, R.drawable.d2, R.drawable.d3, R.drawable.d4,
                R.drawable.d5};
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
	
	private ArrayList<Integer> mChildButtonIDs;

    private ArrayList<Integer> mScores;
    private int mCumulativeScore;

	private int startPos = -1;
	private int endPos = -1;
	private int fromChild = -1;
	private int toParent = -1;

	private CountDownTimer mTimer;

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
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			gameID = extras.getInt("GAME_ID");
		}

		if (gameID == -1) {
			setIDForNewGame();
		} else {
			mGameID = gameID;
		}
		
		setupChildrenAndParents();
		
		if (gameID != -1) {
			loadParentsFromDB();
		}
		
		scoreModel();
        generateButtons();
    }

	private void setPlayerLevelForNewGame() {
		Cursor c = mDB.rawQuery("SELECT * from Miscellaneous where item = 'games played'", null);
		c.moveToFirst();
		int gamesPlayed = Integer.valueOf(c.getString(1));
		mPlayerLevel = gamesPlayed/50 + 1;

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
						if (i+j % 2 == 0) {
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

		mChildButtonIDs = new ArrayList<Integer>();
        for(int i = 0; i < mNumChildren*mNumColumns; i++) {
            mChildButtonIDs.add(-1);
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
                    currChildColors.set(j, mParentColors.get(maxParent));
				}
                doneSoFar += maxMatch;
            }
        }
        for(int i = 0; i < mScores.size();i++) {
            mCumulativeScore += mScores.get(i);
        }
        TextView highScore = (TextView) findViewById(R.id.highScore);
        highScore.setText(String.valueOf(mCumulativeScore));
        TextView score = (TextView) findViewById(R.id.score);
        score.setText(String.valueOf(mCumulativeScore));
    }

    private void generateButtons() {
        GridLayout gl = (GridLayout) findViewById(R.id.parents);
        gl.setRowCount(mNumParents);
        gl.setColumnCount(mNumColumns);

//        GridLayout.LayoutParams lp = (GridLayout.LayoutParams) new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
//        params.height = 10;
//        params.width = 10;
        for (int i = 0; i < mNumParents; i++) {
			String parent = mParentValues.get(i);
			int color = mParentColors.get(i);
            for (int j = 0; j < mNumColumns; j++) {
                Button btn = new Button(this);
                setButtonIcon(btn, parent.charAt(j), color);
//                btn.setLayoutParams(params);
                gl.addView(btn);
            }
        }

        gl = (GridLayout) findViewById(R.id.parents);
        gl.setRowCount(mNumChildren);
        gl.setColumnCount(mNumColumns);

        for (int i = 0; i < mNumChildren; i++) {
			String child = mChildrenValues.get(i);
			ArrayList<Integer> childColors = mChildrenColors.get(i);
            for (int j = 0; j < mNumColumns; j++) {
                Button btn = new Button(this);
				int currentID = View.generateViewId();
                mChildButtonIDs.set(i * mNumColumns + j, currentID);
				btn.setId(currentID);
				setButtonIcon(btn, child.charAt(j), childColors.get(j));
//                btn.setLayoutParams(params);
                gl.addView(btn);
            }
        }
    }

    private void setButtonIcon(Button btn, char value, int color) {
        int resource = resources[(value == 'A' ? 0 : 1)*mNumParents + color];
        btn.setBackgroundResource(resource);
    }

    private void updateHighscores() {
    }

    public void backToMain(View v) {
		updateHighscores();
		super.onBackPressed();
	}

    private void updateUI() {
        ViewGroup layout = (ViewGroup) findViewById(R.id.game_view);
        for (int i = 0; i < mNumColumns; i++) {
            Button button = new Button(this);
//            layout.addView(button, layoutParams);
        }
    }

    // R
    private void updateLists() {
        boolean isValid = checkValidity();
        if (!isValid) {
            Toast toast = Toast.makeText(getApplicationContext(), "Invalid configuration", Toast.LENGTH_SHORT);
            toast.show();
        } else {
			mMoveHistory.add(mParentValues);
		}
    }

	private void undoMove(View v) {
		if(mMoveHistory.size() > 0) {
			mParentValues = mMoveHistory.get(mMoveHistory.size()-1);
			mMoveHistory.remove(mMoveHistory.size()-1);
			scoreModel();
		} else {
            Toast toast = Toast.makeText(getApplicationContext(), "No previous moves found.", Toast.LENGTH_SHORT);
            toast.show();
		}
	}
	
	private void resetGame(View v) {
		setupChildrenAndParents();
		
		loadParentsFromDB();
		
		scoreModel();
        generateButtons();		
	}
	
    private boolean checkValidity() {
		if (fromChild == -1 || toParent == -1 || startPos >= endPos) {
			return false;
		} else {
			String sourceChild = mChildrenValues.get(fromChild);
			String destParent = mParentValues.get(toParent);
			String newParent = destParent.substring(0, startPos) + sourceChild.substring(startPos,endPos+1);
			if (endPos+1 < destParent.length()) {
				newParent += destParent.substring(endPos+1);
			}
			
			for(int i = 0; i < destParent.length(); i++) {
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
				}
				if(!aPresent || !cPresent) {
					return false;
				}
			}
			return true;
		}
    }

	private void saveGame(View v) {
		String deleteQuery = "DELETE FROM SavedGames WHERE gid = " + String.valueOf(mGameID);
		mDB.execSQL(deleteQuery);
		
		String insertQuery = "INSERT INTO SavesGames VALUES (" + String.valueOf(mGameID);
		for(int i=0; i < mNumParents; i++) {
			insertQuery += ",'" + mParentValues.get(i) + "'";
		}
		insertQuery += ")";
		mDB.execSQL(insertQuery);
	}
}
