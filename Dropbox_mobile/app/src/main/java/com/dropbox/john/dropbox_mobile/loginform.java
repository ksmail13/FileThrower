package com.dropbox.john.dropbox_mobile;

import android.app.Activity;
import android.os.Bundle;

import android.widget.*;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;


public class loginform extends Activity implements OnClickListener {

    Button join_button;
    Button login_button;
    EditText input_id;
    EditText input_pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginform);


        join_button = (Button) findViewById(R.id.join_button);
        join_button.setOnClickListener(this);

        login_button = (Button) findViewById(R.id.login_button);
        login_button.setOnClickListener(this);

        input_id = (EditText) findViewById(R.id.input_id);
        input_pw = (EditText) findViewById(R.id.input_name);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        int getId = v.getId();
        String[] id,password;
        switch (getId) {
            case R.id.join_button:
                Intent intent = new Intent(this, joinform.class);
                startActivity(intent);
                break;
            case R.id.login_button:
                login user = new login(input_id.getText().toString(),input_id.getText().toString());
                int correct_user = user.correct_user();
                if(correct_user==1) {
                    Intent intent2 = new Intent(this, groupform.class);
                    startActivity(intent2);
                }
                else if(correct_user==2)
                {
                    Toast toast = Toast.makeText(this, "Not correct login info",Toast.LENGTH_SHORT);
                    toast.show();
                }

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
