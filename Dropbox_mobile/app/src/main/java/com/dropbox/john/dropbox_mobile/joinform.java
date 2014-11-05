package com.dropbox.john.dropbox_mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class joinform extends Activity implements OnClickListener {

    Button join_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.joinform);


        join_button = (Button) findViewById(R.id.join_button);
        join_button.setOnClickListener(this);

    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(this, loginform.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }




}
