package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.miyuan.smarthome.temp.databinding.FragmentScanFailBinding;

public class ScanFailFragment extends Fragment implements View.OnClickListener {

    private FragmentScanFailBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentScanFailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.retry.setOnClickListener(this);
        binding.titlelayout.back.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.retry:
                break;
        }
    }
}