package com.cwlarson.deviceid;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cwlarson.deviceid.data.Device;
import com.cwlarson.deviceid.data.Hardware;
import com.cwlarson.deviceid.data.Network;
import com.cwlarson.deviceid.data.Software;
import com.cwlarson.deviceid.databinding.FragmentTabsBinding;
import com.cwlarson.deviceid.util.MyAdapter;

public class TabFragment extends Fragment {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static final String TAG = "TabFragment";
    private MyAdapter mAdapter;
    private FragmentTabsBinding binding;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.recyclerView.clearOnScrollListeners(); //avoid possible memory leak
    }

    public static TabFragment newInstance(int tabInteger) {
        TabFragment dtf = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("tab", tabInteger);
        dtf.setArguments(bundle);
        return dtf;
    }

    public void setSwipeToRefreshEnabled(Boolean enabled){
        binding.swipeToRefreshLayout.setEnabled(enabled);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch (getArguments().getInt("tab")){
            case 0:
                new Device(getActivity()).setDeviceTiles(mAdapter, false);
                break;
            case 1:
                new Network(getActivity()).setNetworkTiles(mAdapter, false);
                break;
            case 2:
                new Software(getActivity()).setSoftwareTiles(mAdapter, false);
                break;
            case 3:
                new Hardware(getActivity()).setHardwareTiles(mAdapter, false);
                break;
            default:
                new Device(getActivity()).setDeviceTiles(mAdapter, false);
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_tabs,container,false);
        View view = binding.getRoot();

        // use a linear layout manager
        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(),getResources().getInteger(R.integer.grid_layout_columns));
        binding.recyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter((AppCompatActivity) getActivity(), binding.textviewRecyclerviewNoItems);
        binding.recyclerView.setAdapter(mAdapter);

        // Setup SwipeToRefresh
        binding.swipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.recyclerView.setAdapter(mAdapter);
                if(binding.swipeToRefreshLayout.isRefreshing()) binding.swipeToRefreshLayout.setRefreshing(false);
            }
        });
        binding.swipeToRefreshLayout.setColorSchemeResources(R.color.accent_color);
        return view;
    }
}
