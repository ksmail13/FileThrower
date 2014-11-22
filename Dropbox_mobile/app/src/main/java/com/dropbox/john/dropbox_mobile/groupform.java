package com.dropbox.john.Dropbox_Mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.content.*;
import android.widget.Button;

import com.dropbox.john.Dropbox_Mobile.Artifacts.MyFtpClient;
import com.dropbox.john.Dropbox_Mobile.Artifacts.MySocket;
import com.dropbox.john.Dropbox_Mobile.Artifacts.NetworkTask;

import com.dropbox.john.Dropbox_Mobile.Artifacts.group_management;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class groupform extends Activity implements OnClickListener {

    Button enter_button, make_button, delete_button, invite_button, exit_button, refresh_button;

    ListView grouplist;
    String group_id = null;
    HashMap<String,String> comment = new HashMap<String, String>();
    boolean logout=false;
    String user_id;
    group_management gm = new group_management();

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

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");





        refreshList();
    }

    public void refreshList() {

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        grouplist = (ListView) findViewById(R.id.grouplist);

        String[] from = new String[]{"Group_ID", "Group_Master"};
        int[] to = new int[]{R.id.name_text, R.id.master_text};


        list = gm.load_list();

        for(int i=0;i<list.size();i++) {

            comment.put(list.get(i).get("Group_ID"), list.get(i).get("Comment"));
        }

        SimpleAdapter notes = new SimpleAdapter(this, list, R.layout.grouplist, from, to);
        grouplist.setAdapter(notes);
        grouplist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        grouplist.setOnItemClickListener(new ListViewItemClickListener());
        grouplist.setOnItemLongClickListener(new ListViewItemLongClickListener());
        group_id=null;
    }

    private class ListViewItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            group_id = ((TextView) view.findViewById(R.id.name_text)).getText().toString();
            final Dialog dialog = new Dialog(groupform.this);
            dialog.setContentView(R.layout.groupinfo);
            dialog.setTitle("Group Info");

            ListView mem_list = (ListView) dialog.findViewById(R.id.member_list);
            TextView com = (TextView) dialog.findViewById(R.id.comment_text);

            com.setText(comment.get(group_id));
            ArrayList<String> list = new ArrayList<String>();

            list = gm.load_member();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(groupform.this, android.R.layout.simple_list_item_1, list);

            mem_list.setAdapter(adapter);
            mem_list.setChoiceMode(mem_list.CHOICE_MODE_SINGLE);




            dialog.show();

            return false;
        }
    }

    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            group_id = ((TextView) view.findViewById(R.id.name_text)).getText().toString();

        }
    }

    public void onBackPressed() {
        AlertDialog.Builder alert3 = new AlertDialog.Builder(groupform.this);
        alert3.setTitle("Are you want to logout?");
        // Set an EditText view to get user input


        alert3.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


                logout=true;
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

                if (group_id != null) {

                    Intent intent = new Intent(this, filelistform.class);
                    intent.putExtra("group_id", group_id);
                    intent.putExtra("user_id", user_id);
                    startActivity(intent);

                } else {
                    Toast toast = Toast.makeText(groupform.this, "Choice Group", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.make_button:

                final EditText input_name, input_comment;
                Button ok;
                final Dialog dialog = new Dialog(groupform.this);
                dialog.setContentView(R.layout.makegroup);


                input_name = (EditText) dialog.findViewById(R.id.input_name);
                input_comment = (EditText) dialog.findViewById(R.id.input_comment);
                input_name.setFilters(new InputFilter[]{filterAlphaNum});
                ok = (Button) dialog.findViewById(R.id.button);

                ok.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        String name = input_name.getText().toString();
                        String comment = input_comment.getText().toString();

                        gm.make_group(user_id, name, comment);

                        dialog.dismiss();

                        refreshList();
                    }
                });

                dialog.show();

                break;
            case R.id.invite_button:
                if (group_id != null) {
                    AlertDialog.Builder alert4 = new AlertDialog.Builder(groupform.this);
                    alert4.setTitle("Input User ID");
                    // Set an EditText view to get user input
                    final EditText input2 = new EditText(this);
                    alert4.setView(input2);

                    alert4.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input2.getText().toString();


                            gm.invite_group(user_id, group_id, value);
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

                } else {
                    Toast toast = Toast.makeText(groupform.this, "Choice Group", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;

            case R.id.refresh_button:

                refreshList();

                break;
            case R.id.exit_button:

                if (group_id != null) {
                    AlertDialog.Builder alert3 = new AlertDialog.Builder(groupform.this);
                    alert3.setTitle("Are you sure you want to exit?");
                    // Set an EditText view to get user input


                    alert3.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            gm.exit_group(user_id, group_id);
                            // Do something with value!

                            refreshList();
                        }
                    });
                    alert3.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });
                    alert3.show();
                } else {
                    Toast toast = Toast.makeText(groupform.this, "Choice Group", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.delete_button:
                if (group_id != null) {
                    AlertDialog.Builder alert2 = new AlertDialog.Builder(groupform.this);
                    alert2.setTitle("Are you sure you want to delete?");
                    // Set an EditText view to get user input


                    alert2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            gm.delete_group(user_id, group_id);
                            // Do something with value!

                            refreshList();
                        }
                    });
                    alert2.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });
                    alert2.show();
                } else {
                    Toast toast = Toast.makeText(groupform.this, "Choice Group", Toast.LENGTH_SHORT);
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

/*
    public class receive_invite extends AsyncTask<Void, Void, Void> {

        String isInvited="";


        @Override
        protected Void doInBackground(Void... arg0) {


            try {
                System.out.println("aaa");
                while(logout==false) {

                    MySocket ms = new MySocket();
                    isInvited = ms.receive_msg();
                    System.out.println("vvv");
                }

            }
            catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
        }


    }
*/
}
