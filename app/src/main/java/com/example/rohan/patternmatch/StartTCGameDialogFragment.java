package com.example.rohan.patternmatch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by rohan on 11/3/2017.
 */

// https://developer.android.com/guide/topics/ui/dialogs.html
public class StartTCGameDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.time_challenge_prompt)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startNewTCGame();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    private void startNewTCGame() {
        Intent intent = new Intent(getActivity(), GameActivity.class);
        int gameID = -1;
        boolean timeChallenge = true;
        intent.putExtra("GAME_ID", gameID);
        intent.putExtra("TIME_CHALLENGE", timeChallenge);
        startActivity(intent);
    }
}
