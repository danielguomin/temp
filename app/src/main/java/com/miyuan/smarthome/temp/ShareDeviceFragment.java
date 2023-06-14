package com.miyuan.smarthome.temp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.databinding.FragmentShareDeviceBinding;

public class ShareDeviceFragment extends Fragment implements View.OnClickListener {

    private FragmentShareDeviceBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentShareDeviceBinding.inflate(inflater, container, false);
        }
        initView();
        return binding.getRoot();
    }

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("给他人共享");
        binding.copy.setOnClickListener(this);
        binding.deviceID.setText(BlueManager.tempInfoLiveData.getValue().getDeviceId());
        String name = TempApplication.currentLiveData.getValue().getName();
        binding.content.setText(Html.fromHtml("<font>   是否共享当前测温成员“" + name + "”的体温信息给其他APP查看？ <br/><br/>   确认共享请点击下方</font><font color='#2BAC69'>【复制共享码】</font><font>按钮，将共享码发给共享的人。切勿修改共享码。<br/><br/>  共享期间请保持手机与设备正常连接。建议在wifi模式下共享信息。<font/>", Html.FROM_HTML_MODE_LEGACY));
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
            case R.id.copy:
                ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("deviceID", binding.deviceID.getText().toString().trim());
                clipboardManager.setPrimaryClip(clip);
                Toast.makeText(getActivity(), "已复制", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}