package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.blue.ProtocolUtils;
import com.miyuan.smarthome.temp.databinding.FragmentCreateFamilyMemberBinding;
import com.miyuan.smarthome.temp.log.Log;

public class CreateFamilyMemberFragment extends Fragment implements View.OnClickListener {

    private FragmentCreateFamilyMemberBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentCreateFamilyMemberBinding.inflate(inflater, container, false);
            initView();
        }
        return binding.getRoot();
    }

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("创建成员");
        binding.titlelayout.back.setOnClickListener(this);
        binding.save.setOnClickListener(this);


        BlueManager.membearLiveData.observe(this.getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                Log.d("CreateFamilyMemberFragment membearLiveData onChanged ");
                if (success) {
                    Log.d("CreateFamilyMemberFragment onChanged  navigateUp");
                    Navigation.findNavController(getView()).navigateUp();
                    Log.d("CreateFamilyMemberFragment onChanged send getTempStatus");
                    BlueManager.getInstance().send(ProtocolUtils.getTempStatus(System.currentTimeMillis()));
                } else {
                    Toast.makeText(getActivity(), "创建成员失败，请重试！", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                Log.d("CreateFamilyMemberFragment onClick navigateUp");
                Navigation.findNavController(v).navigateUp();
                break;
            case R.id.save:
                String name = binding.name.getText().toString().trim();
                Log.d("CreateFamilyMemberFragment onClick send updateMember");
                BlueManager.getInstance().send(ProtocolUtils.updateMember(true, BlueManager.tempInfoLiveData.getValue().getMemberCount() + 1, name));
                break;
        }
    }
}