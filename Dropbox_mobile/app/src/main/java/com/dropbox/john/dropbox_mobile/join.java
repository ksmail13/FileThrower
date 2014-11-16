package com.dropbox.john.dropbox_mobile;

/**
 * Created by John on 2014-11-06.
 */
public class join {

    String Name,ID,PW,Confirm_PW;
    public join (String name, String id, String pw, String confirm_pw)
    {
        Name = name;
        ID = id;
        PW = pw;
        Confirm_PW = confirm_pw;

    }
    public int correct_info()
    {
        if(PW.equals(Confirm_PW))
        {
            if(true)//id 중복확인
            {
                // 서버로 아이디생성 정보 전송
                return 3;
            }
            else return 2;
        }
        else return 1;

    }
}
