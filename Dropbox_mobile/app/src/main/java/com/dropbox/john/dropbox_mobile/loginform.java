package com.dropbox.john.dropbox_mobile;

import android.app.Activity;
import android.content.SharedPreferences;
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
    CheckBox checkbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginform);

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);


        join_button = (Button) findViewById(R.id.join_button);
        join_button.setOnClickListener(this);

        login_button = (Button) findViewById(R.id.login_button);
        login_button.setOnClickListener(this);

        input_id = (EditText) findViewById(R.id.input_id);
        input_pw = (EditText) findViewById(R.id.input_pw);
        checkbox = (CheckBox) findViewById(R.id.checkBox);


        String text = pref.getString("input_id", "");
        String text2 = pref.getString("input_pw", "");
        Boolean chk1 = pref.getBoolean("checkbox", false);


        input_id.setText(text);
        input_pw.setText(text2);
        checkbox.setChecked(chk1);



    }
    public void onStop(){
// 어플리케이션이 화면에서 사라질때
        super.onStop();
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
// UI 상태를 저장합니다.
        SharedPreferences.Editor editor = pref.edit();
// Editor를 불러옵니다.
        checkbox = (CheckBox) findViewById(R.id.checkBox);
        input_id = (EditText) findViewById(R.id.input_id);
        input_pw = (EditText) findViewById(R.id.input_pw);

// 저장할 값들을 입력합니다.
        editor.putString("input_id", input_id.getText().toString());
        editor.putString("input_pw", input_pw.getText().toString());
        editor.putBoolean("checkbox", checkbox.isChecked());


        editor.commit();
// 저장합니다.
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
                login user = new login(input_id.getText().toString(),input_pw.getText().toString());
                int correct_user = user.correct_user();
                if(correct_user==1) {
                    global global_id = (global)getApplicationContext();
                    global_id.setState(input_id.getText().toString());

                    Intent intent2 = new Intent(this, groupform.class);
                    startActivity(intent2);

                }
                else if(correct_user==3){
                    Intent intent3 = new Intent(this, inviteform.class);
                    startActivity(intent3);
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
