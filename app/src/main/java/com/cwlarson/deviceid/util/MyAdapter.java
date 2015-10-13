package com.cwlarson.deviceid.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cwlarson.deviceid.MainActivity;
import com.cwlarson.deviceid.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = "MyAdapter";
    private boolean isFiltered = false;
    private Context context;
    private Activity activity;

    private List<List<String>> visibleObjects = new ArrayList<>();
    private List<List<String>> allObjects = new ArrayList<>();
    private TextView mNoItemsTextView;

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
                    new DataUtil(activity).onClickAdapter(mTextView.getText().toString(), mTextViewBody.getText().toString(), mMoreButton);
                }
            });
            // Long click of recyclerview item
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                // This isn't a valid body so we shouldn't do anything and inform the user
                if (!mTextViewBody.getText().toString().equals(context.getResources().getString(R.string.not_found))) {
                    DataUtil dataUtil = new DataUtil(activity);
                    dataUtil.copyToClipboard(mTextView.getText().toString(),mTextViewBody.getText().toString());
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
                    final DataUtil dataUtil = new DataUtil(activity);
                    if (dataUtil.isFavoriteItem(mTextView.getText().toString())) {
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
                                    dataUtil.copyToClipboard(mTextView.getText().toString(),mTextViewBody.getText().toString());
                                    break;
                                case 2: //Favorite item stuff
                                    if(list.get(i).equals(context.getResources().getString(R.string.item_menu_favorite))){
                                        // is not a favorite currently
                                        DataUtil dataUtil = new DataUtil(activity);
                                        dataUtil.saveFavoriteItem(mTextView.getText().toString());
                                    } else { // is a favorite
                                        DataUtil dataUtil = new DataUtil(activity);
                                        dataUtil.removeFavoriteItem(mTextView.getText().toString());
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
    public MyAdapter(Activity parentActivity, TextView noItemsTextView) {
        activity = parentActivity;
        mNoItemsTextView = noItemsTextView;
        setNoItemsTextViewVisible();
    }

    public void addItem(List<String> yourObject) {
        allObjects.add(yourObject);
        visibleObjects.add(yourObject);
        setNoItemsTextViewVisible();
        notifyItemInserted(allObjects.size() - 1);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);
            return new ViewHolderItem(v);
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

    public void flushFilter(){
        visibleObjects.clear(); //Clear the list
        visibleObjects.addAll(allObjects);
        isFiltered=false;
        setNoItemsTextViewVisible();
        notifyDataSetChanged();
    }

    public void setFilterFavorite() {
        visibleObjects.clear(); //Clear the list
        DataUtil dataUtil = new DataUtil(activity);
        for (List<String> item:allObjects) {
            if (dataUtil.isFavoriteItem(item.get(0))) {
                Log.v(TAG, "Showing favorite: "+item.get(0));
                visibleObjects.add(item);
            }
        }
        isFiltered=true;
        setNoItemsTextViewVisible();
        notifyDataSetChanged();
    }

    private void setNoItemsTextViewVisible() {
        if (getItemCount()<=0)
            mNoItemsTextView.setVisibility(View.VISIBLE);
        else
            mNoItemsTextView.setVisibility(View.GONE);
    }

    public void setSearch(String queryText) {
        visibleObjects.clear(); //Clear the list
        for (List<String> item:allObjects) {
            if ((item.get(0).toLowerCase().contains(queryText) ||
                    item.get(1).toLowerCase().contains(queryText))) {
                Log.v(TAG, "Showing search: "+item.get(0));
                visibleObjects.add(item);
            }
        }
        isFiltered=true;
        setNoItemsTextViewVisible();
        notifyDataSetChanged();
    }

    public boolean isFiltered() {
        return isFiltered;
    }
}
