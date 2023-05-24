package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.databinding.FragmentScanFailBinding;

public class ScanFailFragment extends Fragment implements View.OnClickListener {

    private FragmentScanFailBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentScanFailBinding.inflate(inflater, container, false);
            initView();
        }
        return binding.getRoot();
    }

    private void initView() {
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
                BlueManager.getInstance().startScan();
                Navigation.findNavController(getView()).navigateUp();
                break;
        }
    }
}