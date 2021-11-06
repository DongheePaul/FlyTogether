package com.dong.Broadcasting_List;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dong.streaming.viewer.ViewerActivity_;
import com.nhancv.kurentoandroid.R;

import java.util.ArrayList;



public class b_list_recycler_adapter extends RecyclerView.Adapter<b_list_recycler_adapter.ViewHolder> {
    Context context;
    ArrayList<b_list_item> b_list_items_array = new ArrayList<>();
    b_list_item b_list_item;
    Broadcasting_list broadcasting_list;

    //생성자
    public b_list_recycler_adapter(Context context, ArrayList<b_list_item> b_list_items_array) {
        this.context = context;
        this.b_list_items_array = b_list_items_array;
    }

    @NonNull
    /**
     * 필수 메소드 1: 새로운 뷰 생성.
     */
    @Override
    public b_list_recycler_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.broadcasting_list_item,parent,false);
        broadcasting_list = new Broadcasting_list();
        return new ViewHolder(view);
    }

    /**
     * 필수 메소드 2: 새로운 뷰에 데이터 셋해주는 메소드.
     */
    @Override
    public void onBindViewHolder(@NonNull b_list_recycler_adapter.ViewHolder holder, int position) {
        //데이터를 담을 리스트의 포지션값을 가져온다.
        b_list_item = b_list_items_array.get(position);
        //입력된 데이터를 아이템의 각 항목에 셋해준다.
        holder.TV_roomid.setText(b_list_item.getRoom_id());
        holder.TV_roomname.setText(b_list_item.getRoom_name());
        holder.TV_bjname.setText(b_list_item.getBj_name());

        Log.e("뷰가 만들어지고 있나요? : ",b_list_item.getBj_name() );
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b_list_item = b_list_items_array.get(position);
                String roomid = holder.TV_roomid.getText().toString();
                String roomname = holder.TV_roomname.getText().toString();
                Intent intent = new Intent(v.getContext(), ViewerActivity_.class);
                intent.putExtra("room_id", roomid);
                intent.putExtra("user_id", broadcasting_list.st_id);
                intent.putExtra("key", "enter");
                intent.putExtra("roomname", roomname);
                v.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return b_list_items_array.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView TV_roomid, TV_roomname, TV_bjname;
        public ViewHolder(View itemView) {
            super(itemView);
            TV_roomid = (TextView) itemView.findViewById(R.id.tv_room_id);
            TV_roomname = (TextView) itemView.findViewById(R.id.tv_room_name);
            TV_bjname = (TextView)itemView.findViewById(R.id.tv_bj_name);
        }
    }

}
