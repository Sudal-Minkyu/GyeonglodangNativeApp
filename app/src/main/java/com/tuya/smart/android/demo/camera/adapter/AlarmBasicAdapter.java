package com.tuya.smart.android.demo.camera.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.bean.AlarmMessage;

import java.util.List;

public class AlarmBasicAdapter extends RecyclerView.Adapter<AlarmBasicAdapter.MyViewHolder> {

    private final LayoutInflater mInflater;
    private final List<AlarmMessage> cameraMessageBeans;

    public AlarmBasicAdapter(Context context, List<AlarmMessage> cameraMessageBeans) {
        mInflater = LayoutInflater.from(context);
        this.cameraMessageBeans = cameraMessageBeans;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.alarm_newui_more_motion_recycle_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final AlarmMessage messageBean = cameraMessageBeans.get(position);
        if (messageBean != null) {

            String title = messageBean.getTitle();
            String text = messageBean.getText();
            String date = messageBean.getDate();

            if (title != null) {
                holder.mTvTitle.setText(title);
            }
            if (text != null) {
                holder.mTvBody.setText(text);
            }
            if (date != null) {
                holder.mTvRegDate.setText(date);
            }
        }
    }

    @Override
    public int getItemCount() {
        return cameraMessageBeans.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTvTitle;
        private final TextView mTvBody;
        private final TextView mTvRegDate;

        public MyViewHolder(final View view) {
            super(view);
            mTvTitle = view.findViewById(R.id.tv_title);
            mTvBody = view.findViewById(R.id.tv_body);
            mTvRegDate = view.findViewById(R.id.tv_reg_date);

        }
    }
}
