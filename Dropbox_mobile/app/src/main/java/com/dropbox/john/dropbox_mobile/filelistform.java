package com.dropbox.john.dropbox_mobile;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.*;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

import android.widget.*;


public class filelistform extends Activity implements OnClickListener {
    ArrayList<String> list;
    ArrayAdapter<String> adapter;
    ListView filelist;

    Button download_button, upload_button, rename_button, delete_button,refresh_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.filelistform);


        download_button = (Button) findViewById(R.id.download_button);
        download_button.setOnClickListener(this);

        upload_button = (Button) findViewById(R.id.upload_button);
        upload_button.setOnClickListener(this);

        rename_button = (Button) findViewById(R.id.rename_button);
        rename_button.setOnClickListener(this);

        delete_button = (Button) findViewById(R.id.delete_button);
        delete_button.setOnClickListener(this);

        refresh_button = (Button) findViewById(R.id.refresh_button);
        refresh_button.setOnClickListener(this);



        refreshList(1000);
    }
    public void refreshList(int i) {

        ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();
        filelist = (ListView) findViewById(R.id.filelist);

        String[] from = new String[] {"name","date"};
        int[] to = new int[]{R.id.name_text,R.id.date_text};

        for(;i<1100;i++) {
            HashMap<String, String> item =   new HashMap<String, String>();
            item.put("name", "aaa");
            item.put("date",Integer.toString(i));
            list.add(item);
        }


        SimpleAdapter notes = new SimpleAdapter(this,list, R.layout.filelist, from,to);
        filelist.setAdapter(notes);
        filelist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        filelist.setOnItemClickListener( new ListViewItemClickListener() );

    }

    private class ListViewItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Toast toast = Toast.makeText(filelistform.this,((TextView) view.findViewById(R.id.date_text)).getText().toString(),Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int getId = v.getId();
        switch (getId) {
            case R.id.download_button:

                break;
            case R.id.upload_button:


                break;
            case R.id.delete_button:
                AlertDialog.Builder alert3 = new AlertDialog.Builder(filelistform.this);
                alert3.setTitle("Are you sure you want to delete?");
                // Set an EditText view to get user input


                alert3.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        // Do something with value!

                    }
                });
                alert3.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        });
                alert3.show();

                break;

            case R.id.rename_button:
                AlertDialog.Builder alert = new AlertDialog.Builder(filelistform.this);
                alert.setTitle("Input File Name");
                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                alert.setView(input);

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        value.toString();
                        // Do something with value!

                    }
                });
                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        });
                alert.show();
                break;

            case R.id.refresh_button:
                refreshList(1040);

                break;

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }




}
