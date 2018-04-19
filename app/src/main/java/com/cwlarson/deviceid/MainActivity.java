package com.cwlarson.deviceid;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.cwlarson.deviceid.database.AppDatabase;
import com.cwlarson.deviceid.databinding.ActivityMainBinding;
import com.cwlarson.deviceid.util.TabsViewPagerAdapter;


public class MainActivity extends PermissionsActivity {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ACTION_SEARCH = "com.cwlarson.deviceid.SEARCH";
    private static final String SEARCH_KEY = "SEARCH_KEY";
    private static final String SEARCH_FOCUS_KEY = "SEARCH_FOCUS_KEY";
    private TabsViewPagerAdapter mAdapter;
    private MenuItem mSearchViewMenuItem;
    private SearchView mSearchView;
    private boolean launchSearch;
    private String mSearchString;
    private boolean mSearchFocus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_MainTheme); //Removes splash screen
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.myToolbar);

        if (savedInstanceState != null) {
          mSearchString = savedInstanceState.getString(SEARCH_KEY);
          mSearchFocus = savedInstanceState.getBoolean(SEARCH_FOCUS_KEY);
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mAdapter = new TabsViewPagerAdapter(getSupportFragmentManager(), this);
        binding.viewpager.setAdapter(mAdapter);
        binding.viewpager.setOffscreenPageLimit(mAdapter.getCount()); //Prevent reloading of views on tab switching
        // Give the TabLayout the ViewPager
        binding.tabs.setupWithViewPager(binding.viewpager);
        handleIntent(getIntent());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mSearchView!=null) {
            mSearchString = mSearchView.getQuery().toString();
            mSearchFocus = mSearchView.hasFocus();
            outState.putString(SEARCH_KEY, mSearchString);
            outState.putBoolean(SEARCH_FOCUS_KEY, mSearchFocus);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if(intent!=null && ACTION_SEARCH.equals(intent.getAction())) {
            //Shortcut launched for search
            if(mSearchViewMenuItem==null)
                launchSearch = true;
            else {
                mSearchViewMenuItem.expandActionView();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_menu, menu);
        inflater.inflate(R.menu.search_menu, menu);
        // Get checkable menu item value
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        menu.findItem(R.id.action_hide_unables).setChecked(sharedPreferences.getBoolean("hide_unables",false));

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchViewMenuItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) mSearchViewMenuItem.getActionView();
        if(searchManager!=null)
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        if(launchSearch) {
            mSearchViewMenuItem.expandActionView();
            launchSearch = false;
        }
        if(!TextUtils.isEmpty(mSearchString)) {
            mSearchViewMenuItem.expandActionView();
            mSearchView.setQuery(mSearchString, false);
            if(!mSearchFocus) mSearchView.clearFocus();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
    protected void onDestroy() {
        mAdapter.destroy();
        AppDatabase.destroyInstance();
        super.onDestroy();
    }
}
