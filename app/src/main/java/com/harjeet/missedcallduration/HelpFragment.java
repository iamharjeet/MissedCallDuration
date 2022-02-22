package com.harjeet.missedcallduration;

/**
 * Created by HARJEET on 05-May-17.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class HelpFragment extends ListFragment {


    private long aboutClicked = 0;

    private long shareClicked = 0;

    private long supportClicked = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String[] values = new String[]{"About", "Share", "Rate This App", "Support"};
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }

        MyAdapter myAdapter = new MyAdapter(getActivity(), values);

        setListAdapter(myAdapter);

        final ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {

                switch(values[position]){
                    case "About" :

                        Intent intent = new Intent(getContext(), AboutActivity.class);
                        if((System.currentTimeMillis() - aboutClicked) > 500) {
                            aboutClicked = System.currentTimeMillis();
                            startActivity(intent);
                        }


                        break;
                    case "Share" :
                        try {
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("text/plain");
                            i.putExtra(Intent.EXTRA_SUBJECT, "Missed Call Duration");
                            String sAux = "\nHey! Checkout this awesome application!\n\n";
                            sAux = sAux + "https://play.google.com/store/apps/details?id=com.harjeet.missedcallduration";
                            i.putExtra(Intent.EXTRA_TEXT, sAux);

                            if((System.currentTimeMillis() - shareClicked) > 800) {
                                shareClicked = System.currentTimeMillis();
                                startActivity(Intent.createChooser(i, "Share"));
                            }
                        } catch(Exception e) {
                            e.toString();
                        }

                        break;
                    case "Rate This App" :
                        try {
                            Intent viewIntent =
                                    new Intent("android.intent.action.VIEW",
                                            Uri.parse("https://play.google.com/store/apps/details?id=com.harjeet.missedcallduration"));
                            startActivity(viewIntent);
                        }catch(Exception e) {
                            Toast.makeText(getContext(),"Unable to Connect. Try Again...",
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        break;
                    case "Support" :
                        final Dialog supportDialog = new Dialog(getContext());
                        supportDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        supportDialog.setContentView(R.layout.support_dialog);
                        supportDialog.setCancelable(true);

                        Button supportButtonNo = (Button) supportDialog.findViewById(R.id.supportDialogNo);
                        supportButtonNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                supportDialog.dismiss();
                            }
                        });
                        Button supportButtonYes = (Button) supportDialog.findViewById(R.id.supportDialogYes);
                        supportButtonYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                supportDialog.dismiss();
                                Intent viewIntent =
                                        new Intent("android.intent.action.VIEW",
                                                Uri.parse("https://play.google.com/store/apps/developer?id=Jeet+Inc.&hl=en"));
                                startActivity(viewIntent);
                            }
                        });

                        if((System.currentTimeMillis() - supportClicked) > 500) {
                            supportClicked = System.currentTimeMillis();
                            supportDialog.show();
                            supportDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
    }

}

class MyAdapter extends BaseAdapter
{
    private String[] data;
    private Context context;


    public MyAdapter(Context context, String[] data) {
        super();
        this.data = data;
        this.context = context;
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
                inflate(R.layout.help_row, parent, false);

        TextView text1 = (TextView) rowView.findViewById(R.id.help_row_text);
        text1.setText(data[position]);

        return rowView;
    }

}
