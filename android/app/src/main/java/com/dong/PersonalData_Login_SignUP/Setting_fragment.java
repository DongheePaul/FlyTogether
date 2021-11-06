package com.dong.PersonalData_Login_SignUP;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dong.AR.DrawARActivity;
import com.dong.EtheriumToken.WalletTokenActivity;
import com.dong.FaceDetection.FaceDetectionActivity;
import com.dong.Main2Activity;
import com.nhancv.kurentoandroid.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class Setting_fragment extends Fragment {
    //얼굴인식(vision api)를 활용한 마스크 씌우기를 위해 필요한 변수들.
    String mCurrentPhotoPath;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2;
    Button bt_FaceDetection;


    //로그인 위한 위젯, 변수들.
    EditText et_id, et_pw;
    String st_id_for_login, st_pw;
    Button bt_login, bt_cancle;
    String st_return_from_server, st_result, st_result_id, st_result_name;
    String st_autologin_id, st_autologin_pw;

    //회원가입버튼
    Button  bt_sign_up, bt_AR;


    TextView tv_show_id, tv_show_name;

    //로그인된 아이디, 이름
    String st_id;
    String st_name;

    //자동로그인용 sharedpreference
    SharedPreferences autoLogin;
    SharedPreferences.Editor editor;

    //Setting_fragment의 뷰. 로그인 여부에 따라 fragment_setting_fragemnt(로그인) 와 fragment_login(로그인 안함) 둘 중 하나를 띄운다.
    View v;

    private OnFragmentInteractionListener mListener;

    public Setting_fragment() {

    }

    // TODO: Rename and change types and number of parameters
    public static Setting_fragment newInstance(String param1, String param2) {
        Setting_fragment fragment = new Setting_fragment();
        Bundle args = new Bundle();
/*        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*/
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("LongLogTag")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //자동로그인용 id, 비번이 저장되어 있다면(=로그인한 상태)
        //자동로그인용 sharedpreference 파일.
        autoLogin = this.getActivity().getSharedPreferences("autoLogin",Activity.MODE_PRIVATE);
        st_id = autoLogin.getString("id", "");
        st_name = autoLogin.getString("name", "");
        //로그인된 상황이라면 회원정보 프래그먼트를 호출한다.
        if(!st_name.equals("")){
            Log.e("자동로그인 아이디 in setting_fragment", st_id);
            Log.e("자동로그인 비번 in setting_fragment", st_name);
            //회원정보 프래그먼트를 호출한다.
            v = inflater.inflate(R.layout.fragment_setting_fragment, container, false);
            // Inflate the layout for this fragment
            //로그인한 유저의 id를 보여주는 textView
            tv_show_id = (TextView)v.findViewById(R.id.tv_show_id);
            //로그인한 유저의 이름을 보여주는 textView
            tv_show_name = (TextView)v.findViewById(R.id.tv_show_name);

            //로그아웃 버튼. 누르면 Login_activity로 넘어간다.
            bt_login = (Button)v.findViewById(R.id. bt_login);
            bt_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor = autoLogin.edit();
                    editor.remove("id");
                    editor.remove("name");
                    editor.remove("pw");
                    editor.commit();

                    Intent intent = new Intent(getActivity(), Main2Activity.class);
                    startActivity(intent);
                    getActivity().finish();

                    Log.e("로그아웃 된거?", "로그아웃");
                }
            });
            //회원아이디를 보여준다
            tv_show_id.setText(st_id);
            //회원정보를 보여준다.
            tv_show_name.setText(st_name);

            bt_AR = (Button)v.findViewById(R.id.bt_AR);
            bt_AR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), DrawARActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });

            Button bt_createwallet = (Button)v.findViewById(R.id.bt_WalletToken);
            bt_createwallet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), WalletTokenActivity.class);
                    startActivity(intent);
                }
            });
        }
        //자동로그인용 id, 비번이 저장되어 있지 있다면(=로그인 안한 상태)
        else {
            Log.e("자동로그인 아이디 in setting_fragment", "없다능");
            Log.e("자동로그인 비번 in setting_fragment", "없다능");
            v = inflater.inflate(R.layout.activity_login_activity, container, false);
            et_id = (EditText)v.findViewById(R.id.et_id);
            et_pw = (EditText)v.findViewById(R.id.et_pw);
            bt_login = (Button)v.findViewById(R.id.bt_login);
            bt_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    st_id_for_login = et_id.getText().toString();
                    st_pw = et_pw.getText().toString();

                    loginDB lDB=new loginDB();
                    lDB.execute();
                }
            });

            bt_sign_up = (Button)v.findViewById(R.id.bt_sign_up);
            bt_sign_up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), Sign_up_activity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });

        }

        bt_FaceDetection = (Button)v.findViewById(R.id.bt_Facedetection);
        bt_FaceDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageFromCameraClick(v);
            }
        });


        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    /*    if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //로그인 하기 위해 서버랑 통신하는 클래스
    public class loginDB extends AsyncTask<Void, Integer, Void> {
        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(Void... params) {
            //아이디, 비밀번호를 전달할 파라미터 변수
            String param = "u_id=" + st_id_for_login + "&u_pw=" + st_pw;
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
                Intent intent = new Intent(getActivity(), Main2Activity.class);
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
                getActivity().finish();
            }

            //비밀번호 불일치 시 서버에서 0을 리턴한다. 서버에서 0을 리턴시 다이얼로그창을 띄운다.
            else if(st_result.equals("0")){
                Log.e("RESULT","비밀번호가 일치하지 않습니다.");
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
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
                Toast.makeText(getActivity(), "존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT).show();

            }

            //에러 발생시 알럴트 창 띄우기
            else
            {
                Log.e("Result", "에러발생! ERRORCODE = "+st_return_from_server);
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
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


    public void onImageFromCameraClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG);
            }

            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // permission not granted, initiate request
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            mCurrentPhotoPath = image.getAbsolutePath(); // save this to use in the intent

            return image;
        }

        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = new Intent(getActivity(), FaceDetectionActivity.class);
            intent.putExtra("mCurrentPhotoPath", mCurrentPhotoPath);
            startActivity(intent);
        }
    }
}
