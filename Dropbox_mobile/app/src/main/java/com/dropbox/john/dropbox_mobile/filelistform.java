package com.dropbox.john.dropbox_mobile;

import android.app.*;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import java.util.*;

import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.database.Cursor;

import android.widget.*;


public class filelistform extends Activity implements OnClickListener {
    ArrayList<String> list;
    ArrayAdapter<String> adapter;
    ListView filelist;
    String file_id=null;
    String group_id="";
    file_management fm = new file_management();
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

        Intent intent = getIntent();
        group_id = intent.getStringExtra("group_id");
        System.out.println(group_id);

        refreshList();
    }
    public void refreshList() {

        ArrayList<HashMap<String,String>> list =new ArrayList<HashMap<String, String>>();
        filelist = (ListView) findViewById(R.id.filelist);

        String[] from = new String[] {"name","size","date","uploader"};
        int[] to = new int[]{R.id.name_text,R.id.size_text,R.id.date_text,R.id.uploader_text};



        list = fm.load_list();



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

            file_id = ((TextView) view.findViewById(R.id.name_text)).getText().toString();

        }
    }

    public void onBackPressed() {
        finish();
    }

    private String getPath(Uri uri)
    {
        String res = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = intent.getData();
                String path = getPath(uri);
                System.out.println(path);
            }

        }
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int getId = v.getId();
        switch (getId) {
            case R.id.download_button:
                if(file_id!=null) {


                }
                else {
                    Toast toast = Toast.makeText(filelistform.this,"Choice File",Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.upload_button:

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");

                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(i, 1);


                break;
            case R.id.delete_button:

                if(file_id!=null) {

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
                }
                else {
                    Toast toast = Toast.makeText(filelistform.this,"Choice File",Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;

            case R.id.rename_button:
                if(file_id!=null) {
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
                }
                else {
                    Toast toast = Toast.makeText(filelistform.this,"Choice File",Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;

            case R.id.refresh_button:
                refreshList();

                break;

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }




}
