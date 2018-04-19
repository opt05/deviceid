package com.cwlarson.deviceid.util;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cwlarson.deviceid.BR;
import com.cwlarson.deviceid.PermissionsActivity;
import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;

import java.lang.ref.WeakReference;
import java.util.List;

public class MyAdapter extends ListAdapter<Item, MyAdapter.CustomViewHolder> {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = MyAdapter.class.getSimpleName();
    private WeakReference<PermissionsActivity> handler;
    private static final int TEXT_VIEWTYPE=0,CHART_VIEWTYPE=1;

    public MyAdapter(PermissionsActivity parentActivity) {
        super(new DiffUtil.ItemCallback<Item>() {
            @Override
            public boolean areItemsTheSame(Item oldItem, Item newItem) {
                return oldItem.itemsTheSame(newItem);
            }

            @Override
            public boolean areContentsTheSame(Item oldItem, Item newItem) {
                return oldItem.equals(newItem);
            }
        });
        handler = new WeakReference<>(parentActivity);
    }

    public void setItems(List<Item> itemList) {
        submitList(itemList);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        ViewDataBinding viewDataBinding;
        if(viewType==CHART_VIEWTYPE)
            viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.recycler_chart_view, parent, false);
        else
            viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.recycler_text_view, parent, false);
        return new CustomViewHolder(viewDataBinding);
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position).getChartitem()!=null)
            return CHART_VIEWTYPE;
        else
            return TEXT_VIEWTYPE;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.bind(getItem(position),handler.get());
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding binding;

        CustomViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Item item, AppCompatActivity activity) {
            binding.setVariable(BR.item, item);
            binding.setVariable(BR.handler, new ItemClickHandler(activity, item));
            binding.executePendingBindings();
        }
    }
}
