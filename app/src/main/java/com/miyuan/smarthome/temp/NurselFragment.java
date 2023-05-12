package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
        binding = FragmentNurseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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