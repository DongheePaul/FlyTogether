package com.dong.streaming.viewer;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dong.streaming.broadcaster.broadcaster_recycler_adapter;
import com.dong.streaming.broadcaster.broadcaster_recycler_item;
import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.nhancv.kurentoandroid.R;
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

@EActivity(R.layout.activity_viewer)
public class ViewerActivity extends MvpActivity<ViewerView, ViewerPresenter> implements ViewerView {
    private static final String TAG = ViewerActivity.class.getSimpleName();

    @ViewById(R.id.vGLSurfaceViewCall)
    protected SurfaceViewRenderer vGLSurfaceViewCall;

    //방송관련 메소드들.
    private EglBase rootEglBase;
    private ProxyRenderer remoteProxyRenderer;
    private Toast logToast;
    //main2activity로부터 전달받은 방송방 id
    /**
     * 아이디도 전달 받아야함.
     */
    public String room_id, user_id, room_title;

    //채팅 관련 변수, 클래스, 메소드들.
    Handler handler;
    String data;
    SocketChannel socketChannel;
    private static final String HOST = "222.239.249.149";
    private static final int PORT = 5001;
    String msg_for_hash, msg_for_chat, key;
    send_quit_msg send_quit_msg;


    //채팅 관련 리사이클러뷰, 리사이클러뷰 어뎁터, 라시이클러뷰 데이터 담은 어레이리스트.
    RecyclerView broadcasting_chatting_recyclerview;
    broadcaster_recycler_adapter broadcast_chatting_recycler_adapter;
    ArrayList<broadcaster_recycler_item> broadcaster_chatting_array = new ArrayList<>();
    EditText et_chatting;
    Button bt_chat_send;

    String id_receive_from_chatting_server,  msg_receive_from_chatting_server;

    //채팅 보내기 버튼 클릭시 채팅 메시지를 담을 변수
    String return_msg;

    //채팅 시간 구하기 위한 변수들.  chat_time = current_time - base_time
    long base_time, current_time, chat_time;

    //chat_save 클래스 관련 변수
    public String st_return_from_chat_save;
    chat_save chatSave;


