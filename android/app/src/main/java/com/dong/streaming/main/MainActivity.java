package com.dong.streaming.main;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.EditText;

import com.dong.streaming.broadcaster.BroadCasterActivity_;
import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.nhancv.kurentoandroid.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;


/**
 * 2018-11-30 19:40
 * 방송시작 화면.
 * 방송제목을 입력하는 에디트텍스트와 방송시작 버튼이 있다.
 */

@EActivity(R.layout.activity_main)
public class MainActivity extends MvpActivity<MainView, MainPresenter> implements MainView {
    private static final String TAG = MainActivity.class.getName();
    @ViewById(R.id.et_streaming_room_name)
    //방송방 제목
    protected EditText et_streaming_room_name;

    //Main2Activity에서 넘어온 로그인한 유저의 id.
    public String user_id;
    //방송시작 버튼을 누르면 일어나는 이벤트.
    @Click(R.id.btBroadCaster)
    protected void btBroadCasterClick() {
        //입력된 방송제목을 스트링으로 만든다
        String st_streaming_room_name = et_streaming_room_name.getText().toString();
        //로그인한 유저의 id를 broadcasteractivity로 보낸다.
        BroadCasterActivity_.intent(this).extra("user_id", user_id).extra("streaming_room_name", st_streaming_room_name).extra("key", "create").start();
        //BroadCasterActivity_.intent(this).start();
    }

    @NonNull
    @Override
    public MainPresenter createPresenter() {
        user_id= getIntent().getStringExtra("user_id");
        Log.e("mainactivity", "got user_id: " + user_id);
        return new MainPresenter(getApplication());
    }


}
