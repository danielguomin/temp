package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.miyuan.smarthome.temp.databinding.FragmentTempRemindAddBinding;
import com.miyuan.smarthome.temp.db.Remind;
import com.miyuan.smarthome.temp.db.TempDataBase;

public class TempRemindAddFragment extends Fragment implements View.OnClickListener {

    private FragmentTempRemindAddBinding binding;
    private TempDataBase db;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentTempRemindAddBinding.inflate(inflater, container, false);
        }
        initView();
        return binding.getRoot();
    }

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("新增温度提醒设置");
        binding.titlelayout.back.setOnClickListener(this);
        binding.high.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.low.setChecked(!isChecked);
                }
            }
        });
        binding.low.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.high.setChecked(!isChecked);
                }
            }
        });
        binding.confirm.setOnClickListener(this);
        binding.cancel.setOnClickListener(this);
        db = Room.databaseBuilder(getContext(), TempDataBase.class, "database_temp").allowMainThreadQueries().build();
        initNumbers();
    }

    private void initNumbers() {
        binding.whole.setMaxValue(42);
        binding.whole.setMinValue(34);
        binding.decimal.setMinValue(0);
        binding.decimal.setMaxValue(9);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                Navigation.findNavController(v).navigateUp();
                break;
            case R.id.confirm:
                // 操作数据库
                try {
                    if (binding.high.isChecked() || binding.low.isChecked()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(binding.whole.getValue()).append(".").append(binding.decimal.getValue());
                        Remind remind = new Remind();
                        remind.setTemp(Float.valueOf(sb.toString()));
                        remind.setHigh(binding.high.isChecked());
                        remind.setLow(binding.low.isChecked());
                        remind.setOpen(binding.high.isChecked());
                        db.getRemindDao().insert(remind);
                        Navigation.findNavController(v).navigateUp();
                    } else {
                        Toast.makeText(getActivity(), "请选择提醒模式！", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "添加温度重复！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.cancel:
                Navigation.findNavController(v).navigateUp();
                break;
        }
    }
}