package com.cwlarson.deviceid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.cwlarson.deviceid.BR;
import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.CustomViewHolder> {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = "MyAdapter";
    private Context context;
    private AppCompatActivity activity;
    AdapterView.OnItemClickListener onItemClickListener

    private final SortedList<Item> visibleObjects;
    private final View mNoItemsTextView;
    private final RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            checkAdapterIsEmpty();
        }

        @Override
        public void onChanged() {
            super.onChanged();
            checkAdapterIsEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            checkAdapterIsEmpty();
        }
    };

    private void checkAdapterIsEmpty() {
        if(getItemCount()==0) {
            mNoItemsTextView.setVisibility(View.VISIBLE);
        } else {
            mNoItemsTextView.setVisibility(View.GONE);
        }
    }

    private final View.OnAttachStateChangeListener mAttachListener = new View.OnAttachStateChangeListener() {

        private boolean isRegistered;

        @Override
        public void onViewAttachedToWindow(final View v) {
            if (!isRegistered) {
                isRegistered = true;
                registerAdapterDataObserver(dataObserver);
            }
        }

        @Override
        public void onViewDetachedFromWindow(final View v) {
            if (isRegistered) {
                isRegistered = false;
                unregisterAdapterDataObserver(dataObserver);
            }
        }
    };

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView.removeOnAttachStateChangeListener(mAttachListener);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnAttachStateChangeListener(mAttachListener);
    }

    public MyAdapter(AppCompatActivity parentActivity, TextView noItemsTextView) {
        activity=parentActivity;
        context = parentActivity.getApplicationContext();
        mNoItemsTextView = noItemsTextView;
        visibleObjects = new SortedList<>(Item.class, new SortedListAdapterCallback<Item>(this) {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(Item oldItem, Item newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(Item item1, Item item2) {
                return item1.equals(item2);
            }
        });
        checkAdapterIsEmpty();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void onItemHolderClick(CustomViewHolder holder) {
        if(onItemClickListener!=null) onItemClickListener.onItemClick(null, holder.itemView, holder.getAdapterPosition(), holder.getItemId());
    }

    public void add(Item item){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences.getBoolean("hide_unables",false) && !item.getHasOptions(context)) return;
        visibleObjects.add(item);
    }

    public void clear(){
        visibleObjects.beginBatchedUpdates();
        while(visibleObjects.size()>0){
            visibleObjects.removeItemAt(visibleObjects.size()-1);
        }
        visibleObjects.endBatchedUpdates();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CustomViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        ViewDataBinding viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.my_text_view, parent, false);
        return new CustomViewHolder(viewDataBinding);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ViewDataBinding viewDataBinding = holder.getViewDataBinding();
        viewDataBinding.setVariable(BR.item, visibleObjects.get(position));
        viewDataBinding.setVariable(BR.activity, activity);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return visibleObjects.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ViewDataBinding mViewDataBinding;

        CustomViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding.getRoot());

            mViewDataBinding = viewDataBinding;
            mViewDataBinding.executePendingBindings();
        }

        ViewDataBinding getViewDataBinding() {
            return mViewDataBinding;
        }

        @Override
        public void onClick(View view) {
            notifyItemRangeChanged(0, );
        }
    }
}
