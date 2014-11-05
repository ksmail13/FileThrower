package com.dropbox.john.dropbox_mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class groupform extends Activity implements OnClickListener {

    Button enter_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.groupform);


        enter_button = (Button) findViewById(R.id.enter_button);
        enter_button.setOnClickListener(this);

    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, filelistform.class);
        startActivity(intent);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }




}
