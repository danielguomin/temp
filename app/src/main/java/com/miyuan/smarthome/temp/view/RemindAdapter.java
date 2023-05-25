package com.miyuan.smarthome.temp.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.miyuan.smarthome.temp.R;
import com.miyuan.smarthome.temp.db.Remind;
import com.miyuan.smarthome.temp.db.TempDataBase;

import java.util.List;

public class RemindAdapter extends
        RecyclerView.Adapter<RemindAdapter.ViewHolder> {
    private List<Remind> mList;
    private OnItemClickListerner onItemClickListerner;
    private boolean edit;
    private TempDataBase db;

    public void setmList(List<Remind> mList) {
        this.mList = mList;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public void setOnItemClickListerner(OnItemClickListerner onItemClickListerner) {
        this.onItemClickListerner = onItemClickListerner;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int
            viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.remind_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        db = Room.databaseBuilder(view.getContext(), TempDataBase.class, "database_temp").allowMainThreadQueries().build();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Remind remind = mList.get(position);
        if (edit) {
            holder.status.setVisibility(View.GONE);
            holder.choice.setVisibility(View.VISIBLE);
            holder.choice.setBackgroundResource(remind.isLock() ? R.drawable.prohibit : remind.isChoice() ? R.drawable.choice : R.drawable.unchoice);
        } else {
            holder.choice.setVisibility(View.GONE);
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setChecked(remind.isHigh() || remind.isLow());
            holder.temp.setTextColor((remind.isHigh() || remind.isLow()) ? Color.BLACK : Color.GRAY);
        }
        holder.temp.setText(remind.getTemp() + "℃提醒");
        if (onItemClickListerner != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (edit) {
                        onItemClickListerner.onItemClick(position);
                    }
                }
            });
        }
        holder.status.setTag(position);
        holder.status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (edit) {
                    holder.temp.setTextColor(Color.BLACK);
                } else {
                    if (isChecked) {
                        holder.temp.setTextColor(Color.BLACK);
                    } else {
                        holder.temp.setTextColor(Color.GRAY);
                    }
                }
                Remind temp = mList.get((Integer) holder.status.getTag());
                if (temp.isOpen()) {
                    temp.setHigh(isChecked);
                } else {
                    temp.setLow(isChecked);
                }
                db.getRemindDao().update(temp);
            }
        });
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
        TextView temp;
        Switch status;
        View choice;

        public ViewHolder(@NonNull View view) {
            super(view);
            temp = view.findViewById(R.id.temp);
            status = view.findViewById(R.id.status);
            choice = view.findViewById(R.id.choice);
        }
    }
}
