package dropbox.user_interface;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.EditText;


import dropbox.artifacts.join;



import java.io.IOException;
import java.util.regex.Pattern;


public class joinform extends Activity implements OnClickListener {

    Button join_button;
    EditText input_id,input_pw, input_email, input_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.joinform);


        join_button = (Button) findViewById(R.id.join_button);
        join_button.setOnClickListener(this);

        input_email = (EditText) findViewById(R.id.input_email);


        input_id = (EditText) findViewById(R.id.input_id);
        input_id.setFilters(new InputFilter[] {filterAlphaNum});

        input_pw = (EditText) findViewById(R.id.input_pw);

        input_confirm = (EditText) findViewById(R.id.input_confirm);

    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        join user = new join(input_email.getText().toString(), input_id.getText().toString(), input_pw.getText().toString(), input_confirm.getText().toString());
        int correct_info = 0;
        try {
            correct_info = user.correct_info();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (correct_info == 3) {

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


}
