package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.databinding.FragmentNurseBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

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
        binding.timeLayout.setOnClickListener(this);
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        binding.time.setText(sdf.format(now));
        binding.save.setOnClickListener(this);
        binding.medicine.setOnClickListener(this);
        binding.physical.setOnClickListener(this);
        binding.other.setOnClickListener(this);
        binding.spirit.setOnClickListener(this);
        binding.appetite.setOnClickListener(this);
        binding.cooling.setOnClickListener(this);
        binding.dose.setOnClickListener(this);
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
            case R.id.save:
                break;
            case R.id.medicine:
                binding.medicine.setSelected(true);
                binding.physical.setSelected(false);
                binding.other.setSelected(false);
                break;
            case R.id.physical:
                binding.medicine.setSelected(false);
                binding.physical.setSelected(true);
                binding.other.setSelected(false);
                break;
            case R.id.other:
                binding.medicine.setSelected(false);
                binding.physical.setSelected(false);
                binding.other.setSelected(true);
                break;
            case R.id.spirit:
                binding.spirit.setSelected(!binding.spirit.isSelected());
                break;
            case R.id.appetite:
                binding.appetite.setSelected(!binding.appetite.isSelected());
                break;
            case R.id.cooling:
                binding.cooling.setSelected(!binding.cooling.isSelected());
                break;
            case R.id.dose:
                binding.dose.setSelected(!binding.dose.isSelected());
                break;
        }
    }
}