package com.dong.Diary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nhancv.kurentoandroid.R;

import java.util.ArrayList;

public class DiaryList_recycler_adapter extends RecyclerView.Adapter<DiaryList_recycler_adapter.ViewHolder> {
    Context context;
    ArrayList<DiaryList_item> diaryList = new ArrayList<>();
    DiaryList_item v_item;

    //생성자
    public DiaryList_recycler_adapter(Context context, ArrayList<DiaryList_item> diaryList_itemarray) {
        this.context = context;
        this.diaryList = diaryList_itemarray;
    }

    @SuppressLint("LongLogTag")
    @NonNull
    /**
     * 필수 메소드 1: 새로운 뷰 생성.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vodlist_item,parent,false);
        Log.e("브이오디 리스트 oncreateViewHolder", "들어와쩌염 뿌우");
        return new ViewHolder(view);
    }

    /**
     * 필수 메소드 2: 새로운 뷰에 데이터 셋해주는 메소드.
     */
    //재활용 되는 뷰가 호출하여 실행되는 메소드, 뷰 홀더를 전달하고 어댑터는 position 의 데이터를 결합시킵니다.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //데이터를 담을 리스트의 포지션값을 가져온다.
        v_item = diaryList.get(position);
        //입력된 데이터를 아이템의 각 항목에 셋해준다.
        holder.tv_vodid.setText(v_item.getVod_id());
        holder.tv_bjname.setText(v_item.getBj_id());
        holder.tv_vodname.setText(v_item.getVod_name());
        //썸네일 경로를 통해 썸네일을 가져와 이미지 뷰에 셋한다.
        Glide.with(holder.itemView.getContext())
                .load("http://222.239.249.149/"+v_item.getSt_Thumbnail())
                .into(holder.IV_thumbnail);

        Log.e("븨오디 리스트가 만들어지고 있나요? : ", v_item.getSt_Thumbnail());
        //vod 목록에서 원하는 아이템(vod)을 클릭하면 vod_id를 인텐트에 넣어 VodPlayActivity로 넘겨준다.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v_item = diaryList.get(position);
                String st_vodTitle = holder.tv_vodid.getText().toString();
                Log.e("vodTitle check, vodlist", st_vodTitle);
                Intent intent = new Intent(v.getContext(), DiaryReadActivity.class);
                intent.putExtra("id", st_vodTitle);
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return diaryList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView IV_thumbnail;
        TextView tv_vodid, tv_vodname, tv_bjname;
        public ViewHolder(View itemView) {
            super(itemView);

            IV_thumbnail = (ImageView)itemView.findViewById(R.id.iv_VodThumbnail);
            tv_vodid = (TextView) itemView.findViewById(R.id.tv_VodIdSave);
            tv_vodname = (TextView) itemView.findViewById(R.id.tv_VodName);
            tv_bjname = (TextView)itemView.findViewById(R.id.tv_BjName);
        }
    }
}
