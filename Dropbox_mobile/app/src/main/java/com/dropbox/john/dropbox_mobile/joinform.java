package com.dropbox.john.dropbox_mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.EditText;


public class joinform extends Activity implements OnClickListener {

    Button join_button;
    EditText input_id,input_pw, input_name, input_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.joinform);


        join_button = (Button) findViewById(R.id.join_button);
        join_button.setOnClickListener(this);

        input_name = (EditText) findViewById(R.id.input_name);
        input_id = (EditText) findViewById(R.id.input_id);
        input_pw = (EditText) findViewById(R.id.input_name);

        input_confirm = (EditText) findViewById(R.id.input_confirm);

    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        join user = new join(input_name.getText().toString(), input_id.getText().toString(), input_pw.getText().toString(), input_confirm.getText().toString());
        int correct_info = user.correct_info();
        if (correct_info == 3) {
            Intent intent = new Intent(this, loginform.class);
            startActivity(intent);
            finish();
        } else if (correct_info == 2)
        {
            Toast toast = Toast.makeText(this, "ID already exist",Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(correct_info==1)
        {
            Toast toast = Toast.makeText(this, "Input Correct Confirm Password",Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }




}
