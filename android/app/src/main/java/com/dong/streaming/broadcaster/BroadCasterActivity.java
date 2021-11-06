package com.dong.streaming.broadcaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
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

import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.nhancv.kurentoandroid.R;
import com.nhancv.npermission.NPermission;
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
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

/**
 *  bj 화면 액티비티.
 *  -방송 기능 흐름.
 *  1. 액티비티가 시작되면 Main2Activity로부터 로그인한 유저(bj)의 아이디, 작성했던 방제목를 전달 받는다.
 *  2. 서버(222.239.249.149)로 유저의 아이디를 쿠렌토 미디어 서버로 전송한다.  => create_streaming_room 클래스.
 *     2-1 쿠렌토서버는 FlyTogether 데이터베이스의 streaming_room 테이블에 유저의 아이디, 방제목을 삽입한다.
 *     2-2 유저 아이디, 방제목 삽입이 완료되면 서버는 방의 인덱스(streaming_room에서 컬럼명 = ROOM_INDEX, 이 자바 파일에선 = st_roomid)를 클라이언트(bj)로 보낸다.
 *  3. 클라이언트(bj)는 방 인덱스 번호를 받으면 쿠렌토 서버로 접속한다. ( presenter.startCall() )
 *     3-1. 접속이 이루어지면 2-2에서 받은 방의 인덱스번호를 쿠렌토 서버로 보낸다 (presenter.send_bj_id(st_roomid))
 *     3-2. 쿠렌토서버는 bj 배열(presenter[])에  3-1에서 받은 방의 인덱스 번호로 presenter 생성.
 *          이후 시청자가 쿠렌토 서버로 들어오면, bj 배열에서 3-1에서 받은 방 인덱스 번호의 presenter와 연결시켜준다.
 *
 *  -채팅 기능 흐름.
 *  1. 액티비티가 시작되면 Main2Activity로부터 로그인한 유저(bj)의 아이디, 작성했던 방제목를 전달 받는다.
 *  2. "create_streaming_room" 클래스의 결과값으로 방번호를 받는다. 그리고 jsonObg.put("key", key), jsonObg.put("userid", user_id), jsonObg.put("roomID", st_roomid); 라는 제이슨을 네티 서버로 보내고, 소켓을 연결한다.
 *  3. 네티 서버는 해당 방번호로 해시맵을 만들고 클라이언트의 소켓을 넣는다.
 *  4. 네티 서버는 같은 방에 있는 클라이언트들의 소켓에 메시지를 전송한다.
 *
 *  - vod 기능 흐름.
 *  1. 액티비티가 시작되면 send_bj_id(st_roomid, st_streaming_room_name, user_id) 를 통해 쿠렌토서버로 방번호, 방제목, bj의 ID를 보낸다.
 *  2. 쿠렌토 서버는 해당 endpoint(클라이언트)로부터 오는 영상을 저장(recorderEndpoint.record()) 한다. 파일명은 방제목.mp4이다.
 *  3. 액티비티가 종료되면 서버의 db(222.239.249.149)의  vod_table 테이블에 파일명과 bj 이름을 저장한다.  => save_vod_name 클래스
 */

