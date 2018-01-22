package com.cwlarson.deviceid.util;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cwlarson.deviceid.BR;
import com.cwlarson.deviceid.PermissionsActivity;
import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.databinding.Item;

import java.lang.ref.WeakReference;
import java.util.List;

public class MyAdapter extends DiffUtilAdapter<Item, MyAdapter.CustomViewHolder> {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = MyAdapter.class.getSimpleName();
    private WeakReference<PermissionsActivity> handler = new WeakReference<>(null);
    private static final int TEXT_VIEWTYPE=0,CHART_VIEWTYPE=1;

    public MyAdapter(PermissionsActivity parentActivity) {
        handler = new WeakReference<>(parentActivity);
    }

    public void setItems(final List<Item> itemList) {
        update(itemList);
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
        if(getDataset().get(position).getChartitem()!=null)
            return CHART_VIEWTYPE;
        else
            return TEXT_VIEWTYPE;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.bind(getDataset().get(position),handler.get());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return getDataset().size();
        //return (visibleObjects==null)?0:visibleObjects.size();
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
