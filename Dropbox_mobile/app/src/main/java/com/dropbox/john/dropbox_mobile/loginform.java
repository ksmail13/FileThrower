package com.dropbox.john.dropbox_mobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;


public class loginform extends Activity implements OnClickListener {

    Button join_button;
    Button login_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginform);


        join_button = (Button) findViewById(R.id.join_button);
        join_button.setOnClickListener(this);

        login_button = (Button) findViewById(R.id.login_button);
        login_button.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        int getId = v.getId();
        switch (getId) {
            case R.id.join_button:
                Intent intent = new Intent(this, joinform.class);
                startActivity(intent);
                break;
            case R.id.login_button:
                Intent intent2 = new Intent(this, groupform.class);
                startActivity(intent2);
                break;
        }

    }

    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }




}
