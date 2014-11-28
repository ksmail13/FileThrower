package dropbox.user_interface;

import android.app.Activity;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import dropbox.artifacts.MySocket;
import dropbox.common.Message;
import dropbox.common.MessageType;



public class inviteform extends Activity implements OnClickListener {

    Button accept_button,reject_button;

    ListView invitelist;
    String group_id=null;
    String list_data;
    String data;
    String result;
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

                    obj.put("group",group_id);

                    obj.put(Message.SUBCATEGORY_KEY,"accept");
                    data = obj.toString();

                    Thread accept = new Thread( new Runnable() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            msg.msg = data;
                            msg.messageType = MessageType.Group;
                            MySocket ms = new MySocket();
                            try {
                                ms.send_msg(msg);
                                result = ms.receive_msg();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    accept.start();
                    try {
                        accept.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }



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
