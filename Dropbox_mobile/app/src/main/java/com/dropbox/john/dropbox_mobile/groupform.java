package com.dropbox.john.dropbox_mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.content.*;
import android.widget.Button;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class groupform extends Activity implements OnClickListener {

    Button enter_button,make_button,delete_button,invite_button,exit_button,refresh_button;

    ListView grouplist;
    String group_id=null;
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

        String[] from = new String[] {"Group_ID","Group_Master"};
        int[] to = new int[]{R.id.name_text,R.id.master_text};

        group_management gm = new group_management();
        list = gm.load_list();


        SimpleAdapter notes = new SimpleAdapter(this,list, R.layout.grouplist, from,to);
        grouplist.setAdapter(notes);
        grouplist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        grouplist.setOnItemClickListener( new ListViewItemClickListener() );
        grouplist.setOnItemLongClickListener( new ListViewItemLongClickListener() );
    }

    private class ListViewItemLongClickListener implements AdapterView.OnItemLongClickListener
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            final Dialog dialog = new Dialog(groupform.this);
            dialog.setContentView(R.layout.groupinfo);
            dialog.setTitle("Group Info");

            ListView mem_list = (ListView) dialog.findViewById(R.id.member_list);

            ArrayList<String> list = new ArrayList<String>();
            group_management gm = new group_management();
            list = gm.load_member();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(groupform.this,android.R.layout.simple_list_item_1,list);

            mem_list.setAdapter(adapter);
            mem_list.setChoiceMode(mem_list.CHOICE_MODE_SINGLE);


            dialog.show();

            return false;
        }
    }
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            group_id = ((TextView) view.findViewById(R.id.name_text)).getText().toString();

        }
    }

    public void onBackPressed() {
        AlertDialog.Builder alert3 = new AlertDialog.Builder(groupform.this);
        alert3.setTitle("Are you want to logout?");
        // Set an EditText view to get user input


        alert3.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // Do something with value!
                finish();
            }
        });
        alert3.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
        alert3.show();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int getId = v.getId();
        switch (getId) {
            case R.id.enter_button:

                if(group_id!=null) {

                    Intent intent = new Intent(this, filelistform.class);
                    intent.putExtra("group_id",group_id);
                    startActivity(intent);

                }
                else {
                    Toast toast = Toast.makeText(groupform.this,"Choice Group",Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.make_button:

                final EditText input_name, input_comment;
                Button ok;
                final Dialog dialog = new Dialog(groupform.this);
                dialog.setContentView(R.layout.makegroup);


                input_name =(EditText)dialog.findViewById(R.id.input_name);
                input_comment=(EditText)dialog.findViewById(R.id.input_comment);
                input_name.setFilters(new InputFilter[] {filterAlphaNum});
                ok = (Button)dialog.findViewById(R.id.button);

                ok.setOnClickListener(new OnClickListener(){

                    public void onClick(View v){
                        String name = input_name.getText().toString();
                        String comment = input_comment.getText().toString();


                        dialog.dismiss();
                    }
                });

                dialog.show();

                break;
            case R.id.invite_button:
                if(group_id!=null) {
                    AlertDialog.Builder alert4 = new AlertDialog.Builder(groupform.this);
                    alert4.setTitle("Input User ID");
                    // Set an EditText view to get user input
                    final EditText input2 = new EditText(this);
                    alert4.setView(input2);

                    alert4.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input2.getText().toString();
                            value.toString();
                            // Do something with value!

                        }
                    });
                    alert4.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });
                    alert4.show();


                    NetworkTask myClientTask = new NetworkTask("183.96.37.226", 5555);
                    myClientTask.execute();
                }
                else {
                    Toast toast = Toast.makeText(groupform.this,"Choice Group",Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;

            case R.id.refresh_button:


                break;
            case R.id.exit_button:

                if(group_id!=null) {
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
                }
                else {
                    Toast toast = Toast.makeText(groupform.this,"Choice Group",Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.delete_button:
                if(group_id!=null) {
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
                }
                else {
                    Toast toast = Toast.makeText(groupform.this,"Choice Group",Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    protected InputFilter filterAlphaNum = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };


}
