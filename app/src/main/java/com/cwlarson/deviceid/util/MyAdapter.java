package com.cwlarson.deviceid.util;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cwlarson.deviceid.R;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String TAG = "MyAdapter";
    public static final int VIEW_TYPE_HEADER  = 0;
    public static final int VIEW_TYPE_ITEM = 1;
    private String[][] mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolderItem extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public TextView mTextViewBody;
        public ViewHolderItem(View v) {
            super(v);
            this.mTextView = (TextView) v.findViewById(R.id.item_title);
            this.mTextViewBody = (TextView) v.findViewById(R.id.item_body);
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
    public MyAdapter(String[][] myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
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
                ((ViewHolderItem) holder).mTextView.setText(mDataset[position][0]);
            } catch (ArrayIndexOutOfBoundsException e) {
                ((ViewHolderItem) holder).mTextView.setText("");
            }
            try { //Body
                ((ViewHolderItem) holder).mTextViewBody.setText(mDataset[position][1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                ((ViewHolderItem) holder).mTextViewBody.setText("");
            }
        } else if (holder instanceof ViewHolderHeader) { //Header
            try { //Title
                ((ViewHolderHeader) holder).header.setText(mDataset[position][0]);
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
        return mDataset.length;
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataset[position][1].equals(DataUtil.HEADER)){
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
        //return position % 2;
    }
}
