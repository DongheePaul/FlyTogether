package com.dong.PersonalData_Login_SignUP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dong.Main2Activity;
import com.nhancv.kurentoandroid.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Login_activity extends AppCompatActivity {
    EditText et_id, et_pw;
    String st_id, st_pw;
    Button bt_login, bt_cancle;
    String st_return_from_server, st_result, st_result_id, st_result_name;
    String st_autologin_id, st_autologin_pw;

    SharedPreferences autoLogin;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);
        et_id = (EditText)findViewById(R.id.et_id);
        et_pw = (EditText)findViewById(R.id.et_pw);
        bt_login = (Button)findViewById(R.id.bt_login);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                st_id = et_id.getText().toString();
                st_pw = et_pw.getText().toString();

                loginDB lDB=new loginDB();
                lDB.execute();
            }
        });

        //자동로그인용 sharedpreference 파일.
        autoLogin = getSharedPreferences("autoLogin",Activity.MODE_PRIVATE);
        st_autologin_id = autoLogin.getString("id", "");
        st_autologin_pw = autoLogin.getString("pw", "");
        //자동로그인용 id, 비번이 저장되어 있는지 확인.
        if(!st_autologin_id.equals("")){
            Log.e("현재 저장된 자동로그인 아이디", st_autologin_id);
            Log.e("현재 저장된 자동로그인 비번", st_autologin_pw);
        }

    }



    //로그인 하기 위해 서버랑 통신하는 클래스
    public class loginDB extends AsyncTask<Void, Integer, Void> {
        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(Void... params) {
            //아이디, 비밀번호를 전달할 파라미터 변수
            String param = "u_id=" + st_id + "&u_pw=" + st_pw;
            Log.e("param sent in loginactivity",param);

            try {
                //로그인에 사용되는 파일에 접근하기 위한 url값 저장
                URL url = new URL("http://222.239.249.149/flytogether/login.php");
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

                //서버로부터 받은 인풋스트림을 스트링화
                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                //서버의 리턴값

                st_return_from_server = buff.toString().trim();
                try {
                    JSONObject jsonObject = new JSONObject(st_return_from_server);
                    st_result = jsonObject.getString("result");
                    st_result_id = jsonObject.getString("id");
                    st_result_name = jsonObject.getString("name");
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
                /* 서버에서 한 응답을 보여주는 로그 */
                Log.e("server return check in LoginActivity", st_return_from_server);

                if(st_result.equals("1"))
                {
                    Log.e("login result","성공적으로 처리되었습니다!");
                }
                else
                {
                    Log.e("login result","에러 발생! ERRCODE = " + st_return_from_server);
                }
            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }

        //서버의 리턴값에 해당하는 이벤트 실행한다.
        //로그인 성공, 존재하지 않는 아이디, 비밀번호 불일치, 에러 발생시
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //로그인 성공시 서버에서 1을 리턴한다. 서버에서 1을 리턴시 메인으로 이동
            if(st_result.equals("1")){
                Log.e("RESULT", "성공적으로 로긴처리");
                Intent intent = new Intent(Login_activity.this, Main2Activity.class);
                intent.putExtra("user_id", st_result_id);
                intent.putExtra("user_name", st_result_name);
                startActivity(intent);

                //자동로그인 sharedpreference에 새로운 값을 저장.
                editor = autoLogin.edit();
                editor.putString("id", st_result_id);
                editor.putString("pw", st_pw);
                editor.putString("name", st_result_name);
                editor.commit();
                Log.e("새로운 자동로그인 아이디", autoLogin.getString("id",""));
                Log.e("새로운 자동로그인 비번.", autoLogin.getString("pw",""));
                Log.e("새로운 자동로그인 이름.", autoLogin.getString("name",""));
                finish();
            }

            //비밀번호 불일치 시 서버에서 0을 리턴한다. 서버에서 0을 리턴시 다이얼로그창을 띄운다.
            else if(st_result.equals("0")){
                Log.e("RESULT","비밀번호가 일치하지 않습니다.");
                AlertDialog.Builder dialog = new AlertDialog.Builder(Login_activity.this);
                dialog.setTitle("알림");
                dialog.setMessage("아이디와 비밀번호를 확인하세요.");
                dialog.setCancelable(true);
                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                });
                dialog.create();
                dialog.show();

            }

            //존재하지 않는 계정 입력 할 시 서버에서 B를 리턴한다.
            //서버에서 B를 리턴 시 토스트메세지를 띄운다.
            else if (st_result.equals("null")){
                Log.e("존재하지 않는 계정", "존재하지 않는 계정");
                Toast.makeText(Login_activity.this, "존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            //에러 발생시 알럴트 창 띄우기
            else
            {
                Log.e("Result", "에러발생! ERRORCODE = "+st_return_from_server);
                AlertDialog.Builder dialog = new AlertDialog.Builder(Login_activity.this);
                dialog.setTitle("알림");
                dialog.setMessage("에러가 발생했습니다. 이 메세지가 계속된다면 개발자에게 문의해주시길 부탁드립니다. \n E-mail:dlehdgml0480@naver.com ");
                dialog.setCancelable(true);
                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                });
                dialog.create();
                dialog.show();
            }

        }
    }
    //회원가입한 계정으로 로그인 하기 위해 서버랑 통신하는 클래스
    //끝

}
