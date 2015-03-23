package com.cwlarson.deviceid;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.cwlarson.deviceid.ui.DividerItemDecoration;
import com.cwlarson.deviceid.util.DataUtil;
import com.cwlarson.deviceid.util.MyAdapter;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {

    private RecyclerView mRecyclerView;
    private MenuItem searchItem;
    private SearchView searchView;
    private final String TAG = MainActivity.this.getClass().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //add decoration
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext()));

        // specify an adapter (see also next example)
        RecyclerView.Adapter mAdapter = new MyAdapter(populateDataset());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            searchView.clearFocus();
            searchItem.collapseActionView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //handled by AppCompat (nothing to do here)
                return true;
            case R.id.action_filter:
                if (!((MyAdapter) mRecyclerView.getAdapter()).isFiltered()) {
                    ((MyAdapter) mRecyclerView.getAdapter()).setFilterFavorite();
                } else {
                    ((MyAdapter) mRecyclerView.getAdapter()).flushFilter();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.base_menu, menu);
        // SearchView
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            searchView.setQueryHint("Search here");
            searchView.setOnQueryTextListener(this);
            searchView.setMaxWidth(4000);
            searchView.setIconifiedByDefault(true);
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    ((MyAdapter) mRecyclerView.getAdapter()).flushFilter();
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    private ArrayList<ArrayList<String>> populateDataset() {
        DataUtil mData = new DataUtil();
        ArrayList<String> titles = mData.titles;
        ArrayList<String> bodies=mData.bodies(this);

        int rows = ((titles.size()>=bodies.size()) ? titles.size() : bodies.size());

        if (titles.size()!=bodies.size()) {
            Log.w(MainActivity.class.toString(),"There are not equal amounts of titles & text bodies!");
        }

        ArrayList<ArrayList<String>> myDataset = new ArrayList<>();//new String[rows][2];

        for (int i=0;i<rows;i++) {
            ArrayList<String> myDatasetItems = new ArrayList<>();
            //Iterate through first column
            try {
                myDatasetItems.add(titles.get(i));
            } catch (ArrayIndexOutOfBoundsException e) {
                myDatasetItems.add("");
            }
            //Iterate through second column
            try {
                myDatasetItems.add(bodies.get(i));
            } catch (ArrayIndexOutOfBoundsException e) {
                myDatasetItems.add("");
            }
            myDataset.add(myDatasetItems);
        }

        /*for (int i=0;i<myDataset.size();i++){
            //Iterate through first column
            try {
                myDataset[i][0] = titles[i];
            } catch (ArrayIndexOutOfBoundsException e) {
                myDataset[i][0] = "";
            }
            for (int j=1;j<myDataset[i].length;j++){
                //Iterate through second column
                try {
                    myDataset[i][j] = bodies[i];
                } catch (ArrayIndexOutOfBoundsException e){
                    myDataset[i][j] = "";
                }
            }
        }*/

        return myDataset;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Log.d(TAG, s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        Log.d(TAG, s);
        if(s.length()>0) {
            ((MyAdapter) mRecyclerView.getAdapter()).setSearch(s);
        } else {
            ((MyAdapter) mRecyclerView.getAdapter()).flushFilter();
        }
        return false;
    }
}
