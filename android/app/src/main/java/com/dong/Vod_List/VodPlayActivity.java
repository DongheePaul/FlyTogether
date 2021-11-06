package com.dong.Vod_List;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.dong.streaming.broadcaster.broadcaster_recycler_adapter;
import com.dong.streaming.broadcaster.broadcaster_recycler_item;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.nhancv.kurentoandroid.R;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * vod 스트리밍 하는 액티비티.
 * VodList_fragment에서 선택한 vod 제목으로 서버(라이트세일)에 저장된 m3u8 파일을 exoplayer로 스트리밍한다.
 */
public class VodPlayActivity extends AppCompatActivity {
    //vod 목록에서 전달 받은 vod 제목
    String vod_title;
    //exoplayer 관련 변수들
    PlayerView playerView;
    ProgressBar loading;
    SimpleExoPlayer player;

    //채팅 로드 관련 변수들.
    chat_load chatLoad;
    String st_return_from_chat_load, vod_Title, user_id, chat_msg;
    long chat_time;

    //vod 재생시간
    long duration, duration_in_thread;

    //오리엔테이션 변경 및 활동 중지에 플레이어 상태를 저장하는 데 사용하도록 초기화 된 몇 가지 변수가 필요합니다.
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    private TimerTask second;
    private final Handler handler1 = new Handler();
    int timer_sec, count;

    //채팅 관련 리사이클러뷰, 리사이클러뷰 어뎁터, 라시이클러뷰 데이터 담은 어레이리스트.
    RecyclerView vod_chatting_recyclerview;
    broadcaster_recycler_adapter vod_chatting_recycler_adapter;
    ArrayList<broadcaster_recycler_item> vod_chatting_array = new ArrayList<>();
    @Override
    protected void onStart() {
        super.onStart();

        //--------------------------------------
        //Creating default track selector
        //and init the player
        TrackSelection.Factory adaptiveTrackSelection = new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(getApplicationContext()),
                new DefaultTrackSelector(adaptiveTrackSelection),
                new DefaultLoadControl());

        //init the player
        playerView.setPlayer(player);

        //-------------------------------------------------
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Exo2"), defaultBandwidthMeter);
        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        //-----------------------------------------------
        //Create media source


        String hls_url = "http://13.209.238.21/flytogetherVideo/"+vod_title+".mp4";
        Uri uri = Uri.parse(hls_url);
        Handler mainHandler = new Handler();
        /*DashMediaSource mediaSource = new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);*/
        MediaSource mediaSource = new ExtractorMediaSource(uri,
                dataSourceFactory, extractorsFactory, null, null);

        player.prepare(mediaSource);


