package com.miyuan.smarthome.temp.view;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.miyuan.smarthome.temp.R;
import com.miyuan.smarthome.temp.db.Member;

import java.util.List;

public class MemberAdapter extends
        RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    private List<Member> mList;
    private OnItemClickListerner onItemClickListerner;


    public MemberAdapter(List<Member> mList) {
        this.mList = mList;
    }

    public void setmList(List<Member> mList) {
        this.mList = mList;
    }

    public void setOnItemClickListerner(OnItemClickListerner onItemClickListerner) {
        this.onItemClickListerner = onItemClickListerner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int
            viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.family_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Member member = mList.get(position);
        holder.name.setText(member.getName());
        if (member.isChoice()) {
            holder.status.setVisibility(View.VISIBLE);
            holder.status_tag.setVisibility(View.VISIBLE);
        } else {
            holder.status.setVisibility(View.INVISIBLE);
            holder.status_tag.setVisibility(View.INVISIBLE);
        }
        if (onItemClickListerner != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListerner.onItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //点击事件
    public interface OnItemClickListerner {
        void onItemClick(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        View status;
        View status_tag;

        public ViewHolder(@NonNull View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            status = (View) view.findViewById(R.id.status);
            status_tag = (View) view.findViewById(R.id.status_tag);
        }
    }
}
