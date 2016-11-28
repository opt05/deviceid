package com.cwlarson.deviceid.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.util.DataUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemMoreButtonDialog extends AppCompatDialogFragment {
    public ItemMoreButtonDialog() {
        // Empty
    }

    public static ItemMoreButtonDialog newInstance(Item item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("item",item);
        ItemMoreButtonDialog dialog = new ItemMoreButtonDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Item item  = getArguments().getParcelable("item");
        builder.setTitle(item==null ? "" : item.getTitle());
        //Get menu list
        final List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(getActivity().getResources().getStringArray(R.array.item_menu)));
        //Get favorite item
        final DataUtil dataUtil = new DataUtil(getActivity());
        if (dataUtil.isFavoriteItem(item==null ? "" : item.getTitle())) {
            list.add(getActivity().getResources().getString(R.string.item_menu_unfavorite));
        } else {
            list.add(getActivity().getResources().getString(R.string.item_menu_favorite));
        }
        builder.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0: //Share
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, item==null ? "" : item.getSubTitle());
                        sendIntent.setType("text/plain");
                        getActivity().startActivity(Intent.createChooser(sendIntent, getActivity().getResources().getText(R.string.send_to)));
                        break;
                    case 1: //Copy to clipboard
                        dataUtil.copyToClipboard(item==null ? "" : item.getTitle(), item==null ? "" : item.getSubTitle());
                        break;
                    case 2: //Favorite item stuff
                        if (list.get(i).equals(getActivity().getResources().getString(R.string.item_menu_favorite))) {
                            // is not a favorite currently
                            DataUtil dataUtil = new DataUtil(getActivity());
                            dataUtil.saveFavoriteItem(item==null ? "" : item.getTitle(), item==null ? "" : item.getSubTitle());
                        } else { // is a favorite
                            DataUtil dataUtil = new DataUtil(getActivity());
                            dataUtil.removeFavoriteItem(item==null ? "" : item.getTitle(), item==null ? "" : item.getSubTitle());
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        return builder.create();
    }
}
