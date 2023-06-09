package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.databinding.FragmentShareBinding;

public class ShareFragment extends Fragment implements View.OnClickListener {

    private FragmentShareBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentShareBinding.inflate(inflater, container, false);
        }
        initView();
        return binding.getRoot();
    }

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("共享设备");
        binding.shareLayout.setOnClickListener(this);
        binding.showLayout.setOnClickListener(this);
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
            case R.id.share_layout:
                if (null != BlueManager.tempInfoLiveData.getValue()) {
                    Navigation.findNavController(v).navigate(R.id.action_ShareFragment_to_ShareDeviceFragment);
                } else {
                    Toast.makeText(getActivity(), "请链接蓝牙设备", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.show_layout:
                Navigation.findNavController(v).navigate(R.id.action_ShareFragment_to_ViewShareDeviceFragment);
                break;
            default:
                break;
        }
    }
}