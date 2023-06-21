package com.miyuan.smarthome.temp.view;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.miyuan.smarthome.temp.R;
import com.miyuan.smarthome.temp.db.History;

import java.util.List;
import java.util.Map;

public class HistoryTempAdapter extends
        RecyclerView.Adapter<HistoryTempAdapter.ViewHolder> {
    private Map<String, List<History>> datas;
    private OnItemClickListerner onItemClickListerner;


    public HistoryTempAdapter(Map<String, List<History>> datas) {
        this.datas = datas;
    }

    public void setOnItemClickListerner(OnItemClickListerner onItemClickListerner) {
        this.onItemClickListerner = onItemClickListerner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int
            viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        String time = (String) datas.keySet().toArray()[position];
        holder.name.setText(time);
        if (onItemClickListerner != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListerner.onItemClick(time);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    //点击事件
    public interface OnItemClickListerner {
        void onItemClick(String time);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
        }
    }
}
