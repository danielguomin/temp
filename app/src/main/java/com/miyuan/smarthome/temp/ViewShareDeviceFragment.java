package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.databinding.FragmentViewShareDeviceBinding;

public class ViewShareDeviceFragment extends Fragment implements View.OnClickListener {

    private FragmentViewShareDeviceBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentViewShareDeviceBinding.inflate(inflater, container, false);
        }
        initView();
        return binding.getRoot();
    }

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("查看他人共享");
        binding.show.setOnClickListener(this);
        binding.content.setText(Html.fromHtml("<font>   请将获取到的共享码复制到下方输入设备共享码框中，然后点击【开始共享】按钮，查看共享信息。<br/><br/>   建议在wifi模式下查看共享信息。</font>", Html.FROM_HTML_MODE_LEGACY));
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
                Navigation.findNavController(getView()).navigateUp();
                break;
            case R.id.show:
                String deviceId = binding.deviceId.getText().toString().trim();
                if (TextUtils.isEmpty(deviceId)) {
                    Toast.makeText(getActivity(), "请输入设备码！", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString("deviceId", deviceId);
                Navigation.findNavController(getView()).navigate(R.id.action_ViewShareDeviceFragment_to_HomeShowFragment);
                break;
        }
    }
}