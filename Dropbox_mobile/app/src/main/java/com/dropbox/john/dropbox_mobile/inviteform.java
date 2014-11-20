package com.dropbox.john.dropbox_mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class inviteform extends Activity implements OnClickListener {

    Button accept_button,reject_button;

    ListView invitelist;
    String group_id=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitelistform);


        accept_button = (Button) findViewById(R.id.accept_button);
        accept_button.setOnClickListener(this);

        reject_button = (Button) findViewById(R.id.reject_button);
        reject_button.setOnClickListener(this);


        refreshList(100);
}

    public void refreshList(int i) {

        ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();
        invitelist = (ListView) findViewById(R.id.invitelist);

        String[] from = new String[] {"Group_ID","Inviter"};
        int[] to = new int[]{R.id.group_id,R.id.inviter};


        list = load_list();


        SimpleAdapter notes = new SimpleAdapter(this,list, R.layout.invitelist, from,to);
        invitelist.setAdapter(notes);
        invitelist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        invitelist.setOnItemClickListener( new ListViewItemClickListener() );
    }

    private class ListViewItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            group_id = ((TextView) view.findViewById(R.id.group_id)).getText().toString();

        }
    }

    public void onBackPressed() {
        Intent intent2 = new Intent(this, groupform.class);
        startActivity(intent2);
        finish();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int getId = v.getId();
        switch (getId) {
            case R.id.accept_button:

                if(group_id!=null) {

                    Intent intent = new Intent(this, filelistform.class);
                    startActivity(intent);

                }
                else {
                    Toast toast = Toast.makeText(inviteform.this,"Choice Message",Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.reject_button:



                break;

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    public ArrayList load_list() {

        ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();


        // 서버로 부터 그룹 목록을 받음

        String data = "[{\"Group_ID\":\"a\",\"Inviter\":\"1\"},{\"Group_ID\":\"b\",\"Inviter\":\"2\"},{\"Group_ID\":\"c\",\"Inviter\":\"3\"}]";
        try{

            JSONArray ja = new JSONArray(data);
            for (int i = 0; i < ja.length(); i++){
                JSONObject order = ja.getJSONObject(i);
                HashMap<String, String> item =   new HashMap<String, String>();
                item.put("Group_ID",order.getString("Group_ID"));
                item.put("Inviter",order.getString("Inviter"));
                System.out.println(item);
                list.add(item);
            }
        }
        catch (JSONException e){ ;}

        return list;
    }


}
