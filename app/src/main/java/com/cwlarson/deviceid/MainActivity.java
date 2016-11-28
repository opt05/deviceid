package com.cwlarson.deviceid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.cwlarson.deviceid.data.Permissions;
import com.cwlarson.deviceid.databinding.ActivityMainBinding;
import com.cwlarson.deviceid.util.TabsViewPagerAdapter;


public class MainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = "MainActivity";
    private TabsViewPagerAdapter mAdapter;
    private ActivityMainBinding binding;
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
                TabFragment tabFragment = mAdapter.getFragment(binding.viewpager.getCurrentItem());
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
        setTheme(R.style.AppTheme); //Removes splash screen
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mAdapter = new TabsViewPagerAdapter(getSupportFragmentManager(), this);
        binding.viewpager.setAdapter(mAdapter);
        binding.viewpager.setOffscreenPageLimit(mAdapter.getCount()); //Prevent reloading of views on tab switching

        // Give the TabLayout the ViewPager
        binding.tabs.setupWithViewPager(binding.viewpager);

        setSupportActionBar(binding.myToolbar);
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
        binding.myToolbarLayout.addOnOffsetChangedListener(this);
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
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        binding.myToolbarLayout.removeOnOffsetChangedListener(this);
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
        new Permissions(this).onRequestPermissionsResult(requestCode, grantResults, mAdapter);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        index=verticalOffset;
    }
}
