package com.cwlarson.deviceid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;

import com.cwlarson.deviceid.databinding.Item;
import com.cwlarson.deviceid.dialog.ItemClickDialog;
import com.cwlarson.deviceid.util.TabsViewPagerAdapter;

public abstract class PermissionsActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    public boolean onItemClick(Item item) {
        if(item.getSubTitle().equals(getResources().getString(R.string.phone_permission_denied)))
            getPermissionClickAdapter(MY_PERMISSIONS_REQUEST_READ_PHONE_STATE, item.getTitle());
        else if(item.getSubTitle().equals(getResources().getString(R.string.not_found))
                || item.getSubTitle().startsWith(getResources().getString(R.string.no_longer_possible).replace("%s", ""))
                || item.getSubTitle().startsWith(getResources().getString(R.string.not_possible_yet).replace("%s", "")))
            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.snackbar_not_found_adapter, item.getTitle()), Snackbar.LENGTH_LONG).show();
        else  // This is a valid body so we should do something and inform the user
            ItemClickDialog.newInstance(item).show(getSupportFragmentManager(),"itemClickDialog");
        return true;
    }

    // Request permission for IMEI/MEID for Android M+
    public void getPermissionClickAdapter(final int MY_PERMISSION, String itemTitle){
        final String permission;
        switch (MY_PERMISSION){
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                permission = Manifest.permission.READ_PHONE_STATE;
                break;
            default:
                return;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
                if(findViewById(android.R.id.content)!=null) {
                    final PermissionsActivity act = this;
                    Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.phone_permission_snackbar, itemTitle), Snackbar.LENGTH_INDEFINITE).setAction(R.string.phone_permission_snackbar_button, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(act, new String[]{permission}, MY_PERMISSION);
                        }
                    }).show();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, MY_PERMISSION);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults, TabsViewPagerAdapter mAdapter) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mAdapter.notifyDataSetChanged(); // GRANTED: Refresh View
                else
                    break; // DENIED: We do nothing (it is handled by the ViewAdapter)
            }
        }
    }
}
