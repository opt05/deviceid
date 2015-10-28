package com.cwlarson.deviceid.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cwlarson.deviceid.MainActivity;
import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.data.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = "MyAdapter";
    private Context context;
    private final Activity activity;

    private final SortedList<Item> visibleObjects;
    private final TextView mNoItemsTextView;

    public MyAdapter(Activity parentActivity, TextView noItemsTextView) {
        activity = parentActivity;
        context = activity.getApplicationContext();
        mNoItemsTextView = noItemsTextView;
        visibleObjects = new SortedList<>(Item.class, new SortedList.Callback<Item>() {

            @Override
            public int compare(Item o1, Item o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }

            @Override
            public void onInserted(int position, int count) {
                setNoItemsTextViewVisible();
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                setNoItemsTextViewVisible();
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                setNoItemsTextViewVisible();
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Item oldItem, Item newItem) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }

            @Override
            public boolean areItemsTheSame(Item item1, Item item2) {
                return item1.getTitle().equals(item2.getTitle()) &&
                        item1.getSubTitle().equals(item2.getSubTitle());
            }
        });
        setNoItemsTextViewVisible();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolderItem extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public final TextView mTextView;
        public final TextView mTextViewBody;
        public final ImageButton mMoreButton;
        public final ImageView mFavStar;
        public ViewHolderItem(final View v) {
            super(v);
            this.mTextView = (TextView) v.findViewById(R.id.item_title);
            this.mTextViewBody = (TextView) v.findViewById(R.id.item_body);
            this.mMoreButton = (ImageButton) v.findViewById(R.id.item_more_button);
            this.mFavStar = (ImageView) v.findViewById(R.id.item_fav_star);
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
                if (!isNotRealItem(mTextViewBody.getText().toString())) {
                    DataUtil dataUtil = new DataUtil(activity);
                    dataUtil.copyToClipboard(mTextView.getText().toString(),mTextViewBody.getText().toString());
                    return true;
                }
                return false;
                }
            });
            // Set more button click event
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
                                    dataUtil.copyToClipboard(mTextView.getText().toString(), mTextViewBody.getText().toString());
                                    break;
                                case 2: //Favorite item stuff
                                    if (list.get(i).equals(context.getResources().getString(R.string.item_menu_favorite))) {
                                        // is not a favorite currently
                                        DataUtil dataUtil = new DataUtil(activity);
                                        dataUtil.saveFavoriteItem(mTextView.getText().toString(), mTextViewBody.getText().toString());
                                    } else { // is a favorite
                                        DataUtil dataUtil = new DataUtil(activity);
                                        dataUtil.removeFavoriteItem(mTextView.getText().toString(), mTextViewBody.getText().toString());
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

    /*public Item get(int position){
        return visibleObjects.get(position);
    }*/

    public void add(Item item){
        visibleObjects.add(item);
    }

    /*public int indexOf(Item item){
        return visibleObjects.indexOf(item);
    }

    public void updateItemAt(int index, Item item){
        visibleObjects.updateItemAt(index,item);
    }*/

    public void addAll(List<Item> items){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        visibleObjects.beginBatchedUpdates();
        for (Item item : items){
            // Skips current item if not real and hide setting is on
            if(sharedPreferences.getBoolean("hide_unables",false)&&isNotRealItem(item.getSubTitle())) continue;
            visibleObjects.add(item);
        }
        visibleObjects.endBatchedUpdates();
    }

    /*public void addAll(Item[] items){
        addAll(Arrays.asList(items));
    }*/

    public void remove(Item item){
        visibleObjects.remove(item);
    }

    public void setFavStar(Item item){
        // Set view of fav star icon
        int index = visibleObjects.indexOf(item);
        if(index>=0) {
            visibleObjects.removeItemAt(index);
            visibleObjects.add(item);
        }
    }

    /*public Item removeItemAt(int index){
        return visibleObjects.removeItemAt(index);
    }*/

    public void clear(){
        visibleObjects.beginBatchedUpdates();
        while(visibleObjects.size()>0){
            visibleObjects.removeItemAt(visibleObjects.size()-1);
        }
        visibleObjects.endBatchedUpdates();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);
            return new ViewHolderItem(v);
    }

    private Boolean isNotRealItem(String textviewBody){
        return textviewBody.equals(context.getResources().getString(R.string.not_found))
                || textviewBody.equals(context.getResources().getString(R.string.phone_permission_denied))
                || textviewBody.startsWith(context.getResources().getString(R.string.no_longer_possible).replace("%s", ""))
                || textviewBody.startsWith(context.getResources().getString(R.string.not_possible_yet).replace("%s", ""));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (holder instanceof ViewHolderItem) { //Item
            try { //Title
                ((ViewHolderItem) holder).mTextView.setText(visibleObjects.get(position).getTitle());
                // Set view of fav star icon
                DataUtil dataUtil = new DataUtil(activity);
                if (dataUtil.isFavoriteItem(((ViewHolderItem) holder).mTextView.getText().toString()))
                    ((ViewHolderItem) holder).mFavStar.setVisibility(View.VISIBLE);
                else
                    ((ViewHolderItem) holder).mFavStar.setVisibility(View.GONE);
            } catch (ArrayIndexOutOfBoundsException e) {
                ((ViewHolderItem) holder).mTextView.setText("");
            }
            try { //Body
                ((ViewHolderItem) holder).mTextViewBody.setText(visibleObjects.get(position).getSubTitle());
                // Hide the more button if it is unavailable
                if(isNotRealItem(((ViewHolderItem) holder).mTextViewBody.getText().toString()))
                    ((ViewHolderItem) holder).mMoreButton.setVisibility(View.GONE);
                else
                    ((ViewHolderItem) holder).mMoreButton.setVisibility(View.VISIBLE);
            } catch (ArrayIndexOutOfBoundsException e) {
                ((ViewHolderItem) holder).mTextViewBody.setText("");
            }

        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return visibleObjects.size();
    }

    private void setNoItemsTextViewVisible() {
        if (getItemCount()<=0)
            mNoItemsTextView.setVisibility(View.VISIBLE);
        else
            mNoItemsTextView.setVisibility(View.GONE);
    }
}
