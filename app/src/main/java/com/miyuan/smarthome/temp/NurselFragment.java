package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.databinding.FragmentNurseBinding;
import com.miyuan.smarthome.temp.db.Nurse;
import com.miyuan.smarthome.temp.db.TempDataBase;
import com.miyuan.smarthome.temp.utils.TimeUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NurselFragment extends Fragment implements View.OnClickListener {

    private FragmentNurseBinding binding;

    private TempDataBase db;

    private int nurselType = 0;

    private long nurselTime = System.currentTimeMillis();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentNurseBinding.inflate(inflater, container, false);
        initView();
        db = Room.databaseBuilder(getContext(), TempDataBase.class, "database_temp").allowMainThreadQueries().build();
        return binding.getRoot();
    }

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("护理记录");
        binding.timeLayout.setOnClickListener(this);
        binding.time.setText(TimeUtils.getNormal());
        binding.save.setOnClickListener(this);
        binding.medicine.setOnClickListener(this);
        binding.medicine.setSelected(true);
        binding.physical.setOnClickListener(this);
        binding.other.setOnClickListener(this);
        binding.record.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                temp = s;
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.count.setText(s.length() + "/70");
                selectionStart = binding.record.getSelectionStart();
                selectionEnd = binding.record.getSelectionEnd();
                if (temp.length() > 70) {
                    s.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionEnd;
                    binding.record.setText(s);
                    binding.record.setSelection(tempSelection);//设置光标在最后
                }
            }
        });

        TimePickerFragment.timeLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");
                String date = sdf.format(new Date());
                binding.time.setText(date + s);
                Timestamp timestamp = Timestamp.valueOf(date + s);
                nurselTime = timestamp.getTime();
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
                Navigation.findNavController(v).navigateUp();
                break;
            case R.id.time_layout:
                TimePickerFragment.newInstance().show(getParentFragmentManager(), "");
                break;
            case R.id.medicine:
                nurselType = 0;
                binding.medicine.setSelected(true);
                binding.physical.setSelected(false);
                binding.other.setSelected(false);
                break;
            case R.id.physical:
                nurselType = 1;
                binding.medicine.setSelected(false);
                binding.physical.setSelected(true);
                binding.other.setSelected(false);
                break;
            case R.id.other:
                nurselType = 2;
                binding.medicine.setSelected(false);
                binding.physical.setSelected(false);
                binding.other.setSelected(true);
                break;
            case R.id.save:
                String content = binding.record.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(getActivity(), "请输入内容！", Toast.LENGTH_SHORT).show();
                    return;
                }
                binding.save.setEnabled(false);
                try {
                    Nurse nurse = new Nurse();
                    nurse.setTime(nurselTime);
                    nurse.setType(nurselType);
                    nurse.setContent(content);
                    nurse.setMemberId(1);
                    nurse.setMemberId(TempApplication.currentLiveData.getValue().getMemberId());
                    nurse.setDeviceId(BlueManager.tempInfoLiveData.getValue().getDeviceId());
                    db.getNurseDao().insert(nurse);
                    Toast.makeText(getActivity(), "保存成功！", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(v).navigateUp();
                } catch (Exception e) {
                    binding.save.setEnabled(true);
                    Toast.makeText(getActivity(), "保存失败！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}