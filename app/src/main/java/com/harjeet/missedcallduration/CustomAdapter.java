package com.harjeet.missedcallduration;

/**
 * Created by HARJEET on 05-May-17.
 */

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class CustomAdapter extends ArrayAdapter {

    Context context;
    String name;
    boolean contactPermission = false;


    public CustomAdapter(Context context, List<MissedData> data) {
        super(context, R.layout.custom_row, data);
        this.context = context;

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            contactPermission = false;
        }else {
            contactPermission = true;
        }


    }

    static class ViewHolder {
        private TextView title;
        //        private TextView name;
        private TextView startTime;
        private TextView startDate;
        private TextView duration;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_row, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.textTitle);
//            holder.name = (TextView) convertView.findViewById(R.id.textName);
            holder.startTime = (TextView) convertView.findViewById(R.id.textTime);
            holder.startDate = (TextView) convertView.findViewById(R.id.textDate);
            holder.duration = (TextView) convertView.findViewById(R.id.textDuration);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MissedData item = (MissedData) getItem(position);
//        holder.title.setText(item.getNumber());
        if(contactPermission == true){
            name = getContactName(context, item.getNumber());
        } else{
            name = "";
        }

        if (name == null || name.equals("")) {
            holder.title.setText(item.getNumber());
        } else {
            holder.title.setText(name);
        }

        String string = item.getStart();
        String[] parts = string.split("=");
        String date = parts[0];
        String time = parts[1];

        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
        Date myDate  = null;
        try {
            myDate = parseFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String time24 = displayFormat.format(myDate);

        if(DateFormat.is24HourFormat(context)){
            time = time24;
        }

        holder.startTime.setText(time);
        holder.startDate.setText(date);
        holder.duration.setText(String.valueOf(item.getDuration()) + " Seconds");

        return convertView;

    }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }
}

