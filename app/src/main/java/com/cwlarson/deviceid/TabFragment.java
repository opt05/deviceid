package com.cwlarson.deviceid;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cwlarson.deviceid.database.AllItemsViewModel;
import com.cwlarson.deviceid.database.AppDatabase;
import com.cwlarson.deviceid.database.DatabaseInitializer;
import com.cwlarson.deviceid.database.Status;
import com.cwlarson.deviceid.databinding.FragmentTabsBinding;
import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.databinding.ItemType;
import com.cwlarson.deviceid.util.MyAdapter;
import com.cwlarson.deviceid.util.SystemUtils;

import java.util.List;

public class TabFragment extends Fragment implements SharedPreferences
    .OnSharedPreferenceChangeListener, DatabaseInitializer.OnPopulate {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static final String TAG = "TabFragment";
    private MyAdapter mAdapter;
    private FragmentTabsBinding binding;
    private Context appContext;
    private SharedPreferences mPreferences;
    private ItemType itemType;
    private AllItemsViewModel mModel;

    public static TabFragment newInstance(int tabInteger) {
        TabFragment dtf = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("tab", tabInteger);
        dtf.setArguments(bundle);
        return dtf;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.appContext = context.getApplicationContext();
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        if(getArguments()==null) return;
        switch (getArguments().getInt("tab")){
            case 0:
                itemType = ItemType.DEVICE;
                break;
            case 1:
                itemType = ItemType.NETWORK;
                break;
            case 2:
                itemType = ItemType.SOFTWARE;
                break;
            case 3:
                itemType = ItemType.HARDWARE;
                break;
        }

        mModel = ViewModelProviders.of(this).get(AllItemsViewModel.class);
        mModel.setHideUnavailable(mPreferences.getBoolean("hide_unables",false));
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        if(mPreferences!=null)
            mPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Loads the data into Room
     */
    private void loadData() {
        DatabaseInitializer.populateAsync(getActivity(), AppDatabase.getDatabase
            (appContext), itemType,TabFragment.this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_tabs,container,false);

        GridLayoutManager mLayoutManager;
        if(getContext()!=null) {
            mLayoutManager = new GridLayoutManager(getActivity(),
                SystemUtils.calculateNoOfColumns(getContext()));
        } else {
            mLayoutManager = new GridLayoutManager(getActivity(),1);
        }
        binding.recyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter((MainActivity) getActivity());
        binding.recyclerView.setAdapter(mAdapter);

        // Setup SwipeToRefresh
        binding.swipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        binding.swipeToRefreshLayout.setColorSchemeResources(R.color.accent_color);
        mModel.getAllItems(itemType).observe(this, new
            Observer<List<Item>>() {
            @Override
            public void onChanged(@Nullable List<Item> items) {
                mAdapter.setItems(items);
                binding.setItemsCount(items==null?0:items.size());
                // Espresso does not know how to wait for data binding's loop so we execute changes sync.
                binding.executePendingBindings();
            }
        });
        mModel.getStatus().observe(this, new Observer<Status>() {
            @Override
            public void onChanged(@Nullable Status status) {
                binding.swipeToRefreshLayout.setRefreshing(status!=null
                    && status == Status.LOADING);
            }
        });
        loadData();
        return binding.getRoot();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("hide_unables")) {
            mModel.setHideUnavailable(sharedPreferences.getBoolean("hide_unables",false));
        }
    }

    @Override
    public void status(Status status) {
        mModel.setStatus(status);
    }
}
