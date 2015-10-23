package com.cwlarson.deviceid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cwlarson.deviceid.data.Permissions;
import com.cwlarson.deviceid.util.TabsViewPagerAdapter;


public class MainActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private final String TAG = "MainActivity";
    public static AlertDialog dialog;
    private static ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new TabsViewPagerAdapter(getSupportFragmentManager(), MainActivity.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(this,SearchActivity.class);
                startActivity(intent);
                return true;
            /*case R.id.action_filter:
                if (!((MyAdapter) mRecyclerView.getAdapter()).isFiltered()) {
                    ((MyAdapter) mRecyclerView.getAdapter()).setFilterFavorite();
                } else {
                    ((MyAdapter) mRecyclerView.getAdapter()).flushFilter();
                }
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setUpToolbar();
    }

    private void setUpToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.myToolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevents memory leaks of dialogs
        if(dialog!=null) dialog.dismiss();
    }

    // Request permission for IMEI/MEID for Android M+
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        new Permissions(this).onRequestPermissionsResult(requestCode, grantResults,(TabsViewPagerAdapter) mViewPager.getAdapter());
    }
}
