package com.dong.PersonalData_Login_SignUP;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dong.Main2Activity;
import com.nhancv.kurentoandroid.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 회원가입 액티비티.
 * 이름, 아이디, 비밀번호를 입력하고 나서 회원가입 버튼을 누르면
 *
 */
public class Sign_up_activity extends AppCompatActivity {
        EditText et_name, et_id, et_password;
        Button bt_sign_up, bt_cancle;
        String user_name, user_id, user_pw;
    String return_from_server;  //요청한 회원가입 처리에 대한 서버의 응답값을 담을 스트링
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_activity);
        //회원가입을 위해 이름을 입력하는 edittext
        et_name = (EditText)findViewById(R.id.et_name);
        //회원가입을 위해 아이디를 입력하는 edittext
        et_id = (EditText)findViewById(R.id.et_id);
        //회원가입을 위해 비밀번호를 입력하는 edittext
        et_password = (EditText)findViewById(R.id.et_password);

        bt_sign_up = (Button)findViewById(R.id.bt_sign_up);
        bt_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            user_name = et_name.getText().toString();
            user_id = et_id.getText().toString();
            user_pw = et_password.getText().toString();

            Sign_up_request sign_up_request = new Sign_up_request();
            sign_up_request.execute();

            }
        });
        bt_cancle = (Button)findViewById(R.id.bt_cancle);
        bt_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    //db와의 통신을 통해 회원 테이블에 회원정보를 삽입하는 회원가입 클래스
    public class Sign_up_request extends AsyncTask<Void, Integer, Void>{


        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(Void... params) {
            //아이디, 비밀번호, 이름을 전달할 파라미터 변수
            String param = "u_id=" + user_id + "&u_pw=" + user_pw + "&u_name="+user_name;
            Log.e("param in Sign_up_A",param);

            try {
                //로그인에 사용되는 파일에 접근하기 위한 url값 저장
                URL url = new URL("http://222.239.249.149/flytogether/member_sign_up.php");
                //httpurlconnection 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //conn 설정
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버로 아이디, 비밀번호 값 전달 */
                OutputStream outs = conn.getOutputStream();
                //통신하기 위한 url 인코딩
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                /* 서버 -> 안드로이드 서버의 리턴값 전달 */
                InputStream is = null;
                BufferedReader in = null;
                return_from_server = "";

                //서버로부터 받은 인풋스트림을 스트링화
                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {  //스트링버퍼를 한줄씩 append.
                    buff.append(line + "\n");
                }
                //서버의 리턴값

                return_from_server = buff.toString().trim();
                Log.e("return from server in Sign", return_from_server);

                if(return_from_server.equals("1")){
                    Log.e("회원관리 무사히", "처리");
                }
                else {
                    Log.e("회원관리 ", "실패");
                }

            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }

        //서버의 리턴값에 해당하는 이벤트 실행한다.
        //회원가입에 성공하면 MainActivity2로 이동한다.
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(return_from_server.equals("1")){
                Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                startActivity(intent);
                finish();
            }
            else{
                Toast.makeText(getApplicationContext(), "회원가입 실패. 다시 시도해주세요", Toast.LENGTH_SHORT).show();
            }

        }


    }


}
