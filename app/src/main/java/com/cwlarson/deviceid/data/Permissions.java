package com.cwlarson.deviceid.data;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.cwlarson.deviceid.R;
import com.cwlarson.deviceid.util.TabsViewPagerAdapter;

public class Permissions {
    public static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    private final Activity activity;
    private final Context context;

    public Permissions(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    @SuppressWarnings("SameParameterValue")
    public Boolean hasPermission(int MY_PERMISSION){
        switch (MY_PERMISSION){
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            default:
                return false;
        }
    }

    @SuppressWarnings("SameParameterValue")
    public void getPermissionClickAdapter(final int MY_PERMISSION, String itemTitle){
        final String permission;
        switch (MY_PERMISSION){
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                permission = Manifest.permission.READ_PHONE_STATE;
                break;
            default:
                return;
        }

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
                View view = activity.findViewById(R.id.main_activity_layout);
                if(view !=null) {
                    Snackbar.make(view, context.getResources().getString(R.string.phone_permission_snackbar, itemTitle), Snackbar.LENGTH_INDEFINITE).setAction(R.string.phone_permission_snackbar_button, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(activity, new String[]{permission}, MY_PERMISSION);
                        }
                    }).show();
                }
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, MY_PERMISSION);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults, TabsViewPagerAdapter mAdapter) {
        switch (requestCode) {
            case Permissions.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mAdapter.notifyDataSetChanged(); // GRANTED: Refresh View
                else
                    break; // DENIED: We do nothing (it is handled by the ViewAdapter)
            }
        }
    }

}
