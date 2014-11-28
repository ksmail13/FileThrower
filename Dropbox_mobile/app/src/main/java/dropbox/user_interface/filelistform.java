package dropbox.user_interface;

import android.app.*;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.database.Cursor;

import android.widget.*;


import dropbox.artifacts.MyFtpClient;
import dropbox.artifacts.file_management;



public class filelistform extends Activity implements OnClickListener {
    ArrayList<String> list;
    ArrayAdapter<String> adapter;
    ListView filelist;
    String file_id=null;
    String group_id="";

    String user_id;
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
        user_id = intent.getStringExtra("user_id");





        try {
            refreshList();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void refreshList() throws InterruptedException {



        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        filelist = (ListView) findViewById(R.id.filelist);


        String[] from = new String[]{"name", "size"};
        int[] to = new int[]{R.id.name_text, R.id.size_text};


        list = fm.load_list(group_id);


        SimpleAdapter notes = new SimpleAdapter(filelistform.this, list, R.layout.filelist, from, to);
        filelist.setAdapter(notes);
        filelist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        /*
        String[] from = new String[] {"name","size","date","uploader"};
        int[] to = new int[]{R.id.name_text,R.id.size_text,R.id.date_text,R.id.uploader_text};



        list = fm.load_list();



        SimpleAdapter notes = new SimpleAdapter(this,list, R.layout.filelist, from,to);
        filelist.setAdapter(notes);
        filelist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        */


        filelist.setOnItemClickListener(new ListViewItemClickListener());
        filelist.setOnItemLongClickListener(new ListViewItemLongClickListener());

        file_id = null;


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

    private String getPath(Uri uri) throws UnsupportedEncodingException {
        String res = null;

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor.moveToFirst()){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            System.out.println("--------"+res);
        }
        cursor.close();
        return res;
    }
    private String getName(Uri uri)
    {
        String[] projection = { MediaStore.Images.ImageColumns.DISPLAY_NAME };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                long file_size=0;
                Uri uri = intent.getData();
                String path = null;
                try {
                    path = getPath(uri);
                    file_id = getName(uri);
                    File f = new File(uri.getPath());
                    file_size = f.length();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                System.out.println(path + "==== " + uri);
                try {

                    fm.upload(user_id,group_id,file_id,file_size,path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    refreshList();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
                    fm.download(group_id,file_id);

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

                            try {
                                fm.delete(group_id,file_id);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // Do something with value!
                            try {
                                refreshList();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

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
                    input.setFilters(new InputFilter[] {filterAlphaNum});
                    alert.setView(input);

                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();
                            fm.rename(value,group_id,file_id);
                            // Do something with value!

                            try {
                                refreshList();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
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
                try {
                    refreshList();
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
    private class ListViewItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            file_id = ((TextView) view.findViewById(R.id.name_text)).getText().toString();
            return false;
        }
    }
}
