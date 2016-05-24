package com.cwlarson.deviceid.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.cwlarson.deviceid.R;

public class ItemClickDialog extends AppCompatDialogFragment {
    public ItemClickDialog() {
        //Empty
    }

    public static ItemClickDialog newInstance(String title, String subTitle) {
        Bundle bundle = new Bundle();
        bundle.putString("title",title);
        bundle.putString("subTitle",subTitle);
        ItemClickDialog dialog = new ItemClickDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString("title"));
        builder.setMessage(getArguments().getString("subTitle"));
        builder.setPositiveButton(R.string.dialog_long_press_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ItemMoreButtonDialog.newInstance(getArguments().getString("title"),getArguments().getString("subTitle")).show(getActivity().getSupportFragmentManager(),"itemMoreButtonDialog");
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.dialog_long_press_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        return builder.create();
    }
}