@EActivity(R.layout.activity_broadcaster)
public class BroadCasterActivity extends MvpActivity<BroadCasterView, BroadCasterPresenter>
        implements BroadCasterView, NPermission.OnPermissionResult {
    private static final String TAG = BroadCasterActivity.class.getSimpleName();

    @ViewById(R.id.vGLSurfaceViewCall)
    protected SurfaceViewRenderer vGLSurfaceViewCall;

    //Main2Activity-> MainActivity에서 넘어온 로그인한 유저의 id. 방송방 제목
    public String user_id, st_streaming_room_name;

    //방송 관련 메소드들.
    private NPermission nPermission;
    private EglBase rootEglBase;
    private ProxyRenderer localProxyRenderer;
    private Toast logToast;
    private boolean isGranted;

    //db의 streaming_room 테이블에 방송방 생성하는 클래스.
    create_streaming_room createstreamingroom;
    //create_streaming_room 관련된 스트링 전역변수들.
    public String st_return_from_server, st_result, st_roomid, st_bj_name;

    //save_vod_name 클래스 관련 변수들
    public String st_return_from_vod_list;
    save_vod_name saveVodName;

    //채팅 시간 구하기 위한 변수들.  chat_time = current_time - base_time
    long base_time, current_time, chat_time;

    //chat_save 클래스 관련 변수
    public String st_return_from_chat_save;
    chat_save chatSave;

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
    String id_receive_from_chatting_server, msg_receive_from_chatting_server;
    //채팅 보내는 버튼 눌렀을때, 채팅메세지가 저장되는 스트링
    String return_msg;

    @SuppressLint("LongLogTag")
    @AfterViews
    protected void init() {
        /**
 * 1. 액티비티가 시작되면 Main2Activity로부터 로그인한 유저(bj)의 아이디, 작성했던 방제목, 채팅 서버로 보낼 key("create")를 전달 받는다.
 */
        //Main2activity에서 보내온 값들.
        //key 값은 create. 방번호는 전역변수 st_streaming_room_name, 유저 id 전역변수는 user_id.
         user_id = getIntent().getStringExtra("user_id");
        st_streaming_room_name = getIntent().getStringExtra("streaming_room_name");
        key = getIntent().getStringExtra("key");
        Log.e("broadcasteractivity", "got room_name: " + st_streaming_room_name);
        Log.e("broadcasteractivity", "got user_id: " + user_id);

/**
 *  2. 서버로 유저의 아이디를 전송한다.
 */
        createstreamingroom = new create_streaming_room();

        //액티비티 종료 때, vod 파일명과 bj 이름을 서버 db의 vod_table에 삽입하는 클래스
        saveVodName = new save_vod_name();

        //권한 요청.
        nPermission = new NPermission(true);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        //config peer
        localProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();
        //방송화면(serfaceview) 관련 설정 + 시작.
        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        vGLSurfaceViewCall.setEnableHardwareScaler(true);
        vGLSurfaceViewCall.setMirror(true);
        localProxyRenderer.setTarget(vGLSurfaceViewCall);
        //peerconnection 시작... 아마도
        presenter.initPeerConfig();

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

        bt_chat_send = (Button)findViewById(R.id.button);
        et_chatting = (EditText)findViewById(R.id.editText);

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
                Log.e("채팅 시간 =>", String.format("%02d:%02d", chat_time / 1000 / 60, (chat_time/1000)%60));
                //네티 서버로 보내는 채팅.
                try {
                    return_msg = et_chatting.getText().toString();
                    JSONObject jsonObg = new JSONObject();
                    try {
                        //서버로 보내는 채팅. key : 'chat'
                        jsonObg.put("key", "chat");
                        jsonObg.put("roomID", st_roomid);
                        jsonObg.put("userid", user_id);
                        jsonObg.put("message", return_msg);
                        msg_for_chat = jsonObg.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (!TextUtils.isEmpty(msg_for_chat)) {
                        Log.e("메시지 보냄 버튼 누름", msg_for_chat);
                        //메시지를 보내는 AsyncTask에 넣는다.
                        new SendmsgTask().execute(msg_for_chat);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //네티 서버로 보내는 채팅. 끝

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
        Log.e("base_time in broadcasteractivity",  String.valueOf(base_time));
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < 23 || isGranted) {
            //presenter.startCall();
            createstreamingroom.execute();
        } else {
            nPermission.requestPermission(this, Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //방종하면 쿠렌토 서버로 방번호와 방제목을 보낸다. 그리고 쿠렌토 서버는 파일명으로 m3u8을 생성하고, 방번호로 recordEndpoint를 종료시킨다. 이후 세션도 종료시킴.
        presenter.send_bj_stop(st_roomid, st_streaming_room_name);
        presenter.disconnect();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        nPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public void onPermissionResult(String permission, boolean isGranted) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                this.isGranted = isGranted;
                if (!isGranted) {
                    nPermission.requestPermission(this, Manifest.permission.CAMERA);
                } else {
                    //nPermission.requestPermission(this, Manifest.permission.RECORD_AUDIO);
                    //1. 여기서 화면이 까매짐.
                    //presenter.startCall();
                    createstreamingroom.execute();
                }
                break;
            default:
                break;
        }
    }
    @NonNull
    @Override
    public BroadCasterPresenter createPresenter() {
        return new BroadCasterPresenter(getApplication());
    }
    @Override
    public void disconnect() {
        //kurento client 종료
        localProxyRenderer.setTarget(null);
        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();
            vGLSurfaceViewCall = null;
        }
        delete_streaming_room deleteStreamingRoom = new delete_streaming_room();
        deleteStreamingRoom.execute();
        finish();
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
    public VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            if (!captureToTexture()) {
                return null;
            }
            //4.
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));

        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            return null;
        }
        return videoCapturer;
    }

    @Override
    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    @Override
    public VideoRenderer.Callbacks getLocalProxyRenderer() {
        //이걸로 iceOncadidate가 이뤄지느듯.
        return localProxyRenderer;
    }

    private boolean useCamera2() {
        //2. 서버 로그 :  Connection received with sessionID 10
        presenter.send_bj_id(st_roomid, st_streaming_room_name, user_id);
        return Camera2Enumerator.isSupported(this) && presenter.getDefaultConfig().isUseCamera2();
    }

    private boolean captureToTexture() {
        //3.
        return presenter.getDefaultConfig().isCaptureToTexture();
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
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
                Log.e("receive chat msg from netty", "msg :" + data);
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
            Log.e("리시브한 메세지 확인 in broadcastAcitivy: ", receive);
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
                broadcast_chatting_recycler_adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e("jsonException in Board", e.getMessage());
            }
            broadcast_chatting_recycler_adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveVodName.execute();
        try {
            JSONObject jsonObg = new JSONObject();
            try {
                jsonObg.put("key", "quit");
                jsonObg.put("roomID", st_roomid);
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

    /**
     * 2-1 서버는 FlyTogether 데이터베이스의 streaming_room 테이블에 유저의 아이디, 방제목을 삽입한다.
     * 2-2 유저 아이디, 방제목 삽입이 완료되면 서버는 방의 인덱스(ROOM_INDEX)를 클라이언트(bj)로 보낸다.
     */
    public class create_streaming_room extends AsyncTask<Void, Integer, Void> {
        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(Void... params) {
            //아이디, 비밀번호를 전달할 파라미터 변수
            String param = "u_id=" + user_id + "&room_name=" + st_streaming_room_name;
            Log.e("param sent in BroadCasterActivity",param);
            try {
                //로그인에 사용되는 파일에 접근하기 위한 url값 저장
                URL url = new URL("http://222.239.249.149/flytogether/create_streaming_room.php");
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
                st_return_from_server = buff.toString().trim();

                //* 서버에서 한 응답을 보여주는 로그 *//*
                Log.e("server return check in LoginActivity", st_return_from_server);
                try {
                    JSONObject jsonObject = new JSONObject(st_return_from_server);
                    st_result = jsonObject.getString("result");
                    st_roomid = jsonObject.getString("id");
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
                if(st_result.equals("1"))
                {
                    Log.e("create streaming room in db","성공적으로 처리되었습니다!");
                }
                else
                {
                    Log.e("create streaming room in db result","create streaming room  ERRCODE = " + st_return_from_server);
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
                Log.e("server return succeed", "presneter start");
                presenter.startCall();
                JSONObject jsonObg = new JSONObject();
                try {
                    //액티비티가 시작되면 key:create를 바로 보냄.
                    jsonObg.put("key", key);
                    jsonObg.put("roomID", st_roomid);
                    jsonObg.put("userid", user_id);
                    jsonObg.put("message", "액티비티 시작 후 key:create 보낸다.");
                    msg_for_hash = jsonObg.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new SendmsgTask().execute(msg_for_hash);
            }

            //에러 발생시 알럴트 창 띄우기
            else
            {
                Log.e("Result", "에러발생! ERRORCODE = "+st_return_from_server);

            }

        }
    }

    //db의 streaming_room 테이블과 통신하는 클래스.  화면을 종료하면 실행된다.
    // streaming_room의 BJ_ID, ROOM_NAME에  로그인한 유저의 아이디와 Main2Activity에서 입력한 방제목을 삽입한다.
    public class delete_streaming_room extends AsyncTask<Void, Integer, Void> {
        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(Void... params) {
            //아이디, 비밀번호를 전달할 파라미터 변수
            String param = "u_id=" + user_id;
            Log.e("param sent in broadcasteractivity",param);

            try {
                //로그인에 사용되는 파일에 접근하기 위한 url값 저장
                URL url = new URL("http://222.239.249.149/flytogether/delete_streaming_room.php");
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
                st_return_from_server = buff.toString().trim();

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

            if(st_return_from_server.equals("1")){
                Log.e("streaming room delete succeed", "presneter start");
            }

            //에러 발생시 알럴트 창 띄우기
            else
            {
                Log.e("Result", "에러발생! ERRORCODE = "+st_return_from_server);

            }
        }
    }

    //db의 vod_table 테이블에 vod 파일명과 bj 이름을 저장한다.
    public class save_vod_name extends AsyncTask<Void, Integer, Void> {
        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(Void... params) {
            //아이디, 비밀번호를 전달할 파라미터 변수
            String param = "u_id=" + user_id + "&vod_name=" + st_streaming_room_name+"&thumbnail="+st_streaming_room_name+"-thumbnail.png" ;
            Log.e("param sent in broadcasteractivity in save_vod_name",param);

            try {
                //로그인에 사용되는 파일에 접근하기 위한 url값 저장
                URL url = new URL("http://222.239.249.149/flytogether/vod_table_insert.php");
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
                st_return_from_vod_list = buff.toString().trim();

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

            if(st_return_from_vod_list.equals("1")){
                Log.e("vod_table insert succeed", "presneter start");
            }

            //에러 발생시 알럴트 창 띄우기
            else
            {
                Log.e("vod_table insert failed", "에러발생! ERRORCODE = "+st_return_from_vod_list);

            }
        }
    }

    //db의 chat_table에 bj 아이디, 채팅 내용, 채팅이 발생한 시간을 저장하는 클래스.
    public class chat_save extends AsyncTask<Void, Integer, Void> {
        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(Void... params) {
            //아이디, vod 제목(방송방제목), 채팅시간(자료형 long), 채팅 메시지를 담는 파라미터
            String param = "u_id=" + user_id + "&vod_name=" + st_streaming_room_name+"&chat_time="+ chat_time+"&chat_msg="+return_msg ;
            Log.e("param sent in broadcasteractivity in chat_save",param);

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
                Log.e("st_return_from_chat_save", "in Broadcaster");
            }

            //에러 발생시 알럴트 창 띄우기
            else
            {
                Log.e("chat_table insert failed", "에러발생! ERRORCODE = "+st_return_from_chat_save);

            }
        }
    }

}
