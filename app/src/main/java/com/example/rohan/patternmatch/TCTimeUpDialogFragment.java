package com.example.rohan.patternmatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by rohan on 11/3/2017.
 */

// https://developer.android.com/guide/topics/ui/dialogs.html
public class TCTimeUpDialogFragment extends DialogFragment {
    public static interface OnCompleteListener {
        public abstract void onAnswer(String time);
    }

    private OnCompleteListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity){
            a=(Activity) context;
            try {
                this.mListener = (OnCompleteListener)a;
            }
            catch (final ClassCastException e) {
                throw new ClassCastException(a.toString() + " must implement OnCompleteListener");
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.time_up)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAnswer("yes");
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAnswer("no");
                    }
                });
        return builder.create();
    }
}
