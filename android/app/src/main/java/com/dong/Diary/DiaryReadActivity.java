package com.dong.Diary;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nhancv.kurentoandroid.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiaryReadActivity extends AppCompatActivity {
TextView TV_title, TV_content, TV_emotion;
ImageView IV_picture;
String id, result, title, content, emotion, img_dir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_read);

        id = getIntent().getStringExtra("id");
        Log.e("다이어리 id", id);

        TV_title = (TextView)findViewById(R.id.tv_for_title);
        TV_content = (TextView)findViewById(R.id.tv_for_content);
        TV_emotion = (TextView)findViewById(R.id.tv_ShowEmotion);
        IV_picture = (ImageView)findViewById(R.id.iv_DiaryImage);
        BoardImage boardImage = new BoardImage();
        boardImage.execute();
    }

    //서버로부터 유저의 프로필 사진을 불러와서 이미지뷰에 뿌리는 클래스.
    public class BoardImage extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String param = "board_id="+ id;
            try{
                URL url = new URL("http://222.239.249.149/flytogether/diary_read.php");
                //httpurlconnection 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //conn 설정
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                //클라이언트에서 서버로 보내는 회원정보
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
                result = buff.toString().trim();

                Log.e("diary_read", result);
                JSONObject jsonObject = new JSONObject(result);
                title = jsonObject.getString("title");
                emotion = jsonObject.getString("time");
                img_dir = jsonObject.getString("image");
                content = jsonObject.getString("content");

            }
            catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        //이미지 뷰에 유저의 프로필 사진 뿌려준다.
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //로그인한 유저와 작성자가 같지 않다면 수정, 삭제버튼이 보이지 않는다.

            TV_title.setText(title);
            TV_content.setText(content);
            TV_emotion.setText(emotion);

            Glide.with(DiaryReadActivity.this).load("http://222.239.249.149/"+img_dir).into(IV_picture);
        }
    }

}
