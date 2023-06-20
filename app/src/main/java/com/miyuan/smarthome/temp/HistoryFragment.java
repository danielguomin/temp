package com.miyuan.smarthome.temp;

import static com.miyuan.smarthome.temp.TempApplication.HIGH_TEMP_DIVIDER;
import static com.miyuan.smarthome.temp.TempApplication.LOW_TEMP_DIVIDER;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.databinding.FragmentHistoryBinding;
import com.miyuan.smarthome.temp.db.Entry;
import com.miyuan.smarthome.temp.db.History;
import com.miyuan.smarthome.temp.db.Nurse;
import com.miyuan.smarthome.temp.db.TempDataBase;
import com.miyuan.smarthome.temp.log.Log;
import com.miyuan.smarthome.temp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class HistoryFragment extends Fragment implements View.OnClickListener, DatePicker.OnDateChangedListener {

    private FragmentHistoryBinding binding;

    private TempDataBase db;
    private List<History> historyList;
    private final static int COUNT = 10;
    boolean[] dpHigh = new boolean[COUNT];
    boolean[] dpLow = new boolean[COUNT];
    Queue<Float> checkQueue = new LinkedList<>();
    boolean high = false;
    int highCount = 0;

    private void getHistory() {
        int memberId = getArguments().getInt("memberId");
        String deviceId = BlueManager.tempInfoLiveData.getValue().getDeviceId();
        historyList = db.getHistoryDao().getAll(deviceId, memberId);
        isSameDay(new Date(System.currentTimeMillis()));
    }

    Handler handler = new Handler();

    private long startTime = 0;

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("历史体温");
        binding.today.setOnClickListener(this);
        ((LinearLayout) binding.time.getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
        binding.time.setOnDateChangedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getNurseInfo(long time) {
        int nurselCount = 0;
        List<Nurse> nurseList = db.getNurseDao().getAll();
        for (Nurse nurse : nurseList) {
            if (TimeUtils.isSameDay(new Date(nurse.getTime()), new Date(time))) {
                nurselCount++;
            }
        }
        binding.lineChart.setNurseData(nurseList);
        binding.nusreCount.setText(String.valueOf(nurselCount));
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentHistoryBinding.inflate(inflater, container, false);
            db = Room.databaseBuilder(getContext(), TempDataBase.class, "database_temp").allowMainThreadQueries().build();
        }
        initView();
        getHistory();
        getNurseInfo(System.currentTimeMillis());
        return binding.getRoot();
    }

    private void isSameDay(Date date) {
        if (historyList == null || historyList.size() <= 0) {
            return;
        }
        List<Entry> entryList = new ArrayList<>();
        float highTemp = 0;
        long highTime = 0;
        for (History history : historyList) {
            if (startTime == 0) {
                startTime = history.getTime();
            }
            if (TimeUtils.isSameDay(new Date(history.getTime()), date)) {
                long start = history.getTime();
                String temps1 = history.getTemps();
                String[] temps = temps1.substring(1, temps1.length() - 1).split(",");
                for (int i = 0; i < temps.length; i++) {
                    Float temp = Float.valueOf(temps[i]);
                    if (temp > highTemp) {
                        highTemp = temp;
                        highTime = start + i * 10 * 1000;
                    }
                    Entry entry = new Entry(start + i * 10 * 1000, temp);
                    entryList.add(entry);
                }
            }
        }

        binding.high.setText(String.valueOf(highTemp));
        binding.highTime.setText(TimeUtils.getHourStr(new Date(Long.valueOf(highTime))));
        handler.post(new Runnable() {
            @Override
            public void run() {
                setData(entryList);
            }
        });

    }

    private void setData(List<Entry> values) {
        if (values == null) {
            return;
        }
        int normalCountTime = 0, highCountTime = 0, lowCountTime = 0;
        highCount = 0;
        high = false;
        for (Entry entry : values) {
            float y = entry.getTemp();
            if (y <= LOW_TEMP_DIVIDER) {
                normalCountTime += 10;
            } else if (y >= HIGH_TEMP_DIVIDER) {
                highCountTime += 10;
            } else {
                lowCountTime += 10;
            }
            if (checkQueue.size() >= COUNT) {
                checkQueue.poll();
            }
            checkQueue.offer(y);
            check();
        }
        binding.measure.setText(TimeUtils.getHourStrForSecond(new
                Date(values.size() * 10 * 1000)));
        binding.highTimeCount.setText(TimeUtils.getHourStrForSecond(new
                Date(highCountTime * 1000)));
        binding.lowTimeCount.setText(TimeUtils.getHourStrForSecond(new
                Date(lowCountTime * 1000)));
        binding.normalTimeCount.setText(TimeUtils.getHourStrForSecond(new
                Date(normalCountTime * 1000)));
        binding.highCount.setText(String.valueOf(highCount));
        binding.lineChart.changeStyle(values, 4);

    }

    private void check() {
        Iterator<Float> iterator = checkQueue.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Float next = iterator.next();
            if (i == 0) {
                dpHigh[i] = next.floatValue() >= HIGH_TEMP_DIVIDER;
                dpLow[i] = next.floatValue() < HIGH_TEMP_DIVIDER;
            } else {
                dpHigh[i] = dpHigh[i - 1] && next.floatValue() >= HIGH_TEMP_DIVIDER;
                dpLow[i] = dpLow[i - 1] && next.floatValue() < HIGH_TEMP_DIVIDER;
            }
            i++;
        }
        if (dpHigh[checkQueue.size() - 1]) {
            if (high) {
                return;
            }
            high = true;
            highCount++;
        }
        if (dpLow[checkQueue.size() - 1]) {
            high = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                Navigation.findNavController(v).navigateUp();
                break;
            case R.id.today:
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                binding.time.updateDate(year, month, day);
                break;
        }
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Log.d(" onDateChanged  year " + year + " month " + monthOfYear + " day " + dayOfMonth);
        Date date = new Date(year - 1900, monthOfYear, dayOfMonth);
        isSameDay(date);
        getNurseInfo(date.getTime());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }
}