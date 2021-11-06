package com.dong;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.dong.Broadcasting_List.Broadcasting_list;
import com.dong.Diary.DiaryListFragment;
import com.dong.PersonalData_Login_SignUP.Setting_fragment;
import com.dong.Vod_List.VodList_fragment;
import com.dong.streaming.main.MainActivity_;
import com.nhancv.kurentoandroid.R;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class Main2Activity extends FragmentActivity {

    //자동로그인용 sharedpreference
    SharedPreferences autoLogin;
    SharedPreferences.Editor editor;

    //자동로그인 sharedpreference("autoLogin")에 저장된 아이디, 이름, 비번
    String st_autologin_id, st_autologin_name, st_autologin_pw;

    private VodList_fragment vodlist_fragment;
    private Broadcasting_list broadcasting_list;
    private Setting_fragment setting_fragment;
    private DiaryListFragment diaryListFragment;
    private FloatingActionButton fab_broadcasting_start;
    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //상태 바 없애기 위한 메소드. setContentView 보다 먼저 선언해줘야함.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);

        //방송시작하는 플로팅 버튼
        fab_broadcasting_start = (FloatingActionButton)findViewById(R.id.fab_broadcasting_start);

        //자동로그인용 sharedpreference 파일.
        autoLogin = getSharedPreferences("autoLogin",Activity.MODE_PRIVATE);
        st_autologin_id = autoLogin.getString("id", "");
        st_autologin_name = autoLogin.getString("name", "");
        //자동로그인용 id, 비번이 저장되어 있는지 확인.
        if(!st_autologin_id.equals("")){
            Log.e("현재 저장된 자동로그인 아이디 in Main2Activity", st_autologin_id);
            Log.e("현재 저장된 자동로그인 비번 in Main2Activity", st_autologin_name);
        }
        //로그인 상태가 아니라면 플로팅 버튼 안 보이게
        else {
            fab_broadcasting_start.setVisibility(FloatingActionButton.GONE);
        }



        //메인화면 구성하는 생방송 목록, 설정 프레그먼트 생성.
        broadcasting_list = new Broadcasting_list();
        setting_fragment = new Setting_fragment();
        vodlist_fragment = new VodList_fragment();
        diaryListFragment = new DiaryListFragment();



        //메인액티비티 시작시 띄울 메인 프레그먼트 설정해주는 메소드
        initFragment();

        //바텀 바 찾아주고 탭리스너 생성,.
        BottomBar bottomBar = (BottomBar)findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(int tabId) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                //bottom bar가 어떤 탭이 클릭되었는지를 인식한다.
                // 그래서 해당 탭과 연결된 프레그먼트로 화면이 전환됨.
                if(tabId == R.id.broadcasting_list_tab){
                    fragmentTransaction.replace(R.id.MainActivity_FrameLayout, broadcasting_list).commit();
                }
                else if(tabId == R.id.setting_tab){
                    fragmentTransaction.replace(R.id.MainActivity_FrameLayout, setting_fragment).commit();
                }
                else if(tabId == R.id.vod_list_tab){
                    fragmentTransaction.replace(R.id.MainActivity_FrameLayout, vodlist_fragment).commit();
                }
                else if(tabId == R.id.diary_list_tap){
                    fragmentTransaction.replace(R.id.MainActivity_FrameLayout, diaryListFragment).commit();
                }
            }
        });

        //방송시작 플로팅 버튼을 누르면 Broadcast_start_activity(방송시작 화면)으로 이동한다.
        fab_broadcasting_start.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2Activity.this, MainActivity_.class);
                intent.putExtra("user_id", st_autologin_id);

                startActivity(intent);
                }

        });

    }
    //메인액티비티 시작시 띄울 메인 프레그먼트 설정해주는 메소드
    public void initFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.MainActivity_FrameLayout, broadcasting_list);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
