package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.databinding.FragmentTempRemindAddBinding;

public class TempRemindAddFragment extends Fragment implements View.OnClickListener {

    private FragmentTempRemindAddBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentTempRemindAddBinding.inflate(inflater, container, false);
            initView();
        }
        return binding.getRoot();
    }

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("新增温度提醒设置");
        binding.titlelayout.back.setOnClickListener(this);
        binding.confirm.setOnClickListener(this);
        binding.cancel.setOnClickListener(this);
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
                break;
            case R.id.cancel:
                break;
        }
    }
}