    /**
     * 1. 액티비티가 시작되면 Main2Activity로부터 로그인한 유저(bj)의 아이디, 작성했던 방제목, 채팅 서버로 보낼 key("enter")를 전달 받는다.
     */
    @SuppressLint("LongLogTag")
    @AfterViews
    protected void init() {
        //Main2activity에서 보내온 값들.
        //key 값은 enter. 방번호는 전역변수 room_id, 유저 id 전역변수는 user_id.
        room_id = getIntent().getStringExtra("room_id");
        user_id = getIntent().getStringExtra("user_id");
        key = getIntent().getStringExtra("key");
        room_title = getIntent().getStringExtra("roomname");
        Log.e("방송방 제목 in ViewrActivity", room_title);

        //config peer
        remoteProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        vGLSurfaceViewCall.setEnableHardwareScaler(true);
        vGLSurfaceViewCall.setMirror(true);
        remoteProxyRenderer.setTarget(vGLSurfaceViewCall);

        presenter.initPeerConfig();
        presenter.startCall();
        //roomid 값으로 방을 찾아가는 메소드.
        presenter.send_bj_id(room_id);


        //채팅 리사이클러뷰 배치
        //방송목록을 생성할 리사이클러뷰를 생성한다.
        broadcasting_chatting_recyclerview = (RecyclerView)findViewById(R.id.broadcasting_recyclerview);
        broadcasting_chatting_recyclerview.setHasFixedSize(true);
        //레이아웃 매니저를 생성한다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //생성한 레이아웃매니저를 리사이클러뷰에 셋해준다.
        broadcasting_chatting_recyclerview.setLayoutManager(layoutManager);
        //리사이클러 어뎁터를 생성한다.
        broadcast_chatting_recycler_adapter = new broadcaster_recycler_adapter(getApplicationContext(), broadcaster_chatting_array);
        //레이아웃매니저를 셋한 리사이클러뷰에 어뎁터를 셋해준다.
        broadcasting_chatting_recyclerview.setAdapter(broadcast_chatting_recycler_adapter);
        //채팅 입력하는 에디트텍스트와 전송버튼.
        bt_chat_send = (Button)findViewById(R.id.button);
        et_chatting = (EditText)findViewById(R.id.editText);

        //채팅 관련 클래스들.
        //액티비티가 시작되면 소켓이 서버와 연결되고, 바로 key:"create"라는 메시지를 보낸다.
        //이후 send 버튼을 누르면 key:"chat"메시지를 보낸다.
        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //소켓 오픈.
                    socketChannel = SocketChannel.open();
                    Log.e("socket is Open?", String.valueOf(socketChannel.isOpen()));
                    Log.e("socketchannel is null?", String.valueOf(socketChannel));
                    //블로킹, 넌블로킹 설정인가?
                    socketChannel.configureBlocking(true);
                    //소켓 연결
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));
                    //소켓연결 확인하는거겠지?
                    Log.e("소켓연결 되었나요? ", String.valueOf(socketChannel.isConnected()));
                } catch (Exception ioe) {
                    Log.e("maybe socketchannel", ioe.getMessage() + "a");
                    ioe.printStackTrace();
                }
                //메시지를 수신하는 receive() 메소드를 수행하는 쓰레드(=checkUpdate)를 시작.
                checkUpdate.start();
                JSONObject jsonObg = new JSONObject();
                try {
                    //액티비티가 시작되면 key:create를 바로 보냄.
                    jsonObg.put("key", key);
                    jsonObg.put("roomID", room_id);
                    jsonObg.put("userid", user_id);
                    jsonObg.put("message", "액티비티 시작 후 key:enter 보낸다.");
                    msg_for_hash = jsonObg.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new SendmsgTask().execute(msg_for_hash);
            }
        }).start();

        //서버로 key:quit 메세지를 보내는 클래스
        send_quit_msg = new send_quit_msg();

        //채팅 보내는 버튼. 누르면 json형태의 메시지를 서버로 보낸다.
        bt_chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //채팅 시간을 구한다.
                chat_time = (SystemClock.elapsedRealtime() - base_time);
                Log.e("채팅 시간 in Viewer =>", String.format("%02d:%02d", chat_time / 1000 / 60, (chat_time/1000)%60));
                try {
                    return_msg = et_chatting.getText().toString();
                    JSONObject jsonObg = new JSONObject();
                    try {
                        //서버로 보내는 채팅. key : 'chat'
                        jsonObg.put("key", "chat");
                        jsonObg.put("roomID", room_id);
                        jsonObg.put("userid", user_id);
                        jsonObg.put("message", return_msg);
                        msg_for_chat = jsonObg.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //이거 서버로 메시지 보내는거 같은데? textutils는 머지?
                    if (!TextUtils.isEmpty(msg_for_chat)) {
                        Log.e("메시지 보냄 버튼 누름", msg_for_chat);
                        //메시지를 보내는 AsyncTask에 넣는다.
                        new SendmsgTask().execute(msg_for_chat);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //채팅 메시지를 chat_table에 저장하는 클래스
                chatSave = new chat_save();
                chatSave.execute();

            }
        });
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //채팅시간을 측정하기 위한 basetime. 액티비티가 실행된 시간을 저장한다.
        base_time = SystemClock.elapsedRealtime();
        Log.e("base_time in ViewerActivity",  String.valueOf(base_time));
    }

    @Override
    public void disconnect() {
        remoteProxyRenderer.setTarget(null);
        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();
            vGLSurfaceViewCall = null;
        }
        finish();
    }

    @NonNull
    @Override
    public ViewerPresenter createPresenter() {
        return new ViewerPresenter(getApplication());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        presenter.disconnect();
    }

    @Override
    public void stopCommunication() {
        onBackPressed();
    }

    @Override
    public void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    @Override
    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    @Override
    public VideoRenderer.Callbacks getRemoteProxyRenderer() {
        return remoteProxyRenderer;
    }

    /**
     * 채팅 관련된 클래스들.
     */
    //서버로 종료 메세지를 보내는 클래스
    private class send_quit_msg extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                Log.e("send quit msg: ", strings[0]);
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8")); // 서버로
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.e("채팅소켓 클로즈 in 브로드캐스터", "간다");
                        socketChannel.close();
                    } catch (IOException e) {
                        Log.e("소켓채널 클로즈", "에러발생 ");
                    }
                }
            });
        }
    }
    //서버로 메세지를 보내는 클래스
    private class SendmsgTask extends AsyncTask<String, Void, Void> {
        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(String... strings) {
            try {
                Log.e("메시지 체크 in SendmsgTask in BroadcasterActivity : ", strings[0]);
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8")); // 서버로
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //메시지가 전송 후 editText를 비움
                    et_chatting.setText("");
                }
            });
        }
    }
    @SuppressLint("LongLogTag")
    void receive() {
        while (true) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socketChannel.read(byteBuffer); //데이터받기
                Log.d("readByteCount", readByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (readByteCount == -1) {
                    throw new IOException();
                }
                byteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("UTF-8");
                data = charset.decode(byteBuffer).toString();
                Log.e("receive chat msg from netty in ViewActivity", "msg :" + data);
                //showUpdate(받은 메시지를 화면에 셋해주는 러너블)를 핸들러로 보낸다.
                handler.post(showUpdate);
            } catch (IOException e) {
                Log.d("getMsg", e.getMessage() + "");
                try {
                    socketChannel.close();
                    break;
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    private Thread checkUpdate = new Thread() {
        public void run() {
            try {
                String line;
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //여기서 리사이클러뷰에 채팅을 추가해줘야 함.
    //받은 메시지를 제이슨으로 변환 => 제이슨의 각 요소를 스트링 값으로 저장 => 스트링 값을 각각의 뷰에 넣어 리스트에 추가
    private Runnable showUpdate = new Runnable() {
        @SuppressLint("LongLogTag")
        public void run() {
            String receive =  data;
            Log.e("리시브한 메세지 확인 in ViwerActivity: ", receive);
            //binding.receiveMsgTv.setText(receive);
            try {
                    JSONObject jsonObject = new JSONObject(receive);
                    //메시지 내용
                    msg_receive_from_chatting_server = jsonObject.getString("message");
                    //메시지 보낸 유저의 id
                    id_receive_from_chatting_server = jsonObject.getString("userid");

                    //채팅메시지의 아이디와 내용을 아이템에 넣는다.
                    broadcaster_recycler_item item_b_list = new broadcaster_recycler_item(id_receive_from_chatting_server, msg_receive_from_chatting_server);
                    //리사이클러뷰에 데이터를 집어 넣은 아이템을 추가한다.
                    broadcaster_chatting_array.add(item_b_list);
                //}
            } catch (JSONException e) {
                Log.e("jsonException in Board", e.getMessage());
            }
            broadcast_chatting_recycler_adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            JSONObject jsonObg = new JSONObject();
            try {
                jsonObg.put("key", "quit");
                jsonObg.put("roomID", room_id);
                jsonObg.put("userid", user_id);
                jsonObg.put("message", "quit다" );
                msg_for_chat = jsonObg.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //이거 서버로 메시지 보내는거 같은데? textutils는 머지?
            if (!TextUtils.isEmpty(msg_for_chat)) {
                Log.e("quit 메시지 보내는거 확인", msg_for_chat);
                //메시지를 보내는 AsyncTask에 넣는다.
                send_quit_msg.execute(msg_for_chat);
            }
            //socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //db의 chat_table에 bj 아이디, 채팅 내용, 채팅이 발생한 시간을 저장하는 클래스.
    public class chat_save extends AsyncTask<Void, Integer, Void> {
        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(Void... params) {
            //아이디, vod 제목(방송방제목), 채팅시간(자료형 long), 채팅 메시지를 담는 파라미터
            String param = "u_id=" + user_id + "&vod_name=" + room_title+"&chat_time="+ chat_time+"&chat_msg="+return_msg ;
            Log.e("param sent in ViewerActivity in chat_save",param);

            try {
                //로그인에 사용되는 파일에 접근하기 위한 url값 저장
                //URL url = new URL("http://222.239.249.149/flytogether/chat_save.php");
                URL url = new URL("http://13.209.238.21/chatSaveMongo.php");
                //httpurlconnection 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //conn 설정
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                //* 안드로이드 -> 서버로 아이디, 비밀번호 값 전달 *//*
                OutputStream outs = conn.getOutputStream();
                //통신하기 위한 url 인코딩
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                //* 서버 -> 안드로이드 서버의 리턴값 전달 *//*
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
                st_return_from_chat_save = buff.toString().trim();

            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }

        //서버의 리턴값에 해당하는 이벤트 실행한다.
        //로그인 성공, 존재하지 않는 아이디, 비밀번호 불일치, 에러 발생시
        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //로그인 성공시 서버에서 1을 리턴한다. 서버에서 1을 리턴시 메인으로 이동

            if(st_return_from_chat_save.equals("1")){
                Log.e("chat_table insert succeed", "in viewer activity start");
            }

            //에러 발생시 알럴트 창 띄우기
            else
            {
                Log.e("chat_table insert failed", "에러발생! ERRORCODE = "+st_return_from_chat_save);

            }
        }
    }

}
