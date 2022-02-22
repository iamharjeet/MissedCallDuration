package com.harjeet.missedcallduration;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.List;

import static com.harjeet.missedcallduration.CallReceiver.NOTIFICATION_ID;
import static com.harjeet.missedcallduration.CallReceiver.setBadge;

public class MainActivity extends AppCompatActivity implements LogsFragment.LogsFragmentCallback, SettingsFragment.SettingsFragmentCallback{

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    public static final int PERMISSIONS_MULTIPLE_REQUEST = 123;

    public static final String PREFS_NAME  = "missedcallprefs";

    public static final String MISSED_CALL = "com.harjeet.missedcallduration.custom.intent.action.MISSEDCALL";

    public static final String CLEAR_LIST = "com.harjeet.missedcallduration.custom.intent.action.CLEARLIST";

    public static final String REFRESH_LIST = "com.harjeet.missedcallduration.custom.intent.action.REFRESHLIST";

    private MissedCallDataSource datasource;

    public List<MissedData> values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        datasource = new MissedCallDataSource(this);
        datasource.open();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }


    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALL_LOG) + ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_PHONE_STATE) + ContextCompat
                .checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.READ_PHONE_STATE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.READ_CALL_LOG) ) {

                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("permission_denied", true);
                editor.apply();

                Snackbar.make(findViewById(android.R.id.content),
                        getResources().getString(R.string.request_permissions),
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS},
                                        PERMISSIONS_MULTIPLE_REQUEST);
                            }
                        }).setActionTextColor(ContextCompat.getColor(this, R.color.colorPrimary)).show();
            } else {

                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                boolean permission_denied = sharedPreferences.getBoolean("permission_denied", false);

                if (!permission_denied) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS},
                            PERMISSIONS_MULTIPLE_REQUEST);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getResources().getString(R.string.request_permissions_message));
                    builder.setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            goToSettings();
                        }
                    });

                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                    Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                    Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                    pbutton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    nbutton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                }
            }
        } else {
            // write your logic code if permission already granted
        }
    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myAppSettings);
    }


    @Override
    protected void onResume() {
        super.onResume();
        datasource.open();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.cancel(NOTIFICATION_ID); // Clears Status Bar Notification
        setBadge(this, 0); // Clears Badge

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("active", true);
        editor.putInt("notificationCount",0);
        editor.apply();

    }

    @Override
    protected void onPause() {
        super.onPause();
        datasource.close();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("active", false);
        editor.apply();
    }



    @Override
    public List<MissedData> getListData(int logLength) {

        values = datasource.getAllData(logLength);

        return values;

    }

    public void deleteData(long id){
        datasource.deleteData(id);
    }

    public void deleteAllData(){
        datasource.deleteAllData();
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        Fragment lf, sf, hf;    // logs fragment, settings fragment, help fragment

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    if(lf == null)
                        lf = new LogsFragment();
                    return lf;
                case 1:
                    if(sf == null)
                        sf = new SettingsFragment();
                    return sf;
                case 2:
                    if(hf == null)
                        hf = new HelpFragment();
                    return hf;
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Logs";
                case 1:
                    return "Settings";
                case 2:
                    return "Help";
            }
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {

                    boolean phonePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean contactsPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (phonePermission && contactsPermission) {
                        Toast.makeText(this, "All Permissions Granted!", Toast.LENGTH_SHORT).show();
                    } else {
                    }
                }
                break;
        }
    }

}
