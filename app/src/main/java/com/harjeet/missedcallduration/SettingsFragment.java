package com.harjeet.missedcallduration;

/**
 * Created by HARJEET on 05-May-17.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.harjeet.missedcallduration.MainActivity.CLEAR_LIST;
import static com.harjeet.missedcallduration.MainActivity.PREFS_NAME;
import static com.harjeet.missedcallduration.MainActivity.REFRESH_LIST;


public class SettingsFragment extends ListFragment {

    public interface SettingsFragmentCallback{
        void deleteAllData();
    }

    private SettingsFragmentCallback callback;

    private boolean notificationRequired = false;

    private long logLastClicked = 0;

    private long clearLastClicked = 0;

    public static final int DEFAULT_LOG_LENGTH = 20;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);


        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        callback = (SettingsFragmentCallback) context;
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String[] values = new String[]{"Display Notification", "Log Length", "Clear List"};
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        notificationRequired = sharedPreferences.getBoolean("KEY_NOTIFICATION", false);

        CustomSettingsAdapter myAdapter = new CustomSettingsAdapter(getActivity(), list, notificationRequired);
        setListAdapter(myAdapter);


        final ListView lv = (ListView) getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch(values[position]){
                    case "Display Notification" :

                        CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkBox);

                        if (checkbox.isChecked()) {
                            checkbox.setChecked(false);
                            notificationRequired = false;
                        } else {
                            checkbox.setChecked(true);
                            notificationRequired = true;
                        }

                        break;
                    case "Log Length" :

                        final String logList[] = {"20", "50", "100", "500"};

                        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        final int logLength = sharedPreferences.getInt("KEY_LOGLENGTH",DEFAULT_LOG_LENGTH);


                        final Dialog dialog = new Dialog(getContext());
                        View myView = getActivity().getLayoutInflater().inflate(R.layout.loglength_dialog, null);
                        ListView lv = (ListView) myView.findViewById(R.id.loglength_list);
                        LogLengthCustomAdapter myAdapter = new LogLengthCustomAdapter(getActivity(), logList, logLength);
                        lv.setAdapter(myAdapter);

                        int index = -1;

                        for (int i=0;i<logList.length;i++) {
                            if (logLength == Integer.parseInt(logList[i])) {
                                index = i;
                                break;
                            }
                        }
                        // Set Selection of log length
                        lv.setSelection(index);


                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("KEY_LOGLENGTH", Integer.parseInt(logList[position]));
                                editor.apply();
                                Intent refreshIntent = new Intent(REFRESH_LIST);
                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(refreshIntent);
                                dialog.dismiss();
                            }
                        });

                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(myView);
                        if((System.currentTimeMillis() - logLastClicked) > 500) {
                            logLastClicked = System.currentTimeMillis();
                            dialog.show();
                        }


                        break;
                    case "Clear List" :

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

//                        builder.setTitle("Confirm");
                        builder.setMessage("Do you want to clear log history?");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {

                                Intent clearIntent = new Intent(CLEAR_LIST);
                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(clearIntent);
                                callback.deleteAllData();

                                //Clear shared prefs of call logs for Android 9 and above
                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("call_number", 0);
                                editor.apply();

                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();

                        if((System.currentTimeMillis() - clearLastClicked) > 500) {
                            clearLastClicked = System.currentTimeMillis();
                            alert.show();

                            Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                            Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                            pbutton.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                            nbutton.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        }


                        break;
                    default :
                        break;
                }

            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("KEY_NOTIFICATION", notificationRequired);
        editor.apply();
    }
}

class CustomSettingsAdapter extends ArrayAdapter<String>
{
    ArrayList<String> myList;
    boolean checkBoxState;
    ViewHolder viewHolder;


    public CustomSettingsAdapter(Context context, ArrayList<String> myList, boolean notificationRequired) {

        super(context, R.layout.settings_row, myList);
        this.myList = myList;
        checkBoxState = notificationRequired;
    }


    //class for caching the views in a row
    private class ViewHolder
    {
        TextView name;
        CheckBox checkBox;
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());

        if(convertView==null) {
            convertView = inflater.inflate(R.layout.settings_row, null);
            viewHolder = new ViewHolder();

            //cache the views
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

            //link the cached views to the convertview
            convertView.setTag(viewHolder);


        }
        else
            viewHolder=(ViewHolder) convertView.getTag();

        //set the data to be displayed
        viewHolder.name.setText(myList.get(position));

        //VITAL PART!!! Set the state of the
        //CheckBox using the boolean array
        viewHolder.checkBox.setChecked(checkBoxState);

        if(myList.get(position).equals("Log Length") || myList.get(position).equals("Clear List")){
            viewHolder.checkBox.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

}


class LogLengthCustomAdapter extends BaseAdapter
{
    private String[] data;
    private Context context;
    int logLength;


    public LogLengthCustomAdapter(Context context, String[] data, int length) {
        super();
        this.data = data;
        this.context = context;
        this.logLength = length;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView = LayoutInflater.from(context).
                inflate(R.layout.loglength_dialog_row, parent, false);

        TextView text1 = (TextView) rowView.findViewById(R.id.textLogLength);
        text1.setText(data[position]);


        if (String.valueOf(logLength).equals(data[position])) {
            text1.setBackgroundColor(ContextCompat.getColor(context, R.color.list_item_grey));
        }

        return rowView;
    }

}


/*class LogLengthCustomAdapter extends ArrayAdapter {

    Context context;
    int logLength;


    public LogLengthCustomAdapter(Context context, String[] list, int length) {
        super(context, R.layout.loglength_dialog, list);
        this.context = context;
        this.logLength = length;
    }

    static class ViewHolder {
        private TextView length;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LogLengthCustomAdapter.ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.loglength_dialog_row, null);
            holder = new com.harjeet.missedcallduration.LogLengthCustomAdapter.ViewHolder();
            holder.length = (TextView) convertView.findViewById(R.id.textLogLength);
            convertView.setTag(holder);
        } else {
            holder = (com.harjeet.missedcallduration.LogLengthCustomAdapter.ViewHolder) convertView.getTag();
        }

        String item = (String) getItem(position);
        holder.length.setText(item);

        // highlight the previously selected log length
        if (String.valueOf(logLength).equals(item)) {
            holder.length.setBackgroundColor(ContextCompat.getColor(context, R.color.list_item_grey));
        }
        return convertView;

    }
}*/
