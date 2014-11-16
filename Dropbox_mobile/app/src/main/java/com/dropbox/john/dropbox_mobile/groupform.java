package com.dropbox.john.dropbox_mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.content.*;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;


public class groupform extends Activity implements OnClickListener {

    Button enter_button,make_button,delete_button,invite_button,exit_button,refresh_button;
    ArrayList<String> list;
    ArrayAdapter<String> adapter;
    ListView grouplist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.groupform);


        enter_button = (Button) findViewById(R.id.enter_button);
        enter_button.setOnClickListener(this);

        refresh_button = (Button) findViewById(R.id.refresh_button);
        refresh_button.setOnClickListener(this);

        make_button = (Button) findViewById(R.id.make_button);
        make_button.setOnClickListener(this);

        invite_button = (Button) findViewById(R.id.invite_button);
        invite_button.setOnClickListener(this);

        exit_button = (Button) findViewById(R.id.exit_button);
        exit_button.setOnClickListener(this);

        delete_button = (Button) findViewById(R.id.delete_button);
        delete_button.setOnClickListener(this);


        refreshList(100);
}

    public void refreshList(int i) {

        ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();
        grouplist = (ListView) findViewById(R.id.grouplist);

        String[] from = new String[] {"name","date"};
        int[] to = new int[]{R.id.name_text,R.id.date_text};

        for(;i<200;i++) {
            HashMap<String, String> item =   new HashMap<String, String>();
            item.put("name", "aaa");
            item.put("date",Integer.toString(i));
            list.add(item);
        }


        SimpleAdapter notes = new SimpleAdapter(this,list, R.layout.grouplist, from,to);
        grouplist.setAdapter(notes);
        grouplist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        grouplist.setOnItemClickListener( new ListViewItemClickListener() );
    }

    private class ListViewItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Toast toast = Toast.makeText(groupform.this,((TextView) view.findViewById(R.id.date_text)).getText().toString(),Toast.LENGTH_SHORT);
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
            case R.id.enter_button:
                Intent intent = new Intent(this, filelistform.class);
                startActivity(intent);
                break;
            case R.id.make_button:

                AlertDialog.Builder alert = new AlertDialog.Builder(groupform.this);
                alert.setTitle("Input Group Name");
                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                alert.setView(input);

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        value.toString();
                        // Do something with value!
                        group_management mkgroup = new group_management();
                        mkgroup.make_group(value.toString());
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
            case R.id.invite_button:


                break;

            case R.id.refresh_button:


                break;
            case R.id.exit_button:
                AlertDialog.Builder alert3 = new AlertDialog.Builder(groupform.this);
                alert3.setTitle("Are you sure you want to exit?");
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
            case R.id.delete_button:
                AlertDialog.Builder alert2 = new AlertDialog.Builder(groupform.this);
                alert2.setTitle("Are you sure you want to delete?");
                // Set an EditText view to get user input


                alert2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {


                        // Do something with value!

                    }
                });
                alert2.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        });
                alert2.show();

                break;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }




}