        player.setPlayWhenReady(playWhenReady);
        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }
            @Override
            public void onLoadingChanged(boolean isLoading) {

            }
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case ExoPlayer.STATE_READY:
                        loading.setVisibility(View.GONE);
                        duration = player.getCurrentPosition();
                        Log.e("이게 플레이시간?", String.valueOf(duration));
                        break;
                    case ExoPlayer.STATE_BUFFERING:
                        loading.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(mediaSource, true, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_play);
        vod_title = getIntent().getStringExtra("vod_title");
        Log.e("vod_title 확인 ==", vod_title);
        playerView = (PlayerView)findViewById(R.id.video_view);
        loading = (ProgressBar) findViewById(R.id.loading);

        //채팅 리사이클러뷰 배치
        //방송목록을 생성할 리사이클러뷰를 생성한다.
        vod_chatting_recyclerview = (RecyclerView)findViewById(R.id.vod_play_recycler);
        vod_chatting_recyclerview.setHasFixedSize(true);
        //레이아웃 매니저를 생성한다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //생성한 레이아웃매니저를 리사이클러뷰에 셋해준다.
        vod_chatting_recyclerview.setLayoutManager(layoutManager);
        //리사이클러 어뎁터를 생성한다.
        vod_chatting_recycler_adapter = new broadcaster_recycler_adapter(getApplicationContext(), vod_chatting_array);
        //레이아웃매니저를 셋한 리사이클러뷰에 어뎁터를 셋해준다.
        vod_chatting_recyclerview.setAdapter(vod_chatting_recycler_adapter);


        //해당 vod의 채팅을 모두 불러온다
        chatLoad = new chat_load();
        chatLoad.execute();


    }

    //1초마다 모든 채팅의 발생시간들을 현재 재생시간과 비교해서
    public void testStart() {
        timer_sec = 0;
        count = 0;
        Timer timer = new Timer();
        second = new TimerTask() {
            @SuppressLint("LongLogTag")
            @Override
            public void run() {
                vod_chatting_array.clear();
                //1초마다 모든 채팅의 채팅시간을 현재 vod 플레이타임과 비교해   플레이타임 == 채팅시간 일때 어레이리스트 아이템 추가해주기.
                try {
                    JSONArray obj = new JSONArray(st_return_from_chat_load);
                    for (int i = 0; i < obj.length(); i++) {
                        //JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONObject jsonObject = obj.getJSONObject(i);
                        //게시물의 글번호
                        vod_Title = jsonObject.getString("vodtitle");
                        //게시물의 제목
                        user_id = jsonObject.getString("userid");
                        //한 페이지의 글들 중 가장 작은 글 번호. 다음 페이지의 글들을 불러오기 위해 필요하다.
                        chat_msg = jsonObject.getString("chatmsg");
                        chat_time = jsonObject.getLong("chattime");
                        if(Long.valueOf(duration_in_thread )!= null) {
                            duration_in_thread = player.getCurrentPosition();
                        }
                        //채팅 발생시간 < vod 재생시간이면 해당 채팅의 내용과 id를 리사이클러뷰에 추가시킨다.
                        if( duration_in_thread > chat_time) {
                            Log.e("duration > chat_time", "와따리");
                            broadcaster_recycler_item vod_chatting_recycler_item = new broadcaster_recycler_item(user_id, chat_msg);
                            vod_chatting_array.add(vod_chatting_recycler_item);

                        }
                    }

                    Update();
                } catch (JSONException e) {
                    Log.e("jsonException in Board", e.getMessage());
                }

            }
        };
        //1초(1000ms)마다 second를 실행하라.
        timer.schedule(second, 0, 1000);
    }
    protected void Update() {
        Runnable updater = new Runnable() {
            public void run() {

                vod_chatting_recycler_adapter.notifyDataSetChanged();
            }
        };
        handler1.post(updater);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    //db의 chat_table에 bj 아이디, 채팅 내용, 채팅이 발생한 시간을 불러오는 클래스
    public class chat_load extends AsyncTask<Void, Integer, Void> {
        @SuppressLint("LongLogTag")
        @Override
        protected Void doInBackground(Void... params) {
            //아이디, vod 제목(방송방제목), 채팅시간(자료형 long), 채팅 메시지를 담는 파라미터
            String param = "vod_name=" + vod_title ;
            Log.e("param sent in VodPlayActivity in chat_load",param);

            try {
                //로그인에 사용되는 파일에 접근하기 위한 url값 저장
                //URL url = new URL("http://222.239.249.149/flytogether/chat_list.php");
                URL url = new URL("http://13.209.238.21/chatLoadMongo.php");
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
                st_return_from_chat_load = buff.toString().trim();

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
            try {
                Log.e("VodPlayActivity에서 서버 리턴값", st_return_from_chat_load);
                //JSONArray jsonArray = new JSONArray(s);
                JSONArray obj = new JSONArray(st_return_from_chat_load);
                Log.e("obj 확인", String.valueOf(obj.length()));
                for (int i = 0; i < obj.length(); i++) {
                    //JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject jsonObject = obj.getJSONObject(i);
                    //게시물의 글번호
                    vod_Title = jsonObject.getString("vodtitle");
                    //게시물의 제목
                    user_id = jsonObject.getString("userid");
                    //한 페이지의 글들 중 가장 작은 글 번호. 다음 페이지의 글들을 불러오기 위해 필요하다.
                    chat_msg = jsonObject.getString("chatmsg");
                    chat_time = jsonObject.getLong("chattime");
                    Log.e("chatmsg check in VodPlayActivity", String.valueOf(vod_Title));
                    Log.e("chatmsg check in VodPlayActivity", String.valueOf(user_id));
                    Log.e("chatmsg check in VodPlayActivity", String.valueOf(chat_time));
                    //게시물의 글번호, 제목, 시간, 가장 작은 글번호(invisible)을 아이템에 넣는다.
                   // b_list_item item_b_list = new b_list_item(st_roomID, st_room_name, st_bj_name);

                    //리사이클러뷰에 데이터를 집어 넣은 아이템을 추가한다.
                    //b_list_items_array.add(item_b_list);
                }
            } catch (JSONException e) {
                Log.e("jsonException in Board", e.getMessage());
            }
            //b_list_recycler_adapter.notifyDataSetChanged();
            //progressDialog.dismiss();
            testStart();
        }
    }

}
