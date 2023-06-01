package com.miyuan.smarthome.temp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.miyuan.smarthome.temp.db.Remind;
import com.miyuan.smarthome.temp.db.TempDataBase;
import com.miyuan.smarthome.temp.log.Log;
import com.miyuan.smarthome.temp.utils.SingleLiveData;

import java.util.List;

public class TempPickerFragment extends DialogFragment implements View.OnClickListener {

    private static SingleLiveData<Remind> _tempLiveData = new SingleLiveData<>();
    public static LiveData<Remind> tempLiveData = _tempLiveData;
    NumberPicker whole;
    NumberPicker decimal;
    private static Remind remind;
    private TempDataBase db;
    private List<Remind> reminds;


    public static TempPickerFragment getInstance(Remind remind) {
        TempPickerFragment.remind = remind;
        TempPickerFragment fragment = new TempPickerFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(dp2px(271), dp2px(280));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_temp, null);
        View cancel = v.findViewById(R.id.cancel);
        View confirm = v.findViewById(R.id.confirm);
        whole = v.findViewById(R.id.whole);
        decimal = v.findViewById(R.id.decimal);
        initNumbers();
        db = Room.databaseBuilder(getContext(), TempDataBase.class, "database_temp").allowMainThreadQueries().build();
        reminds = db.getRemindDao().getAll();
        cancel.setOnClickListener(this);
        confirm.setOnClickListener(this);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
        return alertDialog;
    }

    private void initNumbers() {
        whole.setMaxValue(42);
        whole.setMinValue(34);
        decimal.setMinValue(0);
        decimal.setMaxValue(9);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.confirm:
                StringBuilder sb = new StringBuilder();
                sb.append(whole.getValue()).append(".").append(decimal.getValue());
                float temp = Float.valueOf(sb.toString());
                for(Remind remind:reminds){
                    if (remind.getTemp() == temp) {
                        Toast.makeText(getActivity(), "该温度已存在！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                TempPickerFragment.remind.setTemp(temp);
                _tempLiveData.setValue(TempPickerFragment.remind);
                dismiss();
                break;
        }
    }

    private int dp2px(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
