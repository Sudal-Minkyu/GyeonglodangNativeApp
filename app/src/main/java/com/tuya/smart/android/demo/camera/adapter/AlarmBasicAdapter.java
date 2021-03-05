package com.tuya.smart.android.demo.camera.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.tuya.drawee.view.DecryptImageView;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.bean.AlarmMessage;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageBean;

import org.eclipse.paho.client.mqttv3.util.Strings;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by huangdaju on 2018/3/5.
 */

public class AlarmBasicAdapter extends RecyclerView.Adapter<AlarmBasicAdapter.MyViewHolder> {

    private LayoutInflater mInflater;
    private List<AlarmMessage> cameraMessageBeans;

    public AlarmBasicAdapter(Context context, List<AlarmMessage> cameraMessageBeans) {
        mInflater = LayoutInflater.from(context);
        this.cameraMessageBeans = cameraMessageBeans;
    }

    public void updateAlarmDetectionMessage(List<AlarmMessage> messageBeans) {
        if (null != cameraMessageBeans) {
            cameraMessageBeans.clear();
            cameraMessageBeans.addAll(messageBeans);
            notifyDataSetChanged();
        }
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.alarm_newui_more_motion_recycle_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
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

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvTitle;
        private TextView mTvBody;
        private TextView mTvRegDate;


        public MyViewHolder(final View view) {
            super(view);
            mTvTitle = view.findViewById(R.id.tv_title);
            mTvBody = view.findViewById(R.id.tv_body);
            mTvRegDate = view.findViewById(R.id.tv_reg_date);

        }
    }
}
