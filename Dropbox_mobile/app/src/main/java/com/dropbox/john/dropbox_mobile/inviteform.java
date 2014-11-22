package com.dropbox.john.Dropbox_Mobile;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;



import org.json.JSONArray;
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

public class inviteform extends Activity implements OnClickListener {

    Button accept_button,reject_button;

    ListView invitelist;
    String group_id=null;
    String list_data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitelistform);


        accept_button = (Button) findViewById(R.id.accept_button);
        accept_button.setOnClickListener(this);

        reject_button = (Button) findViewById(R.id.reject_button);
        reject_button.setOnClickListener(this);


        refreshList();
    }

    public void refreshList() {

        ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();
        invitelist = (ListView) findViewById(R.id.invitelist);

        String[] from = new String[] {"Group_ID","Inviter"};
        int[] to = new int[]{R.id.group_id,R.id.inviter};


        list = load_list();


        SimpleAdapter notes = new SimpleAdapter(this,list, R.layout.invitelist, from,to);
        invitelist.setAdapter(notes);
        invitelist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        invitelist.setOnItemClickListener( new ListViewItemClickListener() );
        invitelist.setOnItemLongClickListener(new ListViewItemLongClickListener());
        group_id=null;
    }

    private class ListViewItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            group_id = ((TextView) view.findViewById(R.id.group_id)).getText().toString();

        }
    }
    private class ListViewItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            group_id = ((TextView) view.findViewById(R.id.group_id)).getText().toString();
            return false;
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
            case R.id.accept_button:

                if(group_id!=null) {
                    org.json.simple.JSONObject obj = new org.json.simple.JSONObject();

                    obj.put("Group_ID",group_id);

                    String data = obj.toString();

                    refreshList();

                }
                else {
                    Toast toast = Toast.makeText(inviteform.this,"Choice Invite Message",Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.reject_button:
                if(group_id!=null) {
                    org.json.simple.JSONObject obj = new org.json.simple.JSONObject();

                    obj.put("Group_ID",group_id);
                    String data = obj.toString();


                    refreshList();


                }
                else {
                    Toast toast = Toast.makeText(inviteform.this,"Choice Invite Message",Toast.LENGTH_SHORT);
                    toast.show();
                }

                break;

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    public ArrayList load_list() {

        ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();



        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("Event","invitelist");
        String data = obj.toString();



        // 서버로 부터 초대받은 그룹의 목록을 받음
        list_data = "[{\"Group_ID\":\"a\",\"Inviter\":\"1\"},{\"Group_ID\":\"b\",\"Inviter\":\"2\"},{\"Group_ID\":\"c\",\"Inviter\":\"3\"}]";

        try{

            JSONArray ja = new JSONArray(list_data);
            for (int i = 0; i < ja.length(); i++){
                JSONObject order = ja.getJSONObject(i);
                HashMap<String, String> item =   new HashMap<String, String>();
                item.put("Group_ID",order.getString("Group_ID"));
                item.put("Inviter",order.getString("Inviter"));

                list.add(item);
            }
        }
        catch (JSONException e){ ;}

        return list;
    }


}
