package com.miyuan.smarthome.temp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;

import com.miyuan.smarthome.temp.utils.SingleLiveData;

public class TimePickerFragment extends DialogFragment implements View.OnClickListener {

    private static SingleLiveData<String> _timeLiveData = new SingleLiveData<>();
    public static LiveData<String> timeLiveData = _timeLiveData;
    TimePicker tp;

    public static TimePickerFragment newInstance() {
        TimePickerFragment fragment = new TimePickerFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(dp2px(271), dp2px(266));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_time, null);
        tp = v.findViewById(R.id.time);
        tp.setIs24HourView(true);
        View cancel = v.findViewById(R.id.cancel);
        View confirm = v.findViewById(R.id.confirm);
        cancel.setOnClickListener(this);
        confirm.setOnClickListener(this);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
        return alertDialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.confirm:
                String hour = tp.getHour() < 10 ? "0" + tp.getHour() : tp.getHour() + "";
                String minute = tp.getMinute() < 10 ? "0" + tp.getMinute() : tp.getMinute() + "";
                _timeLiveData.setValue(hour + ":" + minute);
                Toast.makeText(getActivity(), hour + ":" + minute, Toast.LENGTH_SHORT).show();
                dismiss();
                break;
        }
    }

    private int dp2px(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
