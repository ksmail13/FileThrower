package com.dropbox.john.dropbox_mobile;

/**
 * Created by John on 2014-11-20.
 */
import android.app.Application;
public class global extends Application {

    private String state;

    @Override
    public void onCreate() {
        //전역 변수 초기화
        state = "";
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void setState(String state){
        this.state = state;
    }

    public String getState(){
        return state;
    }
}
