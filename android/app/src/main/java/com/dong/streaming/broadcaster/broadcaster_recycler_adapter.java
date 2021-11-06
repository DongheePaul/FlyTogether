package com.dong.streaming.broadcaster;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhancv.kurentoandroid.R;

import java.util.ArrayList;

public class broadcaster_recycler_adapter extends RecyclerView.Adapter<broadcaster_recycler_adapter.ViewHolder> {
    Context context;
    ArrayList<broadcaster_recycler_item> b_chatting_items_array = new ArrayList<>();


    //생성자
    public broadcaster_recycler_adapter (Context context, ArrayList<broadcaster_recycler_item> b_chatting_items_array) {
        this.context = context;
        this.b_chatting_items_array = b_chatting_items_array;
    }

    @NonNull
    /**
     * 필수 메소드 1: 새로운 뷰 생성.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.broadcasting_chatting_item,parent,false);
        Log.e("채팅 oncreateViewHolder", "들어와쩌염 뿌우");
        return new ViewHolder(view);
    }

    /**
     * 필수 메소드 2: 새로운 뷰에 데이터 셋해주는 메소드.
     */
    //재활용 되는 뷰가 호출하여 실행되는 메소드, 뷰 홀더를 전달하고 어댑터는 position 의 데이터를 결합시킵니다.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //데이터를 담을 리스트의 포지션값을 가져온다.
        broadcaster_recycler_item b_chatting_item = b_chatting_items_array.get(position);
        //입력된 데이터를 아이템의 각 항목에 셋해준다.
        holder.tv_content.setText(b_chatting_item.getMessage());
        holder.tv_id.setText(b_chatting_item.getId());
        Log.e("채팅 뷰가 만들어지고 있나요? : ", b_chatting_item.getMessage());
        Log.e("포지션값 확인 : ", b_chatting_item.getId());
    }

    @Override
    public int getItemCount() {
        return b_chatting_items_array.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_id, tv_content, tv_room_id;
        public ViewHolder(View itemView) {
            super(itemView);
            tv_room_id = (TextView) itemView.findViewById(R.id.tv_room_id);
            tv_id = (TextView) itemView.findViewById(R.id.tv_id);
            tv_content = (TextView)itemView.findViewById(R.id.tv_content);
        }
    }

}
