package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.databinding.FragmentTempRemindListBinding;

public class TempRemindListFragment extends Fragment implements View.OnClickListener {

    private FragmentTempRemindListBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentTempRemindListBinding.inflate(inflater, container, false);
            initView();
        }
        return binding.getRoot();
    }

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("成员列表");
        binding.titlelayout.back.setOnClickListener(this);
        binding.add.setOnClickListener(this);
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
            case R.id.add:
                break;
        }
    }
}