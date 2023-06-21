package com.miyuan.smarthome.temp;

import static com.miyuan.smarthome.temp.TempApplication._historyLiveData;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.databinding.FragmentHistoryTempListBinding;
import com.miyuan.smarthome.temp.db.History;
import com.miyuan.smarthome.temp.db.TempDataBase;
import com.miyuan.smarthome.temp.utils.TimeUtils;
import com.miyuan.smarthome.temp.view.HistoryTempAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

;

public class HistoryTempListFragment extends Fragment implements View.OnClickListener {

    private FragmentHistoryTempListBinding binding;

    private TempDataBase db;
    private List<History> historyList;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentHistoryTempListBinding.inflate(inflater, container, false);
            db = Room.databaseBuilder(getContext(), TempDataBase.class, "database_temp").allowMainThreadQueries().build();
        }
        initView();
        return binding.getRoot();
    }

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("历史体温");
        int memberId = getArguments().getInt("memberId");
        String deviceId = BlueManager.tempInfoLiveData.getValue().getDeviceId();
        historyList = db.getHistoryDao().getAll(deviceId, memberId);
        Map<String, List<History>> datas = new HashMap<>();
        for (History history : historyList) {
            String timeStr = TimeUtils.getTimeStr(history.getTime());
            if (datas.containsKey(timeStr)) {
                datas.get(timeStr).add(history);
            } else {
                List<History> list = new ArrayList<>();
                list.add(history);
                datas.put(timeStr, list);
            }
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.date.setLayoutManager(layoutManager);
        HistoryTempAdapter memberAdapter = new HistoryTempAdapter(datas);
        binding.date.setAdapter(memberAdapter);
        memberAdapter.setOnItemClickListerner(new HistoryTempAdapter.OnItemClickListerner() {
            @Override
            public void onItemClick(String time) {
                _historyLiveData.postValue(datas.get(time));
//                bundle.putParcelableArrayList("histories", (ArrayList<? extends Parcelable>) list);
                Navigation.findNavController(getView()).navigate(R.id.action_HistoryTempListFragment_to_HistoryFragment);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                Navigation.findNavController(v).navigateUp();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}