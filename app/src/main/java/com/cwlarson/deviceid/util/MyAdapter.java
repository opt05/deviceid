package com.cwlarson.deviceid.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cwlarson.deviceid.MainActivity;
import com.cwlarson.deviceid.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = "MyAdapter";
    public static final int VIEW_TYPE_HEADER  = 0, VIEW_TYPE_ITEM = 1;
    private boolean isFiltered = false;
    private Context context;
    private Activity activity;

    private ArrayList<ArrayList<String>> visibleObjects;
    private ArrayList<ArrayList<String>> allObjects;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolderItem extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView,mTextViewBody;
        public ImageButton mMoreButton;
        public ViewHolderItem(final View v) {
            super(v);
            this.mTextView = (TextView) v.findViewById(R.id.item_title);
            this.mTextViewBody = (TextView) v.findViewById(R.id.item_body);
            this.mMoreButton = (ImageButton) v.findViewById(R.id.item_more_button);
            context = v.getContext();
            // Single click of the recyclerview item
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataUtil dataUtil = new DataUtil();
                    Boolean shouldWeClick = dataUtil.onClickAdapter(mTextView.getText().toString(),context,activity);
                    if(shouldWeClick) return;
                    // This isn't a valid body so we shouldn't do anything and inform the user
                    if (mTextViewBody.getText().toString().equals(context.getResources().getString(R.string.not_found))) {
                        View view = activity.findViewById(R.id.main_activity_layout);
                        //Fallback if view in unavailable for snackbar
                        if(view==null)
                            Toast.makeText(context,activity.getResources().getString(R.string.snackbar_not_found_adapter,mTextView.getText().toString()),Toast.LENGTH_LONG).show();
                        else
                            Snackbar.make(activity.findViewById(R.id.main_activity_layout), activity.getResources().getString(R.string.snackbar_not_found_adapter,mTextView.getText().toString()), Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(mTextView.getText().toString());
                    builder.setMessage(mTextViewBody.getText().toString());
                    builder.setPositiveButton(R.string.dialog_long_press_positive_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mMoreButton.performClick();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton(R.string.dialog_long_press_negative_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    MainActivity.dialog = builder.create();
                    MainActivity.dialog.show();
                }
            });
            // Long click of recyclerview item
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // This isn't a valid body so we shouldn't do anything and inform the user
                    if (!mTextViewBody.getText().toString().equals(context.getResources().getString(R.string.not_found))) {
                        DataUtil dataUtil = new DataUtil();
                        dataUtil.copyToClipboard(context,mTextView.getText().toString(),mTextViewBody.getText().toString());
                        return true;
                    }
                    return false;
                }
            });

            mMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(mTextView.getText().toString());
                    //Get menu list
                    final List<String> list = new ArrayList<>();
                    list.addAll(Arrays.asList(context.getResources().getStringArray(R.array.item_menu)));
                    //Get favorite item
                    final DataUtil dataUtil = new DataUtil();
                    if (dataUtil.isFavoriteItem(context,mTextView.getText().toString())) {
                        list.add(context.getResources().getString(R.string.item_menu_unfavorite));
                    } else {
                        list.add(context.getResources().getString(R.string.item_menu_favorite));
                    }
                    builder.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0: //Share
                                    Intent sendIntent = new Intent();
                                    sendIntent.setAction(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, mTextViewBody.getText());
                                    sendIntent.setType("text/plain");
                                    context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.send_to)));
                                    break;
                                case 1: //Copy to clipboard
                                    dataUtil.copyToClipboard(context,mTextView.getText().toString(),mTextViewBody.getText().toString());
                                    break;
                                case 2: //Favorite item stuff
                                    if(list.get(i).equals(context.getResources().getString(R.string.item_menu_favorite))){
                                        // is not a favorite currently
                                        DataUtil dataUtil = new DataUtil();
                                        dataUtil.saveFavoriteItem(context, mTextView.getText().toString());
                                    } else { // is a favorite
                                        DataUtil dataUtil = new DataUtil();
                                        dataUtil.removeFavoriteItem(context, mTextView.getText().toString());
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    MainActivity.dialog = builder.create();
                    MainActivity.dialog.show();
                }
            });
        }
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        public final TextView header;

        public ViewHolderHeader(View itemView){
            super(itemView);
            header = (TextView) itemView.findViewById(R.id.header_title);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<ArrayList<String>> myDataset,Activity parentActivity) {
        allObjects = new ArrayList<>(myDataset);
        visibleObjects = new ArrayList<>(myDataset);
        activity = parentActivity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                   int viewType) {
        if (viewType==VIEW_TYPE_ITEM) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_text_view, parent, false);
            v.setTag(Integer.toString(VIEW_TYPE_ITEM));
            return new ViewHolderItem(v);
        } else if (viewType==VIEW_TYPE_HEADER) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_text_view_header, parent, false);
            v.setTag(Integer.toString(VIEW_TYPE_HEADER));
            return new ViewHolderHeader(v);
        } else {
            throw new RuntimeException("Could not inflate layout");
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (holder instanceof ViewHolderItem) { //Item
            try { //Title
                ((ViewHolderItem) holder).mTextView.setText(visibleObjects.get(position).get(0));
            } catch (ArrayIndexOutOfBoundsException e) {
                ((ViewHolderItem) holder).mTextView.setText("");
            }
            try { //Body
                ((ViewHolderItem) holder).mTextViewBody.setText(visibleObjects.get(position).get(1));
                // Hide the more button if it is unavailable
                if(((ViewHolderItem) holder).mTextViewBody.getText().toString().equals(context.getResources().getString(R.string.not_found))||((ViewHolderItem) holder).mTextViewBody.getText().toString().equals(context.getResources().getString(R.string.phone_permission_denied)))
                    ((ViewHolderItem) holder).mMoreButton.setVisibility(View.GONE);
                else
                    ((ViewHolderItem) holder).mMoreButton.setVisibility(View.VISIBLE);
            } catch (ArrayIndexOutOfBoundsException e) {
                ((ViewHolderItem) holder).mTextViewBody.setText("");
            }

        } else if (holder instanceof ViewHolderHeader) { //Header
            try { //Title
                ((ViewHolderHeader) holder).header.setText(visibleObjects.get(position).get(0));
            } catch (ArrayIndexOutOfBoundsException e) {
                ((ViewHolderHeader) holder).header.setText("");
            }
        } else {
            Log.e(TAG,"No instance of ViewHolder found");
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return visibleObjects.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (visibleObjects.get(position).get(1).equals(DataUtil.HEADER)){
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    public void flushFilter(){
        visibleObjects = new ArrayList<>(allObjects);
        isFiltered=false;
        notifyDataSetChanged();
    }

    public void setFilterFavorite() {
        visibleObjects.clear(); //Clear the list
        DataUtil dataUtil = new DataUtil();
        visibleObjects.add(dataUtil.filteredTitle(context,context.getResources().getString(R.string.filter_title_favorites))); //Add title to filtered list
        for (ArrayList<String> item:allObjects) {
            if (dataUtil.isFavoriteItem(context, item.get(0))) {
                Log.v(TAG, "Showing favorite: "+item.get(0));
                visibleObjects.add(item);
            }
        }
        isFiltered=true;
        notifyDataSetChanged();
    }

    public void setSearch(String queryText) {
        visibleObjects.clear(); //Clear the list
        DataUtil dataUtil = new DataUtil();
        visibleObjects.add(dataUtil.filteredTitle(context,context.getResources().getString(R.string.filter_title_search))); //Add title to filtered list
        for (ArrayList<String> item:allObjects) {
            if ((item.get(0).toLowerCase().contains(queryText) ||
                    item.get(1).toLowerCase().contains(queryText))
                    && !item.get(1).equals(DataUtil.HEADER)) {
                Log.v(TAG, "Showing search: "+item.get(0));
                visibleObjects.add(item);
            }
        }
        isFiltered=true;
        notifyDataSetChanged();
    }

    public boolean isFiltered() {
        return isFiltered;
    }
}
