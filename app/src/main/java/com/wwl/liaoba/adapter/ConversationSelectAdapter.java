package com.wwl.liaoba.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wwl.liaoba.R;
import com.wwl.liaoba.bean.User;

import java.util.ArrayList;


public class ConversationSelectAdapter extends BaseAdapter {
    private ArrayList<User> mList;
    private Context mContext;

    public ConversationSelectAdapter(ArrayList<User> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_conversationt_select, null);
            holder = new ViewHolder();
            holder.mTextView = (TextView) convertView.findViewById(R.id.tv_user);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        holder.mTextView.setText(mList.get(position).getUserName());
        return convertView;
    }

    class ViewHolder {
        TextView mTextView;
    }
}
