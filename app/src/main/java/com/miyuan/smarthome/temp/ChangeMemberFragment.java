package com.miyuan.smarthome.temp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;

import com.miyuan.smarthome.temp.utils.SingleLiveData;

public class ChangeMemberFragment extends DialogFragment implements View.OnClickListener {

    private static SingleLiveData<Integer> _confirmLiveData = new SingleLiveData<>();
    public static LiveData<Integer> confirmLiveData = _confirmLiveData;
    private static int position = -1;

    public static ChangeMemberFragment newInstance(int positon) {
        position = positon;
        ChangeMemberFragment fragment = new ChangeMemberFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(dp2px(271), dp2px(330));
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_change_member, null);
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
                _confirmLiveData.setValue(-1);
                break;
            case R.id.confirm:
                _confirmLiveData.setValue(position);
                dismiss();
                break;
        }
    }

    private int dp2px(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
