package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.databinding.FragmentDisclaimerBinding;
import com.tencent.mmkv.MMKV;

public class DisclaimerFragment extends Fragment implements View.OnClickListener {

    private FragmentDisclaimerBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentDisclaimerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.titlelayout.back.setVisibility(View.INVISIBLE);
        binding.titlelayout.title.setText("免责声明");
        binding.agree.setOnClickListener(this);
        binding.disagree.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.disagree:
                getActivity().finish();
                break;
            case R.id.agree:
                MMKV mmkv = MMKV.defaultMMKV();
                mmkv.putBoolean("no_prompt", true);
                Navigation.findNavController(v).navigate(R.id.action_DisclaimerFragment_to_HomeFragment);
                break;
        }
    }
}