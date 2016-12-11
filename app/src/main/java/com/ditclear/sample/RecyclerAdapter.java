package com.ditclear.sample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 页面描述：adapter
 *
 * Created by ditclear on 2016/12/11.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemHolder> {

    private List<String > mList= new ArrayList<>();

    LayoutInflater mInflater;
    public RecyclerAdapter(Context context) {
        mInflater=LayoutInflater.from(context);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(mInflater.inflate(R.layout.item_list,parent,false));
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.mTextView.setText(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setList(List<String> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    static class ItemHolder extends RecyclerView.ViewHolder{
        TextView mTextView;
        public ItemHolder(View itemView) {
            super(itemView);
            mTextView= (TextView) itemView.findViewById(R.id.text);
        }
    }
}
