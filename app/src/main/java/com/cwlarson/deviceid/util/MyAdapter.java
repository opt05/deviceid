package com.cwlarson.deviceid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.preference.PreferenceManager;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cwlarson.deviceid.BR;
import com.cwlarson.deviceid.PermissionsActivity;
import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.CustomViewHolder> implements SharedPreferences.OnSharedPreferenceChangeListener {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = MyAdapter.class.getSimpleName();
    private Context context;
    private PermissionsActivity handler;
    private static final int TEXT_VIEWTYPE=0,CHART_VIEWTYPE=1;

    private SortedList<Item> visibleObjects;
    private List<Item> originalObjects;
    private View mNoItemsTextView;
    private SharedPreferences mPreferences;
    private boolean mFilterFavorites, mFilterUnavailable;
    private Set<String> mFavorites = new HashSet<>();

    private RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
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
        if(mPreferences!=null) mPreferences.unregisterOnSharedPreferenceChangeListener(MyAdapter.this);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnAttachStateChangeListener(mAttachListener);
        if(mPreferences!=null) mPreferences.registerOnSharedPreferenceChangeListener(MyAdapter.this);
    }

    public MyAdapter(PermissionsActivity parentActivity, TextView noItemsTextView, boolean... filterFavorite) {
        handler = parentActivity;
        context = parentActivity.getApplicationContext();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mFilterFavorites = (filterFavorite.length > 0) && filterFavorite[0];
        mFilterUnavailable = mPreferences.getBoolean("hide_unables",false);
        mFavorites.clear();
        mFavorites.addAll(mPreferences.getStringSet(Item.favItemKey, new HashSet<String>()));
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
                return item1.getId()==item2.getId();
            }
        });
        originalObjects=new ArrayList<>();
        checkAdapterIsEmpty();
    }

    public void add(Item item){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        item.setFavorite(mFavorites.contains(String.valueOf(item.getId())));
        if((!sharedPreferences.getBoolean("hide_unables",false))) {
            if(!mFilterFavorites || item.isFavorite()) visibleObjects.add(item);
        } else if((sharedPreferences.getBoolean("hide_unables",false)) && !item.getIsUnavailable(context))
            if(!mFilterFavorites || item.isFavorite()) visibleObjects.add(item);
        originalObjects.add(item);
    }

    private void clearFilter() {
        List<Item> tempAdd = new ArrayList<>();
        // First, check current list and remove no longer matching
        for (int i = 0; i < visibleObjects.size(); i++) {
            visibleObjects.get(i).setFavorite(mFavorites.contains(String.valueOf(visibleObjects.get(i).getId())));
        }
        // Second, get the new items it matches
        for (int j = 0; j < originalObjects.size(); j++) {
            originalObjects.get(j).setFavorite(mFavorites.contains(String.valueOf(originalObjects.get(j).getId())));
            if (visibleObjects.indexOf(originalObjects.get(j))==SortedList.INVALID_POSITION)
                tempAdd.add(originalObjects.get(j));
        }
        // Third, update visible list
        visibleObjects.beginBatchedUpdates();
        for (Item item2 : tempAdd) {
            visibleObjects.add(item2);
        }
        visibleObjects.endBatchedUpdates();
        /*visibleObjects.beginBatchedUpdates();
        visibleObjects.clear();
        for(int i=0;i<originalObjects.size();i++){
            originalObjects.get(i).setFavorite(mFavorites.contains(String.valueOf(originalObjects.get(i).getId())));
            visibleObjects.add(originalObjects.get(i));
        }
        visibleObjects.endBatchedUpdates();*/
    }

    public void filter(String text) {
        if(TextUtils.isEmpty(text)) {
            clearFilter();
            return;
        }
        List<Item> tempRemove = new ArrayList<>(), tempAdd = new ArrayList<>();
        // First, check current list and remove no longer matching
        for(int i=0;i<visibleObjects.size();i++) {
            if(!visibleObjects.get(i).matchesSearchText(text))
                tempRemove.add(visibleObjects.get(i));
        }
        // Second, get the new items it matches
        for(int j=0;j<originalObjects.size();j++){
            if(originalObjects.get(j).matchesSearchText(text))
                tempAdd.add(originalObjects.get(j));
        }
        // Third, update visible list
        visibleObjects.beginBatchedUpdates();
        for(Item item: tempRemove) {
            visibleObjects.remove(item);
        }
        for(Item item2:tempAdd) {
            visibleObjects.add(item2);
        }
        visibleObjects.endBatchedUpdates();
    }

    private void filterOther() {
        if(mFilterUnavailable && !mFilterFavorites) {// Unavailable only
            List<Item> tempRemove = new ArrayList<>(), tempAdd = new ArrayList<>();
            // First, check current list and remove no longer matching
            for (int i = 0; i < visibleObjects.size(); i++) {
                visibleObjects.get(i).setFavorite(mFavorites.contains(String.valueOf(visibleObjects.get(i).getId())));
                if (visibleObjects.get(i).getIsUnavailable(context))
                    tempRemove.add(visibleObjects.get(i));
            }
            // Second, get the new items it matches
            for (int j = 0; j < originalObjects.size(); j++) {
                originalObjects.get(j).setFavorite(mFavorites.contains(String.valueOf(originalObjects.get(j).getId())));
                if (!originalObjects.get(j).getIsUnavailable(context))
                    tempAdd.add(originalObjects.get(j));
            }
            // Third, update visible list
            visibleObjects.beginBatchedUpdates();
            for (Item item : tempRemove) {
                visibleObjects.remove(item);
            }
            for (Item item2 : tempAdd) {
                visibleObjects.add(item2);
            }
            visibleObjects.endBatchedUpdates();
        } else if(!mFilterUnavailable && mFilterFavorites) { // Favs only
            List<Item> tempRemove = new ArrayList<>(), tempAdd = new ArrayList<>();
            // First, check current list and remove no longer matching
            for (int i = 0; i < visibleObjects.size(); i++) {
                visibleObjects.get(i).setFavorite(mFavorites.contains(String.valueOf(visibleObjects.get(i).getId())));
                if (!visibleObjects.get(i).isFavorite())
                    tempRemove.add(visibleObjects.get(i));
            }
            // Second, get the new items it matches
            for (int j = 0; j < originalObjects.size(); j++) {
                originalObjects.get(j).setFavorite(mFavorites.contains(String.valueOf(originalObjects.get(j).getId())));
                if (originalObjects.get(j).isFavorite())
                    tempAdd.add(originalObjects.get(j));
            }
            // Third, update visible list
            visibleObjects.beginBatchedUpdates();
            for (Item item : tempRemove) {
                visibleObjects.remove(item);
            }
            for (Item item2 : tempAdd) {
                visibleObjects.add(item2);
            }
            visibleObjects.endBatchedUpdates();
        } else if(mFilterUnavailable) { // Both filters
            List<Item> tempRemove = new ArrayList<>(), tempAdd = new ArrayList<>();
            // First, check current list and remove no longer matching
            for (int i = 0; i < visibleObjects.size(); i++) {
                visibleObjects.get(i).setFavorite(mFavorites.contains(String.valueOf(visibleObjects.get(i).getId())));
                if (visibleObjects.get(i).getIsUnavailable(context) || !visibleObjects.get(i).isFavorite())
                    tempRemove.add(visibleObjects.get(i));
            }
            // Second, get the new items it matches
            for (int j = 0; j < originalObjects.size(); j++) {
                originalObjects.get(j).setFavorite(mFavorites.contains(String.valueOf(originalObjects.get(j).getId())));
                if(!originalObjects.get(j).getIsUnavailable(context) && originalObjects.get(j).isFavorite())
                    tempAdd.add(originalObjects.get(j));
            }
            // Third, update visible list
            visibleObjects.beginBatchedUpdates();
            for (Item item : tempRemove) {
                visibleObjects.remove(item);
            }
            for (Item item2 : tempAdd) {
                visibleObjects.add(item2);
            }
            visibleObjects.endBatchedUpdates();
        } else { // No filters
            clearFilter();
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CustomViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        ViewDataBinding viewDataBinding;
        if(viewType==CHART_VIEWTYPE)
            viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.recycler_chart_view, parent, false);
        else
            viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.recycler_text_view, parent, false);
        return new CustomViewHolder(viewDataBinding);
    }

    @Override
    public int getItemViewType(int position) {
        if(visibleObjects.get(position).getChartItem()!=null)
            return CHART_VIEWTYPE;
        else
            return TEXT_VIEWTYPE;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.bind(visibleObjects.get(position),handler);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return visibleObjects.size();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("hide_unables")) {
            mFilterUnavailable = sharedPreferences.getBoolean("hide_unables",false);
            filterOther();
        } else if(s.equals(Item.favItemKey)) {
            mFavorites.clear();
            mFavorites.addAll(sharedPreferences.getStringSet(s,new HashSet<String>()));
            filterOther();
        }
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        CustomViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Item item, PermissionsActivity handler) {
            binding.setVariable(BR.item, item);
            binding.setVariable(BR.handler, handler);
            binding.executePendingBindings();
        }
    }
}
