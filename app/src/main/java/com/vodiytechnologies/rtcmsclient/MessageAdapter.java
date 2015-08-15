package com.vodiytechnologies.rtcmsclient;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Diyor on 8/10/2015.
 */
public class MessageAdapter extends BaseAdapter {

    private final List<Message> mMessages;
    private Activity mConext;

    public MessageAdapter(Activity context, List<Message> messages) {
        mConext = context;
        mMessages = messages;
    }

    @Override
    public int getCount() {
        if (mMessages != null) {
            return mMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public Message getItem(int position) {
        if (mMessages != null) {
            return mMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        Message message = getItem(position);

        LayoutInflater inflater = (LayoutInflater)mConext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.messages_list, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        boolean selfMessage = message.getSelf();
        setAlignment(holder, selfMessage);
        holder.mTxtMessage.setText(message.getContent());
        holder.mTxtInfo.setText(message.getTime());

        return convertView;
    }

    public void add(Message message) {
        mMessages.add(message);
    }

    public void add(List<Message> messages) {
        mMessages.addAll(messages);
    }

    private void setAlignment(ViewHolder holder, boolean isMe) {
        if (!isMe) {
            holder.mContentWithBG.setBackgroundResource(R.drawable.out_message_bg);

            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) holder.mContentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.mContentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) holder.mContent.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.mContent.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.mTxtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.mTxtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.mTxtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.mTxtInfo.setLayoutParams(layoutParams);
        } else {
            holder.mContentWithBG.setBackgroundResource(R.drawable.in_message_bg);

            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) holder.mContentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.mContentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) holder.mContent.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.mContent.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.mTxtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.mTxtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.mTxtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.mTxtInfo.setLayoutParams(layoutParams);
        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.mTxtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.mContent = (LinearLayout) v.findViewById(R.id.content);
        holder.mContentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.mTxtInfo = (TextView) v.findViewById(R.id.txtInfo);
        return holder;
    }


    private static class ViewHolder {
        public TextView mTxtMessage;
        public TextView mTxtInfo;
        public LinearLayout mContent;
        public LinearLayout mContentWithBG;
    }

}
