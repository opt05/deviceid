package com.cwlarson.deviceid.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cwlarson.deviceid.R;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = "MyAdapter";
    public static final int VIEW_TYPE_HEADER  = 0;
    public static final int VIEW_TYPE_ITEM = 1;
    private boolean isFiltered = false;
    private Toast toast;
    private Context context;

    private ArrayList<ArrayList<String>> visibleObjects;
    private ArrayList<ArrayList<String>> allObjects;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolderItem extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView,mTextViewBody;
        public ImageButton mShareButton,mFavoriteButtonFalse, mFavoriteButtonTrue;
        public ViewHolderItem(View v) {
            super(v);
            this.mTextView = (TextView) v.findViewById(R.id.item_title);
            this.mTextViewBody = (TextView) v.findViewById(R.id.item_body);
            this.mShareButton = (ImageButton) v.findViewById(R.id.item_share_button);
            this.mFavoriteButtonFalse = (ImageButton) v.findViewById(R.id.item_favorite_button_false);
            this.mFavoriteButtonTrue = (ImageButton) v.findViewById(R.id.item_favorite_button_true);
            context = v.getContext();
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(mTextView.getText(), mTextViewBody.getText());
                    clipboard.setPrimaryClip(clip);
                    //Prevents multiple times toast issue with the button
                    if(toast != null) toast.cancel();
                    toast = Toast.makeText(v.getContext(),
                            v.getContext().getResources().getString(R.string.copy_to_clipboard).replace(v.getContext().getResources().getString(R.string.copy_to_clipboard_replace),mTextView.getText()),
                            Toast.LENGTH_SHORT);
                    toast.show();
                    return false;
                }
            });
            mShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, mTextViewBody.getText());
                    sendIntent.setType("text/plain");
                    v.getContext().startActivity(Intent.createChooser(sendIntent, v.getContext().getResources().getText(R.string.send_to)));
                }
            });
            mFavoriteButtonFalse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataUtil dataUtil = new DataUtil();
                    dataUtil.saveFavoriteItem(v.getContext(), mTextView.getText().toString());
                    mFavoriteButtonFalse.setVisibility(View.GONE);
                    mFavoriteButtonTrue.setVisibility(View.VISIBLE);
                }
            });
            mFavoriteButtonTrue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataUtil dataUtil = new DataUtil();
                    dataUtil.removeFavoriteItem(v.getContext(), mTextView.getText().toString());
                    mFavoriteButtonFalse.setVisibility(View.VISIBLE);
                    mFavoriteButtonTrue.setVisibility(View.GONE);
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
    public MyAdapter(ArrayList<ArrayList<String>> myDataset) {
        allObjects = new ArrayList<>(myDataset);
        visibleObjects = new ArrayList<>(myDataset);
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
            } catch (ArrayIndexOutOfBoundsException e) {
                ((ViewHolderItem) holder).mTextViewBody.setText("");
            }
            //Set favorite star buttons
            DataUtil dataUtil = new DataUtil();
            if (dataUtil.isFavoriteItem(context,visibleObjects.get(position).get(0))) {
                ((ViewHolderItem) holder).mFavoriteButtonTrue.setVisibility(View.VISIBLE);
                ((ViewHolderItem) holder).mFavoriteButtonFalse.setVisibility(View.GONE);
            } else {
                ((ViewHolderItem) holder).mFavoriteButtonTrue.setVisibility(View.GONE);
                ((ViewHolderItem) holder).mFavoriteButtonFalse.setVisibility(View.VISIBLE);
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
