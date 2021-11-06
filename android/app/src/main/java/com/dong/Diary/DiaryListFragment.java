package com.dong.Diary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dong.PersonalData_Login_SignUP.Login_activity;
import com.nhancv.kurentoandroid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DiaryListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DiaryListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DiaryListFragment extends Fragment {
    //로그인된 아이디, 이름
    String st_id;
    String st_name;
    String vod_id, vod_bjID, vod_title, vod_thumbnailTitle;

    String result_from_v_list;
    //자동로그인용 sharedpreference
    SharedPreferences autoLogin;
    SharedPreferences.Editor editor;
    //리사이클러뷰, 리사이클러뷰 어뎁터, 라시이클러뷰 데이터 담은 어레이리스트.
    RecyclerView diarylist_recyclerview;
    DiaryList_recycler_adapter diaryList_recycler_adapter;
    ArrayList<DiaryList_item> diaryList_itemArrayList = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public DiaryListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DiaryListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DiaryListFragment newInstance(String param1, String param2) {
        DiaryListFragment fragment = new DiaryListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_diary_list, container, false);
        //자동로그인용 sharedpreference 파일.
        autoLogin = this.getActivity().getSharedPreferences("autoLogin",Activity.MODE_PRIVATE);
        st_id = autoLogin.getString("id", "");
        st_name = autoLogin.getString("name", "");
        //자동로그인용 id, 비번이 저장되어 있는지 확인.
        if(!st_name.equals("")){
            Log.e("현재 저장된 자동로그인 아이디 in DiaryListFragment", st_id);
            Log.e("현재 저장된 자동로그인 비번 in DiaryListFragment", st_name);
        }

        Button bt_go_to_WriteDiaryActivity = (Button)v.findViewById(R.id.bt_write_diary);
        bt_go_to_WriteDiaryActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WriteDiaryActivity.class);
                intent.putExtra("user_id", st_id);
                startActivity(intent);
                getActivity().finish();

            }
        });
        //방송목록을 생성할 리사이클러뷰를 생성한다.
        diarylist_recyclerview = (RecyclerView)v.findViewById(R.id.diary_recycler);
        diarylist_recyclerview.setHasFixedSize(true);
        //레이아웃 매니저를 생성한다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //생성한 레이아웃매니저를 리사이클러뷰에 셋해준다.
        diarylist_recyclerview.setLayoutManager(layoutManager);
        //리사이클러 어뎁터를 생성한다.
        diaryList_recycler_adapter = new DiaryList_recycler_adapter(getActivity(), diaryList_itemArrayList);
        //레이아웃매니저를 셋한 리사이클러뷰에 어뎁터를 셋해준다.
        diarylist_recyclerview.setAdapter(diaryList_recycler_adapter);

        //vod 정보를 불러와 vod 목록을 생성하는 클래스를 실행한다.
        load_data_for_diary_list load_data_for_diary_list = new load_data_for_diary_list();
        load_data_for_diary_list.execute();

        Button bt_login = (Button)v.findViewById(R.id.bt_LoginInDiaryList);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Login_activity.class);
                startActivity(intent);
                getActivity().finish();
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    //db에 저장된 vod에 대한 정보(인덱스, 제목, bj이름, 썸네일경로)를 불러와 리아시클러뷰에 셋해주는 클래스.
    public class load_data_for_diary_list extends AsyncTask<String, String, String> {
        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... params) {
            try{
                URL url = new URL("http://222.239.249.149/flytogether/diary_list.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    InputStream is = null;
                    BufferedReader in = null;
                    result_from_v_list = "";

                    //서버로부터 받은 인풋스트림을 스트링화
                    is = conn.getInputStream();
                    in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                    String line = null;
                    StringBuffer buff = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        buff.append(line + "\n");
                    }
                    //서버로부터의 응답(목록을 생성할 게시물 데이터.)
                    result_from_v_list = buff.toString().trim();
                }
                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //리턴하면 onPostExecute의 파라미터로 전달된다.
            return result_from_v_list;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            diaryList_itemArrayList.clear();
            try {
                Log.e("VodList에서 서버로부터 온 리턴값", s);
                JSONArray obj = new JSONArray(s);
                for (int i = 0; i < obj.length(); i++) {
                    JSONObject jsonObject = obj.getJSONObject(i);
                    //vod 인덱스
                    Log.e("jsonObject.getString ('vodid')", jsonObject.getString("vod_id"));
                    vod_id = jsonObject.getString("vod_id");
                    //vod 작성자(bj)의 아이디
                    vod_bjID = jsonObject.getString("bj_id");
                    //vod 제목
                    vod_title = jsonObject.getString("vod_title");
                    //vod 썸네일 이름
                    vod_thumbnailTitle = jsonObject.getString("vod_thumbnail");
                    //게시물의 글번호, 제목, 시간, 가장 작은 글번호(invisible)을 아이템에 넣는다.
                    DiaryList_item item_v_list = new DiaryList_item(vod_id, vod_bjID, vod_thumbnailTitle, vod_title);

                    //리사이클러뷰에 데이터를 집어 넣은 아이템을 추가한다.
                    diaryList_itemArrayList.add(item_v_list);
                }
            } catch (JSONException e) {
                Log.e("jsonException in v_list", e.getMessage());
            }
            diaryList_recycler_adapter.notifyDataSetChanged();
        }
    }
}
