package com.harjeet.missedcallduration;

/**
 * Created by HARJEET on 05-May-17.
 */

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.LinkedList;
import java.util.List;

import static com.harjeet.missedcallduration.MainActivity.CLEAR_LIST;
import static com.harjeet.missedcallduration.MainActivity.MISSED_CALL;
import static com.harjeet.missedcallduration.MainActivity.PREFS_NAME;
import static com.harjeet.missedcallduration.MainActivity.REFRESH_LIST;
import static com.harjeet.missedcallduration.SettingsFragment.DEFAULT_LOG_LENGTH;

public class LogsFragment extends ListFragment {

    private LogsFragmentCallback callback;

    private ListAdapter myAdapter;

    public List<MissedData> values;

    public static List<MissedData> newListData = new LinkedList<MissedData>();

    public static boolean databaseChanged = false;

    private long lastClicked = 0;

    MediaPlayer mPlayerCall, mPlayerDelete;

    public interface LogsFragmentCallback{
        List<MissedData> getListData(int length);
        void deleteData(long id);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_log, container, false);


        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        callback = (LogsFragmentCallback) context;
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if(newListData != null)
            newListData.clear();

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int logLength = sharedPreferences.getInt("KEY_LOGLENGTH", DEFAULT_LOG_LENGTH);

        values = callback.getListData(logLength);
        myAdapter = new CustomAdapter(getContext(), values);
        setListAdapter(myAdapter);

        databaseChanged = false;

        final ListView lv = (ListView) getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {

                MissedData item = (MissedData) lv.getItemAtPosition(position);
                final String number = item.getNumber();
                final long id = item.getId();
                final int pos = position;
                TextView tv = (TextView) view.findViewById(R.id.textTitle);
                String name = tv.getText().toString();

                final Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_dialog);
//                dialog.setTitle("Pick a choice");
                dialog.setCancelable(true);

                TextView dialogText = (TextView) dialog.findViewById(R.id.dialogText);
                dialogText.setText(name);


                Button callButton = (Button) dialog.findViewById(R.id.buttonCall);
                callButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPlayerCall = MediaPlayer.create(getContext(), R.raw.ohyeah);
                        if(mPlayerCall!=null){
//                            mPlayerCall.start();      commented to adhere to child policy
                        }
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + number));
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

                Button deleteButton = (Button) dialog.findViewById(R.id.buttonDelete);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPlayerDelete = MediaPlayer.create(getContext(), R.raw.ohno);
                        if(mPlayerDelete!=null){
//                            mPlayerDelete.start();    commented to adhere to child policy
                        }
                        dialog.dismiss();
                        deleteRow(pos);
                        callback.deleteData(id);
                    }
                });

                if((System.currentTimeMillis() - lastClicked) > 500) {
                    lastClicked = System.currentTimeMillis();
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
            }
        });

    }

    BroadcastReceiver refreshListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshList();
        }
    };

    BroadcastReceiver clearListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            clearList();
        }
    };

    BroadcastReceiver missedCallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            long id = 0;
            String number = "";
            String start = "";
            String duration = "";

            if (intent.hasExtra(CallReceiver.ID)) {
                id = intent.getLongExtra(CallReceiver.ID, 100);
            }
            if (intent.hasExtra(CallReceiver.NUMBER)) {
                number = intent.getStringExtra(CallReceiver.NUMBER);
            }
            if (intent.hasExtra(CallReceiver.START)) {
                start = intent.getStringExtra(CallReceiver.START);
            }
            if (intent.hasExtra(CallReceiver.DURATION)) {
                duration = intent.getStringExtra(CallReceiver.DURATION);
            }
            MissedData data = new MissedData(id, number, start, Double.parseDouble(duration));
            addNewRow(data);
            ((CustomAdapter) getListAdapter()).notifyDataSetChanged();
            getListView().smoothScrollToPosition(0);
        }
    };

    public void addNewRow(final MissedData data) {

        if (getListAdapter() == null) {
        } else {
            values.add(0, data);
            databaseChanged = false;

            SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int logLength = sharedPreferences.getInt("KEY_LOGLENGTH", DEFAULT_LOG_LENGTH);
            if(values.size() > logLength){
                values.remove(logLength);
            }
        }

    }

    public void deleteRow(int pos) {
        if (getListAdapter() == null) {
        } else {
            values.remove(pos);
            ((CustomAdapter) getListAdapter()).notifyDataSetChanged();
        }

    }

    public void refreshList(){
        values.clear();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int logLength = sharedPreferences.getInt("KEY_LOGLENGTH", DEFAULT_LOG_LENGTH);
        values = callback.getListData(logLength);
        myAdapter = new CustomAdapter(getContext(), values);
        setListAdapter(myAdapter);
        ((CustomAdapter) getListAdapter()).notifyDataSetChanged();

    }

    public void clearList(){
        values.clear();
        ((CustomAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onResume() {

        if (databaseChanged == true) {

            for (int i = 0; i < newListData.size(); i++) {
                addNewRow(newListData.get(i));
            }
//            ((CustomAdapter) getListAdapter()).notifyDataSetChanged();
            getListView().smoothScrollToPosition(0);
            databaseChanged = false;
            if (newListData != null)
                newListData.clear();
        }
        ((CustomAdapter) getListAdapter()).notifyDataSetChanged();

        IntentFilter filter = new IntentFilter(MISSED_CALL);
        filter.setPriority(999);
//        LocalBroadcastManager.getInstance(getContext()).registerReceiver(missedCallReceiver, filter);
        getContext().registerReceiver(missedCallReceiver, filter);

        IntentFilter clearFilter = new IntentFilter(CLEAR_LIST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(clearListReceiver, clearFilter);
//        getContext().registerReceiver(clearListReceiver, clearFilter);

        IntentFilter refreshFilter = new IntentFilter(REFRESH_LIST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(refreshListReceiver, refreshFilter);

        super.onResume();
    }

    @Override
    public void onPause() {

//        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(missedCallReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(clearListReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshListReceiver);

        getContext().unregisterReceiver(missedCallReceiver);
//        getContext().unregisterReceiver(clearListReceiver);
        super.onPause();
    }
}
