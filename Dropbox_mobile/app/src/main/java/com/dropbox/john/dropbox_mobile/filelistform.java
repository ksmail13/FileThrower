package com.dropbox.john.dropbox_mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;

import android.widget.*;


public class filelistform extends Activity implements OnClickListener {
    ArrayList<String> list;
    ArrayAdapter<String> adapter;
    ListView filelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.filelistform);


        list = new ArrayList<String>();
        list.add("01");
        list.add("02");
        list.add("03");
        list.add("04");
        list.add("05");
        list.add("06");
        list.add("07");
        list.add("08");
        list.add("09");
        list.add("10");
        list.add("10");
        list.add("10");
        list.add("10");
        list.add("10");
        list.add("10");
        list.add("10");
        list.add("10");

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, list);

        filelist = (ListView) findViewById(R.id.filelist);
        filelist.setAdapter(adapter);
        filelist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, joinform.class);
        startActivity(intent);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }




}
