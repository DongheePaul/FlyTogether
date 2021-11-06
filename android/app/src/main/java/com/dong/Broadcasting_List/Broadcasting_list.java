package com.dong.Broadcasting_List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
 * {@link Broadcasting_list.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Broadcasting_list#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Broadcasting_list extends Fragment {

    //로그인된 아이디, 이름
    public static String st_id;
    String st_name;

    //방송 중 리스트
    String result_from_b_list;
    String st_roomID;
    String st_bj_name;
    String st_room_name;

    //자동로그인용 sharedpreference
    SharedPreferences autoLogin;
    SharedPreferences.Editor editor;

    //리사이클러뷰, 리사이클러뷰 어뎁터, 라시이클러뷰 데이터 담은 어레이리스트.
    RecyclerView b_list_recyclerview;
    b_list_recycler_adapter b_list_recycler_adapter;
    ArrayList<b_list_item> b_list_items_array = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public Broadcasting_list() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Broadcasting_list.
     */
    // TODO: Rename and change types and number of parameters
    public static Broadcasting_list newInstance(String param1, String param2) {
        Broadcasting_list fragment = new Broadcasting_list();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_broadcasting_list, container, false);
        //자동로그인용 sharedpreference 파일.
        autoLogin = this.getActivity().getSharedPreferences("autoLogin",Activity.MODE_PRIVATE);
        st_id = autoLogin.getString("id", "");
        st_name = autoLogin.getString("name", "");
        //자동로그인용 id, 비번이 저장되어 있는지 확인.
        if(!st_name.equals("")){
            Log.e("현재 저장된 자동로그인 아이디 in Broadcasting_list", st_id);
            Log.e("현재 저장된 자동로그인 비번 in Broadcasting_list", st_name);
        }

        //방송목록을 생성할 리사이클러뷰를 생성한다.
        b_list_recyclerview = (RecyclerView)v.findViewById(R.id.broadcasting_list_recyclerview);
        b_list_recyclerview.setHasFixedSize(true);
        //레이아웃 매니저를 생성한다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //생성한 레이아웃매니저를 리사이클러뷰에 셋해준다.
        b_list_recyclerview.setLayoutManager(layoutManager);
        //리사이클러 어뎁터를 생성한다.
        b_list_recycler_adapter = new b_list_recycler_adapter(getActivity(), b_list_items_array);
        //레이아웃매니저를 셋한 리사이클러뷰에 어뎁터를 셋해준다.
        b_list_recyclerview.setAdapter(b_list_recycler_adapter);

        //현재 방송중인 방의 정보를 불러와 방송목록을 생성하는 클래스를 실행한다.
        load_data_for_broadcasting_list load_data_for_broadcasting_list = new load_data_for_broadcasting_list();
        load_data_for_broadcasting_list.execute();

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
      /*  if (context instanceof OnFragmentInteractionListener) {
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
    public class load_data_for_broadcasting_list extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try{
                URL url = new URL("http://222.239.249.149/flytogether/broadcasting_list.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    Log.e("http response==", String.valueOf(conn.getResponseCode()));
                    /* 서버 -> 안드로이드 서버의 리턴값 전달 */
                    InputStream is = null;
                    BufferedReader in = null;
                    result_from_b_list = "";

                    //서버로부터 받은 인풋스트림을 스트링화
                    is = conn.getInputStream();
                    in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                    String line = null;
                    StringBuffer buff = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        buff.append(line + "\n");
                    }

                    //서버로부터의 응답(목록을 생성할 게시물 데이터.)
                    result_from_b_list = buff.toString().trim();

                }

                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //리턴하면 onPostExecute의 파라미터로 전달된다.
            return result_from_b_list;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
           // progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            b_list_items_array.clear();
            try {
                Log.e("Broadcasting에서 서버 리턴값", s);
                //JSONArray jsonArray = new JSONArray(s);
                JSONArray obj = new JSONArray(s);
                Log.e("obj 확인", String.valueOf(obj.length()));
               for (int i = 0; i < obj.length(); i++) {
                    //JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject jsonObject = obj.getJSONObject(i);
                    //게시물의 글번호
                    st_roomID = jsonObject.getString("room_index");
                    //게시물의 제목
                    st_bj_name = jsonObject.getString("bj_id");
                    //한 페이지의 글들 중 가장 작은 글 번호. 다음 페이지의 글들을 불러오기 위해 필요하다.
                    st_room_name = jsonObject.getString("room_name");

                    //게시물의 글번호, 제목, 시간, 가장 작은 글번호(invisible)을 아이템에 넣는다.
                    b_list_item item_b_list = new b_list_item(st_roomID, st_room_name, st_bj_name);

                    //리사이클러뷰에 데이터를 집어 넣은 아이템을 추가한다.
                     b_list_items_array.add(item_b_list);
                }
            } catch (JSONException e) {
                Log.e("jsonException in Board", e.getMessage());
            }
            b_list_recycler_adapter.notifyDataSetChanged();
            //progressDialog.dismiss();
        }
    }



}
