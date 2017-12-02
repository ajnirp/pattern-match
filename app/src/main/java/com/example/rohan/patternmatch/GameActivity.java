package com.example.rohan.patternmatch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private ArrayList<Integer> mScores;
    private ArrayList<Integer> mParentColors;
    private int mGameID;

    private int mNumColumns;

    private ArrayList<String> mParentValues;
    private ArrayList<ArrayList<Integer>> mChildrenColors;
    private ArrayList<String> mChildrenValues;
    
    private int mPlayerLevel;
    private int cumulativeScore;
    private SQLiteDatabase mDB;

    private void setButtonIcon(Button btn, int row, int col, ArrayList<Integer> colors, ArrayList<String> values) {
        int[] resources = {R.drawable.c1, R.drawable.c2, R.drawable.c3, R.drawable.c4,
                R.drawable.c5, R.drawable.d1, R.drawable.d2, R.drawable.d3, R.drawable.d4,
                R.drawable.d5};
        char value = values.get(row).charAt(col);
        int resource = resources[(value == 'A' ? 0 : 1)*5 + colors.get(col)];
        btn.setBackgroundResource(resource);
    }

    private void generateButtons(ArrayList<String> parents) {
        GridLayout gl = (GridLayout) findViewById(R.id.parents);
        gl.setColumnCount(mNumColumns);

        int numRows = 5; // TODO: the number of parent strings depends on the game ID
        GridLayout.LayoutParams lp = (GridLayout.LayoutParams) new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < mNumColumns; j++) {
                Button btn = new Button(this);
                setButtonIcon(btn, i, j, mParentColors, mParentValues);
                btn.setLayoutParams(lp);
                gl.addView(btn);
            }
        }

        int numChildren = 5; // TODO: the number of children strings depends on the game ID
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < mNumColumns; j++) {
                Button btn = new Button(this);
                btn.setLayoutParams(lp);
                gl.addView(btn);
            }
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mDB = this.openOrCreateDatabase("gameDatabase", Context.MODE_PRIVATE, null);
//        setupGameFromDB();

        int numChildren = 20;

        mParentColors = new ArrayList<Integer>(5);
        mParentValues = new ArrayList<String>(5);
        mChildrenColors = new ArrayList<ArrayList<Integer>>(numChildren);
        for (int i = 0; i < numChildren; i++) {
            mChildrenColors.set(i, new ArrayList<Integer>(20));
        }
        mChildrenValues = new ArrayList<String>(20);

        updateUI();
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
        }
    }

    // B
    private boolean checkValidity() {
        return false;
    }

//    private void setupGameFromDB() {
//        String baseQuery = "SELECT * FROM Games";
//        String tag = "" + ((TextView)findViewById(R.id.tagEdit)).getText();
//        String size = "" + ((TextView)findViewById(R.id.sizeEdit)).getText();
//        String condition = "";
//
//        if (!tag.equalsIgnoreCase("TAGS") && tag.length() > 0) {
//            String[] tags = tag.split(";");
//            condition = " WHERE (Tags like '%" + tags[0] + "%'";
//            for(int i = 1; i < tags.length; i++) {
//                condition += " OR Tags like '%" + tags[i] + "%'";
//            }
//            condition += ")";
//        }
//
//        if (!size.equalsIgnoreCase("SIZE") && size.length() > 0) {
//            int sizeLow = (int)(Integer.valueOf(size)*0.75);
//            int sizeHigh = (int)(Integer.valueOf(size)*1.25);
//
//            if (condition.length() > 0) {
//                condition += " AND Size < " + String.valueOf(sizeHigh) + " AND Size > " + String.valueOf(sizeLow);
//            } else {
//                condition = " WHERE Size < " + String.valueOf(sizeHigh) + " AND Size > " + String.valueOf(sizeLow);
//            }
//        }
//
//        baseQuery += condition + " LIMIT 1;";
//        Cursor c = db.rawQuery(baseQuery, null);
//        if (c.getCount() > 0) {
//            c.moveToNext();
//
//            photoLocation = c.getString(1);
//            ImageView IV = (ImageView) findViewById(R.id.imageView);
//            Bitmap bMap = BitmapFactory.decodeFile(photoLocation);
//            IV.setImageBitmap(bMap);
//
//            EditText sizeView = (EditText) findViewById(R.id.sizeEdit);
//            sizeView.setText(c.getString(3));
//
//            EditText tagView = (EditText) findViewById(R.id.tagEdit);
//            tagView.setText(c.getString(2));
//        }
//    }

    private void scoreModel() {
        for(int i = 0; i < mChildrenValues.size(); i++) {
            String currChild = mChildrenValues.get(i);
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
                        }
                        if (k - doneSoFar > maxMatch) {
                            maxMatch = k - doneSoFar;
                            maxParent = j;
                        }
                    }
                }
                doneSoFar += maxMatch;
                // TODO: Add children colors based on maxParent
                maxMatch = 0;
                maxParent = 0;
            }
        }
        for(int i = 0; i < mScores.size();i++) {
            cumulativeScore += mScores.get(i);
        }
    }

}
