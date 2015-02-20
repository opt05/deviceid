package com.cwlarson.deviceid;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.cwlarson.deviceid.ui.DividerItemDecoration;
import com.cwlarson.deviceid.util.DataUtil;
import com.cwlarson.deviceid.util.MyAdapter;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

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

    private String[][] populateDataset() {
        DataUtil mData = new DataUtil();
        String[] titles = mData.titles;
        String[] bodies=mData.bodies(this);

        int rows = ((titles.length>=bodies.length) ? titles.length : bodies.length);

        if (titles.length!=bodies.length) {
            Log.w(MainActivity.class.toString(),"There are not equal amounts of titles & text bodies!");
        }

        String[][] myDataset = new String[rows][2];

        for (int i=0;i<myDataset.length;i++){
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
        }

        return myDataset;
    }
}
