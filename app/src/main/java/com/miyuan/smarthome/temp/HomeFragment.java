package com.miyuan.smarthome.temp;

import static com.miyuan.smarthome.temp.TempApplication.HIGH_TEMP_DIVIDER;
import static com.miyuan.smarthome.temp.TempApplication.LOW_TEMP_DIVIDER;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.blue.BoxEvent;
import com.miyuan.smarthome.temp.blue.ProtocolUtils;
import com.miyuan.smarthome.temp.databinding.FragmentHomeBinding;
import com.miyuan.smarthome.temp.db.CurrentTemp;
import com.miyuan.smarthome.temp.db.Entry;
import com.miyuan.smarthome.temp.db.History;
import com.miyuan.smarthome.temp.db.HistoryTemp;
import com.miyuan.smarthome.temp.db.Member;
import com.miyuan.smarthome.temp.db.Nurse;
import com.miyuan.smarthome.temp.db.Remind;
import com.miyuan.smarthome.temp.db.TempDataBase;
import com.miyuan.smarthome.temp.db.TempInfo;
import com.miyuan.smarthome.temp.log.Log;
import com.miyuan.smarthome.temp.net.Response;
import com.miyuan.smarthome.temp.net.TempApiManager;
import com.miyuan.smarthome.temp.utils.TTSManager;
import com.miyuan.smarthome.temp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private FragmentHomeBinding binding;

    private TempDataBase db;
    private List<Entry> entryHistoryList;
    private List<Entry> currentList = new ArrayList<>();

    private final static int COUNT = 10;
    boolean[] dpHigh = new boolean[COUNT];
    boolean[] dpLow = new boolean[COUNT];

    private float lastTemp = 34;

    private List<Remind> reminds;

    private Float[] highReminds = new Float[]{};
    private Float[] lowReminds = new Float[]{};
    private boolean[] charging = new boolean[3];
    private Map<Float, Boolean> remindTag = new HashMap<>();


    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            BlueManager.getInstance().send(ProtocolUtils.getCurrentTemp());
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            Log.d("HomeFragment onCreateView");
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            db = Room.databaseBuilder(getContext(), TempDataBase.class, "database_temp").allowMainThreadQueries().build();
        }
        initView();
        return binding.getRoot();
    }

    Queue<Float> checkQueue = new LinkedList<>();

    private void dealWithHistory(History history) {
        if (TimeUtils.isSameDay(new Date(history.getTime())) || TimeUtils.isYesterday(new Date(history.getTime()))) {
            long start = history.getTime();
            String temps1 = history.getTemps();
            String[] temps = temps1.substring(1, temps1.length() - 1).split(",");
            for (int i = 0; i < temps.length; i++) {
                Entry entry = new Entry(start + i * 10 * 1000, Float.valueOf(temps[i]));
                entryHistoryList.add(entry);
            }
        }
    }

    private long currentFirstTime;


    int nurselCount = 0;

    @Override
    public void onResume() {
        super.onResume();
        getNurseInfo();
//        handler.post(runnable);
    }

    @Override
    public void onPause() {
        super.onPause();
//        handler.removeCallbacks(runnable);
    }

    private void initPointer() {
        CurrentTemp currentTemp = BlueManager.currentTempLiveData.getValue();
        if (currentTemp != null) {
            View pointer = binding.pointer;
            pointer.setPivotX(pointer.getWidth());
            pointer.setPivotY(pointer.getHeight() / 2);
            float from = pointer.getRotation();
            float to = (180 * (currentTemp.getTemp() - 34)) / 8f;
            Log.d("from = " + from + " to " + to);
            if (from != to) {
                pointer.setRotation(to);
                lastTemp = currentTemp.getTemp();
            }
        }
    }

    private void checkCharging(int charging) {
        if (charging <= 20 && this.charging[0] == false) {
            this.charging[0] = true;
            TTSManager.getInstance().speek("当前测温设备电量低于20%，请确认备用电池是否满电");
        } else if (charging <= 40 && this.charging[1] == false) {
            this.charging[1] = true;
            TTSManager.getInstance().speek("当前测温设备电量低于40%，请确认备用电池是否满电");
        }
    }

    private void initView() {
        binding.history.setOnClickListener(this);
        binding.remind.setOnClickListener(this);
        binding.share.setOnClickListener(this);
        binding.record.setOnClickListener(this);
        binding.second.setOnClickListener(this);
        binding.two.setOnClickListener(this);
        binding.second.setSelected(true);
        binding.six.setOnClickListener(this);
        binding.twelve.setOnClickListener(this);
        binding.twenty.setOnClickListener(this);
        binding.triangle.setOnClickListener(this);
        binding.name.setOnClickListener(this);
        binding.scan.setImageResource(R.drawable.scan_bg);
        AnimationDrawable drawable = (AnimationDrawable) binding.scan.getDrawable();
        drawable.start();

        if (BlueManager.getInstance().isConnected()) {
            binding.scanlayout.setVisibility(View.GONE);
            binding.tempLayout.setVisibility(View.VISIBLE);
        }
        reminds = db.getRemindDao().getAll();
        List<Float> highReminds = new ArrayList<>();
        List<Float> lowReminds = new ArrayList<>();
        if (!remindTag.containsKey(35f)) {
            remindTag.put(35f, false);
        }
        for (Remind remind : reminds) {
            if (remind.isOpen() && remind.isHigh()) {
                highReminds.add(remind.getTemp());
            } else if (!remind.isOpen() && remind.isLow()) {
                lowReminds.add(remind.getTemp());
            }
            if (!remindTag.containsKey(remind.getTemp())) {
                remindTag.put(remind.getTemp(), false);
            }
        }
        if (null != highReminds && highReminds.size() > 0) {
            this.highReminds = highReminds.toArray(this.highReminds);
            Arrays.sort(this.highReminds);
        }
        if (null != lowReminds && lowReminds.size() > 0) {
            this.lowReminds = lowReminds.toArray(this.lowReminds);
            Arrays.sort(this.lowReminds);
        }

        initPointer();

        BlueManager.connectStatusLiveData.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                Log.d("HomeFragment connectStatusLiveData onChanged " + integer);
                switch (integer) {
                    case BoxEvent.BLUE_CONNECTED:
                        Log.d("HomeFragment onChanged send ");
                        binding.scanlayout.setVisibility(View.GONE);
                        binding.tempLayout.setVisibility(View.VISIBLE);
                        BlueManager.getInstance().send(ProtocolUtils.getTempStatus(System.currentTimeMillis()));
                        break;
                    case BoxEvent.BLUE_SCAN_FAILED:
                        Log.d("HomeFragment onChanged go ScanFailFragment");
                        Navigation.findNavController(getView()).navigate(R.id.action_HomeFragment_to_ScanFailFragment);
                        break;
                }
            }
        });

        BlueManager.tempInfoLiveData.observe(getViewLifecycleOwner(), new Observer<TempInfo>() {
            @Override
            public void onChanged(TempInfo info) {
                Log.d("HomeFragment tempInfoLiveData onChanged ");
                Log.d(info.toString());
                if (info.getMemberCount() == 0) {
                    Log.d("HomeFragment onChanged go CreateFamilyMemberFragment");
                    Navigation.findNavController(getView()).navigate(R.id.action_HomeFragment_to_CreateFamilyMemberFragment);
                    return;
                }
                if (info.getMemberId() == 0) {
                    Log.d("HomeFragment onChanged go FamilyMemberListFragment");
                    Navigation.findNavController(getView()).navigate(R.id.action_HomeFragment_to_FamilyMemberListFragment);
                    return;
                }
                List<Member> members = info.getMembers();
                for (Member member : members) {
                    if (member.getMemberId() == info.getMemberId()) {
                        TempApplication._currentMemberLiveData.postValue(member);
                    }
                }

                initUI(info, null);

                // 定时获取实时温度
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 1000);//启动定时任务

                getHistory(info);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!charging[2]) {
                            TTSManager.getInstance().speek("正在获取历史体温数据，请勿关闭APP");
                        }
                        BlueManager.getInstance().send(ProtocolUtils.getHistoryTemp());
                    }
                }, 0);
            }
        });

        BlueManager.currentTempLiveData.observe(getViewLifecycleOwner(), new Observer<CurrentTemp>() {
            @Override
            public void onChanged(CurrentTemp temp) {
                Log.d("HomeFragment currentTempLiveData onChanged ");
                initUI(null, temp);
            }
        });

        BlueManager.currentList.observeForever(new Observer<List<Float>>() {
            @Override
            public void onChanged(List<Float> floats) {
                Log.d("HomeFragment currentList onChanged " + floats.size());
                List<Entry> entryList = new ArrayList<>();
                if (entryHistoryList != null) {
                    entryList.addAll(entryHistoryList);
                }

                if (currentFirstTime == 0) {
                    currentFirstTime = System.currentTimeMillis();
                }
                float[] temps = new float[floats.size()];
                for (int i = 0; i < floats.size(); i++) {
                    temps[i] = floats.get(i);
                    Entry entry = new Entry(currentFirstTime + i * 10 * 1000, floats.get(i));
                    entryList.add(entry);
                }
                setData(entryList);
                History history = new History();
                history.setMemberID(TempApplication.currentLiveData.getValue().getMemberId());
                history.setDevicesID(BlueManager.tempInfoLiveData.getValue().getDeviceId());
                history.setTemps(Arrays.toString(temps));
                history.setTime(currentFirstTime);
                try {

                    if (floats.size() > 1) {
                        db.getHistoryDao().update(history);
                    } else {
                        db.getHistoryDao().insert(history);
                    }
                } catch (Exception e) {
                    Log.d(e.getMessage());
                }
                Map<String, String> params = new HashMap<>();
                params.put("devicesID", history.getDevicesID());
                params.put("memberID", String.valueOf(history.getMemberID()));
                params.put("time", String.valueOf(history.getTime()));
                params.put("temps", history.getTemps());
                params.put("name", TempApplication.currentLiveData.getValue().getName());
                TempApiManager.getInstance().updateRealTemp(params)
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Response<String>>() {
                            @Override
                            public void accept(Response<String> response) throws Exception {
                                Log.d(response.toString());
                                if ("000".equals(response.getStatus())) {
                                    history.setUpdated(true);
                                    db.getHistoryDao().update(history);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d(throwable.getMessage());
                            }
                        });
            }
        });

        BlueManager.historyTempLiveData.observe(

                getViewLifecycleOwner(), new Observer<HistoryTemp>() {
                    @Override
                    public void onChanged(HistoryTemp historyTemp) {
                        Log.d("HomeFragment historyTempLiveData onChanged ");
                        try {
                            if (historyTemp.getTempCount() > 0) {
                                // 存入数据库
                                History history = new History();
                                history.setTime(historyTemp.getStartTime());
                                history.setMemberID(historyTemp.getMemberId());
                                history.setDevicesID(BlueManager.tempInfoLiveData.getValue().getDeviceId());
                                history.setTemps(Arrays.toString(historyTemp.getTemps()));
                                dealWithHistory(history);
                                db.getHistoryDao().insert(history);
                                if (historyTemp.getStatus() == 1) {
                                    BlueManager.getInstance().send(ProtocolUtils.getHistoryTemp());
                                } else {
                                    TTSManager.getInstance().speek("历史体温数据获取完成");
                                }
                                Map<String, String> params = new HashMap<>();
                                params.put("devicesID", history.getDevicesID());
                                params.put("memberID", String.valueOf(history.getMemberID()));
                                params.put("time", String.valueOf(history.getTime()));
                                params.put("temps", history.getTemps());
                                TempApiManager.getInstance().updateHistory(params)
                                        .subscribeOn(Schedulers.io())
                                        .unsubscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Response<String>>() {
                                            @Override
                                            public void accept(Response<String> response) throws Exception {
                                                Log.d(response.toString());
                                                if ("000".equals(response.getStatus())) {
                                                    history.setUpdated(true);
                                                    db.getHistoryDao().update(history);
                                                }
                                            }
                                        }, new Consumer<Throwable>() {
                                            @Override
                                            public void accept(Throwable throwable) throws Exception {
                                                Log.d(throwable.getMessage());
                                            }
                                        });
                            }
                        } catch (Exception e) {
                            Log.d(e.getMessage());
                        }

                    }
                });

        BlueManager.memberLiveData.observe(

                getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean success) {
                        Log.d("HomeFragment memberLiveData onChanged ");
                        if (success) {
                            Log.d("HomeFragment memberLiveData onChanged send getTempStatus");
                            BlueManager.getInstance().send(ProtocolUtils.getTempStatus(System.currentTimeMillis()));
                        }
                    }
                });

        TempApplication.currentLiveData.observeForever(new Observer<Member>() {
            @Override
            public void onChanged(Member member) {
                Log.d("HomeFragment currentLiveData onChanged ");
                binding.name.setText(member.getName() + "的体温");
            }
        });

        BlueManager.highLiveData.observe(

                getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        String[] split = s.split("#");
                        binding.high.setText(split[0]);
                        binding.highTime.setText(TimeUtils.getHourStr(new Date(Long.valueOf(split[1]))));
                    }
                });
    }

    private void getHistory(TempInfo info) {
        MainActivity.executors.execute(new Runnable() {
            @Override
            public void run() {
                List<History> list = db.getHistoryDao().getAll(info.getDeviceId(), info.getMemberId());
                entryHistoryList = new ArrayList<>();
                if (list.size() == 0) {
                    getHistoryFromNet(info);
                } else {
                    updateHistoryToNet();
                    for (History history : list) {
                        dealWithHistory(history);
                    }
                    getNurseInfo();
                }
            }
        });
    }

    private void getNurseInfo() {
        List<Nurse> nurseList = db.getNurseDao().getAll();
        nurselCount = 0;
        for (Nurse nurse : nurseList) {
            if (TimeUtils.isSameDay(new Date(nurse.getTime()))) {
                nurselCount++;
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                binding.nusreCount.setText(String.valueOf(nurselCount));
            }
        });
    }

    private void updateHistoryToNet() {
        List<History> updateHistory = db.getHistoryDao().getUpdateHistory(false);
        for (History history : updateHistory) {
            MainActivity.executors.execute(new Runnable() {
                @Override
                public void run() {
                    Map<String, String> params = new HashMap<>();
                    params.put("devicesID", history.getDevicesID());
                    params.put("memberID", String.valueOf(history.getMemberID()));
                    params.put("time", String.valueOf(history.getTime()));
                    params.put("temps", history.getTemps());
                    TempApiManager.getInstance().updateHistory(params)
                            .subscribeOn(Schedulers.io())
                            .unsubscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Response<String>>() {
                                @Override
                                public void accept(Response<String> response) throws Exception {
                                    Log.d(response.toString());
                                    if ("000".equals(response.getStatus())) {
                                        history.setUpdated(true);
                                        db.getHistoryDao().update(history);
                                    }
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Log.d(throwable.getMessage());
                                }
                            });
                }
            });
        }
    }


    private void getHistoryFromNet(TempInfo info) {
        MainActivity.executors.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("devicesID", info.getDeviceId());
                params.put("memberID", String.valueOf(info.getMemberId()));
                TempApiManager.getInstance().getHistory(params)
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Response<List<History>>>() {
                            @Override
                            public void accept(Response<List<History>> response) throws Exception {
                                Log.d(response.toString());
                                if ("000".equals(response.getStatus())) {
                                    db.getHistoryDao().insert(response.getData());
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d(throwable.getMessage());
                            }
                        });
            }
        });

        MainActivity.executors.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("devicesID", info.getDeviceId());
                params.put("memberID", String.valueOf(info.getMemberId()));
                TempApiManager.getInstance().getNurseInfo(params)
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Response<List<Nurse>>>() {
                            @Override
                            public void accept(Response<List<Nurse>> response) throws Exception {
                                Log.d(response.toString());
                                if ("000".equals(response.getStatus())) {
                                    db.getNurseDao().insert(response.getData());
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d(throwable.getMessage());
                            }
                        });
            }
        });
    }

    private void setData(List<Entry> values) {
        if (values == null) {
            return;
        }
        currentList.clear();
        int normalTimeCount = 0, highTimeCount = 0, lowTimeCount = 0;
        for (Entry entry : values) {
            // 过去24小时
            if (entry.getTime() >= System.currentTimeMillis() - 24 * 60 * 60 * 1000) {
                currentList.add(entry);
            }
            float y = entry.getTemp();
            if (checkQueue.size() >= COUNT) {
                checkQueue.poll();
            }
            checkQueue.offer(y);
            check();
            if (y < LOW_TEMP_DIVIDER) {
                normalTimeCount += 10;
            } else if (y > HIGH_TEMP_DIVIDER) {
                highTimeCount += 10;
            } else {
                lowTimeCount += 10;
            }
        }
        binding.measure.setText(TimeUtils.getHourStrForSecond(new Date(currentList.size() * 10 * 1000)));
        binding.highTimeCount.setText(TimeUtils.getHourStrForSecond(new Date(highTimeCount * 1000)));
        binding.lowTimeCount.setText(TimeUtils.getHourStrForSecond(new Date(lowTimeCount * 1000)));
        binding.normalTimeCount.setText(TimeUtils.getHourStrForSecond(new Date(normalTimeCount * 1000)));
        binding.lineChart.setData(currentList);
    }

    private void check() {
        int highCount = 0;
        boolean high = false;
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
        binding.highCount.setText(String.valueOf(highCount));
    }

    private void updateName() {
        TempInfo tempInfo = BlueManager.tempInfoLiveData.getValue();
        if (null != tempInfo && tempInfo.getMemberId() != 0) {
            for (Member member : tempInfo.getMembers()) {
                if (member.getMemberId() == tempInfo.getMemberId()) {
                    binding.name.setText(member.getName() + "的体温");
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        TempInfo tempInfo = BlueManager.tempInfoLiveData.getValue();
        switch (v.getId()) {
            case R.id.name:
            case R.id.triangle:
                if (null != tempInfo && tempInfo.getMemberCount() > 0) {
                    Log.d("HomeFragment onClick go FamilyMemberListFragment");
                    Navigation.findNavController(v).navigate(R.id.action_HomeFragment_to_FamilyMemberListFragment);
                }
                break;
            case R.id.history:
                if (null != tempInfo && tempInfo.getMemberCount() > 0) {
                    Log.d("HomeFragment onClick go HistoryListFragment");
                    Navigation.findNavController(v).navigate(R.id.action_HomeFragment_to_HistoryListFragment);
                }
                break;
            case R.id.remind:
                Log.d("HomeFragment onClick go TempRemindListFragment");
                Navigation.findNavController(v).navigate(R.id.action_HomeFragment_to_TempRemindListFragment);
                break;
            case R.id.share:
                // if (null != tempInfo && tempInfo.getMemberCount() > 0) {
                Log.d("HomeFragment onClick go ShareFragment");
                Navigation.findNavController(v).navigate(R.id.action_HomeFragment_to_ShareFragment);
                // }
                break;
            case R.id.second:
                binding.second.setSelected(true);
                binding.two.setSelected(false);
                binding.six.setSelected(false);
                binding.twelve.setSelected(false);
                binding.twenty.setSelected(false);
                binding.lineChart.changeStyle(currentList, 0);
                break;
            case R.id.two:
                binding.second.setSelected(false);
                binding.two.setSelected(true);
                binding.six.setSelected(false);
                binding.twelve.setSelected(false);
                binding.twenty.setSelected(false);
                binding.lineChart.changeStyle(currentList, 1);
                break;
            case R.id.six:
                binding.second.setSelected(false);
                binding.two.setSelected(false);
                binding.six.setSelected(true);
                binding.twelve.setSelected(false);
                binding.twenty.setSelected(false);
                binding.lineChart.changeStyle(currentList, 2);
                break;
            case R.id.twelve:
                binding.second.setSelected(false);
                binding.two.setSelected(false);
                binding.six.setSelected(false);
                binding.twelve.setSelected(true);
                binding.twenty.setSelected(false);
                binding.lineChart.changeStyle(currentList, 3);
                break;
            case R.id.twenty:
                binding.second.setSelected(false);
                binding.two.setSelected(false);
                binding.six.setSelected(false);
                binding.twelve.setSelected(false);
                binding.twenty.setSelected(true);
                binding.lineChart.changeStyle(currentList, 4);
                break;
            case R.id.record:
                if (null != tempInfo && tempInfo.getMemberCount() > 0) {
                    Navigation.findNavController(v).navigate(R.id.action_HomeFragment_to_NurseFragment);
                }
                break;
        }
    }

    private void initUI(TempInfo info, CurrentTemp temp) {
        updateName();
        if (info != null) {
            binding.charging.setPower(info.getCharging());
            checkCharging(info.getCharging());
        }
        if (temp != null) {
            float t = temp.getTemp();
            binding.temp.setText(String.valueOf(t));
            binding.charging.setPower(temp.getCharging());
            dealWithPointer(t);
            checkCharging(temp.getCharging());
            checkNotify(t);
        }
    }

    private void checkNotify(float temp) {

        if (temp <= 35) {
            if (remindTag.get(35f)) {
                return;
            }
            // 播报
            binding.hint.setVisibility(View.VISIBLE);
            binding.hinttag.setVisibility(View.VISIBLE);
            binding.hint.setText("目前获取温度较低，确认设备正常贴在腋下");
            remindTag.put(35f, true);
            TTSManager.getInstance().speek("目前获取温度较低，确认设备正常贴在腋下");
            return;
        } else {
            binding.hint.setVisibility(View.INVISIBLE);
            binding.hinttag.setVisibility(View.INVISIBLE);
            if (highReminds != null && highReminds.length > 0) {
                for (int i = highReminds.length - 1; i >= 0; i--) {
                    float higher = highReminds[i];
                    if (temp > higher) {
                        if (remindTag.get(higher)) {
                            break;
                        }
                        remindTag.put(higher, true);
                        // 播报
                        TTSManager.getInstance().speek("当前体温高于所设的温度提醒" + higher + "°C，请注意查看护理!");
                        break;
                    } else {
                        remindTag.put(higher, false);
                    }
                }
            }
            if (lowReminds != null && lowReminds.length > 0) {
                for (int i = 0; i < lowReminds.length; i++) {
                    float low = lowReminds[i];
                    if (temp < low) {
                        if (remindTag.get(low)) {
                            break;
                        }
                        remindTag.put(low, true);
                        // 播报
                        TTSManager.getInstance().speek("当前体温低于所设的温度提醒" + low + "°C，请注意查看护理!");
                        break;
                    } else {
                        remindTag.put(low, false);
                    }
                }
            }
        }
    }

    private void dealWithPointer(float temp) {
        //#08BE62
        if (temp <= 37.3f) {
            binding.temp.setTextColor(Color.parseColor("#FF08BE62"));
            binding.tempUnit.setTextColor(Color.parseColor("#FF08BE62"));
        } else if (temp >= 37.4f && temp < 38) {
            binding.temp.setTextColor(Color.parseColor("#FFFFDE00"));
            binding.tempUnit.setTextColor(Color.parseColor("#FFFFDE00"));
        } else if (temp >= 38 && temp <= 39.5) {
            binding.temp.setTextColor(Color.parseColor("#FFFF9C01"));
            binding.tempUnit.setTextColor(Color.parseColor("#FFFF9C01"));
        } else {
            binding.temp.setTextColor(Color.parseColor("#FFFF0101"));
            binding.tempUnit.setTextColor(Color.parseColor("#FFFF0101"));
        }

        View pointer = binding.pointer;
        pointer.setPivotX(pointer.getWidth());
        pointer.setPivotY(pointer.getHeight() / 2);
        float from = pointer.getRotation();
        float to = (180 * (temp - lastTemp)) / 8f;
        Log.d("from = " + from + " to = " + to + " lastTemp = " + lastTemp + " temp =  " + temp);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, from + to);
        valueAnimator.setTarget(pointer);
        valueAnimator.setDuration(500).start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                pointer.setRotation(value);
            }
        });
        lastTemp = temp;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }
}