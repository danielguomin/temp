package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.databinding.FragmentNurseBinding;

public class NurselFragment extends Fragment implements View.OnClickListener {

    private FragmentNurseBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentNurseBinding.inflate(inflater, container, false);
            initView();
        }
        return binding.getRoot();
    }

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("护理记录");
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
        }
    }
}