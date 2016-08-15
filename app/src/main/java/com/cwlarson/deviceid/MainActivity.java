package com.cwlarson.deviceid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.cwlarson.deviceid.data.Permissions;
import com.cwlarson.deviceid.util.TabsViewPagerAdapter;


public class MainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = "MainActivity";
    private AppBarLayout appBarLayout;
    private static ViewPager mViewPager;
    private static TabsViewPagerAdapter mAdapter;
    private int index = 0;
    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        switch(action){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                TabFragment tabFragment = mAdapter.getFragment(mViewPager.getCurrentItem());
                if(tabFragment!=null) {
                    if(index==0)
                        tabFragment.setSwipeToRefreshEnabled(true);
                    else
                        tabFragment.setSwipeToRefreshEnabled(false);
                }
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new TabsViewPagerAdapter(getSupportFragmentManager(), MainActivity.this);
        mViewPager.setAdapter(mAdapter);
        appBarLayout = (AppBarLayout) findViewById(R.id.myToolbarLayout);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if(tabLayout!=null) tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_menu, menu);
        // Get checkable menu item value
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        menu.findItem(R.id.action_hide_unables).setChecked(sharedPreferences.getBoolean("hide_unables",false));
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        appBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(this,SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_hide_unables:
                item.setChecked(!item.isChecked());
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("hide_unables",item.isChecked());
                editor.apply();
                // Refresh tabs due to data added/removed
                mViewPager.getAdapter().notifyDataSetChanged();
                return true;
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
    protected void onStop() {
        appBarLayout.removeOnOffsetChangedListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mAdapter.destroy();
        super.onDestroy();
    }

    // Request permission for IMEI/MEID for Android M+
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        new Permissions(this).onRequestPermissionsResult(requestCode, grantResults,(TabsViewPagerAdapter) mViewPager.getAdapter());
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        index=verticalOffset;
    }
}
