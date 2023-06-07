package com.miyuan.smarthome.temp;

import static com.miyuan.smarthome.temp.TempApplication.HIGH_TEMP_DIVIDER;
import static com.miyuan.smarthome.temp.TempApplication.LOW_TEMP_DIVIDER;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.blue.BoxEvent;
import com.miyuan.smarthome.temp.blue.ProtocolUtils;
import com.miyuan.smarthome.temp.databinding.FragmentHomeBinding;
import com.miyuan.smarthome.temp.db.CurrentTemp;
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

public class HomeFragment extends Fragment implements View.OnClickListener, OnChartGestureListener, OnChartValueSelectedListener {

    private FragmentHomeBinding binding;

    private TempDataBase db;
    private List<Entry> entryHistoryList;
    private List<Entry> currentList;
    private XAxis xAxis;                //X轴
    private YAxis leftYAxis;            //左侧Y轴
    private YAxis rightYaxis;           //右侧Y轴
    private Legend legend;              //图例

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
            BlueManager.getInstance().init(getActivity());
        }
        initView();
        initChart(binding.lineChart);
        return binding.getRoot();
    }

    Queue<Float> checkQueue = new LinkedList<>();

    private void dealWithHistory(History history) {
        if (TimeUtils.isSameDay(new Date(history.getTime())) || TimeUtils.isYesterday(new Date(history.getTime()))) {
            long start = history.getTime();
            String temps1 = history.getTemps();
            String[] temps = temps1.substring(1, temps1.length() - 1).split(",");
            for (int i = 0; i < temps.length; i++) {
                Entry entry = new Entry(TimeUtils.getSecondForDate(start + i * 10 * 1000), Float.valueOf(temps[i]));
                entryHistoryList.add(entry);
            }
        }
    }

    private long currentFirstTime;


    /**
     * 设置高限制线
     *
     * @param high
     */
    public void setHightLimitLine(float high) {
        LimitLine hightLimit = new LimitLine(high, "38.5");
        hightLimit.setTextSize(10f);
        hightLimit.setLineColor(Color.parseColor("#FFFFA326"));
        hightLimit.setTextColor(Color.parseColor("#FFFFA326"));
        hightLimit.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        leftYAxis.addLimitLine(hightLimit);
        binding.lineChart.invalidate();
    }

    /**
     * 设置低限制线
     *
     * @param low
     */
    public void setLowLimitLine(float low) {
        LimitLine hightLimit = new LimitLine(low, "37.3");
        hightLimit.setLineColor(Color.parseColor("#FFFFDE00"));
        hightLimit.setTextColor(Color.parseColor("#FFFFDE00"));
        hightLimit.setTextSize(10f);
        hightLimit.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        leftYAxis.addLimitLine(hightLimit);
        binding.lineChart.invalidate();
    }


    @Override
    public void onResume() {
        super.onResume();
        handler.post(runnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    private int nurselCount = 0;
    private int highTimeCount = 0;
    private int lowTimeCount = 0;
    private int normalTimeCount = 0;
    private int highCount = 0;
    private boolean high = false;

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
        lastTemp = 34;
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
            highReminds.toArray(this.highReminds);
            Arrays.sort(this.highReminds);
        }
        if (null != lowReminds && lowReminds.size() > 0) {
            lowReminds.toArray(this.lowReminds);
            Arrays.sort(this.lowReminds);
        }

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
                handler.postDelayed(runnable, 0);//启动定时任务

                getHistory(info);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!charging[2]) {
                            TTSManager.getInstance().speek("正在获取历史体温数据，请勿关闭APP");
                        }
                        BlueManager.getInstance().send(ProtocolUtils.getHistoryTemp());
                    }
                }, 1000);
            }
        });

        BlueManager.currentTempLiveData.observe(getViewLifecycleOwner(), new Observer<CurrentTemp>() {
            @Override
            public void onChanged(CurrentTemp temp) {
                Log.d("HomeFragment currentTempLiveData onChanged ");
                initUI(null, temp);
            }
        });

        BlueManager.currentList.observe(getViewLifecycleOwner(), new Observer<List<Float>>() {
            @Override
            public void onChanged(List<Float> floats) {
                Log.d("HomeFragment currentList onChanged " + floats);
                List<Entry> entryList = new ArrayList<>();
                if (entryHistoryList != null) {
                    entryList.addAll(entryHistoryList);
                }
                float[] temps = new float[floats.size()];
                for (int i = 0; i < floats.size(); i++) {
                    temps[i] = floats.get(i);
                    Entry entry = new Entry(TimeUtils.getSecondForDate(System.currentTimeMillis() + i * 10 * 1000), floats.get(i));
                    entryList.add(entry);
                }
                if (currentFirstTime == 0) {
                    currentFirstTime = System.currentTimeMillis();
                    binding.measure.setText(TimeUtils.getHourStr(new Date(currentFirstTime)));
                }
                setData(entryList);
                History history = new History();
//                history.setMemberID(2);
                history.setMemberID(TempApplication.currentLiveData.getValue().getMemberId());
                history.setDeviceID(BlueManager.tempInfoLiveData.getValue().getDeviceId());
//                history.setDeviceID("XXXXXXXXXXXXXXX1");
                history.setTemps(Arrays.toString(temps));
                history.setTime(currentFirstTime);
                if (!TimeUtils.isSameDay(new Date(currentFirstTime))) {
                    currentFirstTime = System.currentTimeMillis();
                    history.setTime(currentFirstTime);
                    db.getHistoryDao().insert(history);
                } else {
                    db.getHistoryDao().update(history);
                }
                Map<String, String> params = new HashMap<>();
                params.put("devicesID", history.getDeviceID());
                params.put("memberID", String.valueOf(history.getMemberID()));
                params.put("time", String.valueOf(history.getTime()));
                params.put("temps", history.getTemps());
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

        BlueManager.historyTempLiveData.observe(getViewLifecycleOwner(), new Observer<HistoryTemp>() {
            @Override
            public void onChanged(HistoryTemp historyTemp) {
                Log.d("HomeFragment historyTempLiveData onChanged ");
                if (historyTemp.getTempCount() > 0) {
                    // 存入数据库
                    History history = new History();
                    history.setTime(historyTemp.getStartTime());
                    history.setMemberID(historyTemp.getMemberId());
                    history.setDeviceID(BlueManager.tempInfoLiveData.getValue().getDeviceId());
                    history.setTemps(Arrays.toString(historyTemp.getTemps()));
                    dealWithHistory(history);
                    db.getHistoryDao().insert(history);
                    if (historyTemp.getStatus() == 1) {
                        BlueManager.getInstance().send(ProtocolUtils.getHistoryTemp());
                    } else {
                        TTSManager.getInstance().speek("历史体温数据获取完成");
                    }
                    Map<String, String> params = new HashMap<>();
                    params.put("devicesID", history.getDeviceID());
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
            }
        });

        BlueManager.memberLiveData.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
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

        BlueManager.highLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                String[] split = s.split("#");
                binding.high.setText(split[0]);
                binding.highTime.setText(TimeUtils.getHourStr(new Date(Long.valueOf(split[1]))));
            }
        });
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

    private void getHistory(TempInfo info) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<History> list = db.getHistoryDao().getAll(info.getDeviceId(), info.getMemberId());
                entryHistoryList = new ArrayList<>();
                for (History history : list) {
                    dealWithHistory(history);
                }
                Log.d("entryHistoryList " + entryHistoryList.size());
                List<Nurse> nurseList = db.getNurseDao().getAll();
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
        }).start();
    }

    private void initChart(LineChart lineChart) {
        /***图表设置***/
        //是否展示网格线
        lineChart.setDrawGridBackground(false);
        //是否显示边界
        lineChart.setDrawBorders(true);
        Description description = new Description();
        description.setEnabled(false);
        lineChart.setDescription(description);
        //是否可以拖动
        lineChart.setDragEnabled(true);
        //是否有触摸事件
        lineChart.setTouchEnabled(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.setScaleEnabled(true);
        //设置XY轴动画效果
        lineChart.animateY(1000);
        lineChart.animateX(1000);
        lineChart.setDrawBorders(false);
        lineChart.setDragXEnabled(true);
        /***XY轴的设置***/
        xAxis = lineChart.getXAxis();
        leftYAxis = lineChart.getAxisLeft();
        rightYaxis = lineChart.getAxisRight();
        rightYaxis.setEnabled(false);
        //X轴设置显示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
//        xAxis.setSpaceMin(1);
//        xAxis.setSpaceMax(1);
        xAxis.setAxisMaximum(24 * 60 * 60);
//        xAxis.setLabelCount(5, true);
        //保证Y轴从0开始，不然会上移一点
        leftYAxis.setAxisMinimum(35);
        leftYAxis.setAxisMaximum(42);
//        leftYAxis.setSpaceMax(1);
//        leftYAxis.setSpaceMin(1);
        setHightLimitLine(38.5f);
        setLowLimitLine(37.3f);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (binding.second.isSelected()) {
                    return (int) (value / 3600) + "分";
                } else {
                    return (int) (value / 3600) + "时";
                }
            }
        });
        leftYAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {

                return (float) Math.round(value * 10) / 10 + "°C";
            }
        });
        /***折线图例 标签 设置***/
        legend = lineChart.getLegend();
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        //显示位置 左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);
    }

    private void setData(List<Entry> values) {
        currentList = values;
        LineData data1 = binding.lineChart.getData();
        if (null != data1) {
            binding.lineChart.clear();
        }
        int currentTime = TimeUtils.getSecondForDate(System.currentTimeMillis());
        List<Entry> realValue = new ArrayList<>();
        int min = (int) values.get(0).getX();
        if (binding.second.isSelected()) {
            // >= currentTime - 15*60
            min = currentTime - (15 * 60);
        } else if (binding.two.isSelected()) {
            min = currentTime - 1 * 60 * 60;
        } else if (binding.six.isSelected()) {
            min = currentTime - 3 * 60 * 60;
        } else if (binding.twelve.isSelected()) {
            min = currentTime - 6 * 60 * 60;
        }
        for (Entry entry : values) {
            float y = entry.getY();
            if (entry.getX() >= min) {
                realValue.add(entry);
            }
            if (checkQueue.size() >= COUNT) {
                checkQueue.poll();
            }
            checkQueue.offer(y);
            check();
            if (y <= LOW_TEMP_DIVIDER) {
                normalTimeCount += 10;
            } else if (y >= HIGH_TEMP_DIVIDER) {
                highTimeCount += 10;
            } else {
                lowTimeCount += 10;
            }
        }
        binding.highCount.setText(String.valueOf(highCount));
        binding.highTimeCount.setText(TimeUtils.getHourStrForSecond(new Date(highTimeCount * 1000)));
        binding.lowTimeCount.setText(TimeUtils.getHourStrForSecond(new Date(lowTimeCount * 1000)));
        binding.normalTimeCount.setText(TimeUtils.getHourStrForSecond(new Date(normalTimeCount * 1000)));

        if (binding.second.isSelected()) {
            xAxis.setAxisMinimum(min);
            xAxis.setAxisMaximum(currentTime + 60 * 60);
        } else {
            xAxis.setAxisMaximum(24 * 60 * 60);
            xAxis.setAxisMinimum(min);
        }
        // 创建一个数据集,并给它一个类型
        LineDataSet lineDataSet = new LineDataSet(realValue, "");
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        // 在这里设置线
        lineDataSet.setColor(Color.parseColor("#FF2BAC69"));
        lineDataSet.setDrawCircles(false);
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setCircleRadius(2f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(0);
        lineDataSet.setDrawFilled(false);
        lineDataSet.setFormLineWidth(0f);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        //添加数据集
        dataSets.add(lineDataSet);
        //创建一个数据集的数据对象
        LineData data = new LineData(dataSets);
        //谁知数据
        binding.lineChart.setData(data);
        setChartFillDrawable(getContext().getDrawable(R.drawable.linechart_bg));
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
        binding.highCount.setText(String.valueOf(highCount));
    }

    /**
     * 设置线条填充背景颜色
     *
     * @param drawable
     */
    public void setChartFillDrawable(Drawable drawable) {
        if (binding.lineChart.getData() != null && binding.lineChart.getData().getDataSetCount() > 0) {
            LineDataSet lineDataSet = (LineDataSet) binding.lineChart.getData().getDataSetByIndex(0);
            //避免在 initLineDataSet()方法中 设置了 lineDataSet.setDrawFilled(false); 而无法实现效果
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillDrawable(drawable);
            binding.lineChart.invalidate();
        }
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
                if (null != currentList) {
                    setData(currentList);
                }
                break;
            case R.id.two:
                binding.second.setSelected(false);
                binding.two.setSelected(true);
                binding.six.setSelected(false);
                binding.twelve.setSelected(false);
                binding.twenty.setSelected(false);
                if (null != currentList) {
                    setData(currentList);
                }
                break;
            case R.id.six:
                binding.second.setSelected(false);
                binding.two.setSelected(false);
                binding.six.setSelected(true);
                binding.twelve.setSelected(false);
                binding.twenty.setSelected(false);
                if (null != currentList) {
                    setData(currentList);
                }
                break;
            case R.id.twelve:
                binding.second.setSelected(false);
                binding.two.setSelected(false);
                binding.six.setSelected(false);
                binding.twelve.setSelected(true);
                binding.twenty.setSelected(false);
                if (null != currentList) {
                    setData(currentList);
                }
                break;

            case R.id.twenty:
                binding.second.setSelected(false);
                binding.two.setSelected(false);
                binding.six.setSelected(false);
                binding.twelve.setSelected(false);
                binding.twenty.setSelected(true);
                if (null != currentList) {
                    setData(currentList);
                }
                break;
            case R.id.record:
//                List<Float> floats1 = new ArrayList<>();
//                floats1.add(36.5f);
//                floats1.add(36.1f);
//                floats1.add(36.2f);
//                floats1.add(36.3f);
//                floats1.add(36.4f);
//                floats1.add(36.5f);
//                floats1.add(36.6f);
//                floats1.add(36.7f);
//                floats1.add(36.8f);
//                floats1.add(36.9f);
//                floats1.add(37.0f);
//                floats1.add(37.1f);
//                floats1.add(37.2f);
//                floats1.add(37.3f);
//                floats1.add(37.4f);
//                floats1.add(37.5f);
//                floats1.add(37.6f);
//                floats1.add(37.7f);
//                floats1.add(36.8f);
//                floats1.add(36.9f);
//                floats1.add(37.9f);
//                floats1.add(36.1f);
//                floats1.add(36.2f);
//                BlueManager._currentList.postValue(floats1);
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

    private float currentRemind = 0;

    private void checkNotify(float temp) {

        if (temp <= 35) {
            if (currentRemind == 35 || remindTag.get(35f)) {
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
                for (int i = highReminds.length - 1; i <= 0; i--) {
                    float higher = highReminds[i];
                    if (temp >= highReminds[i]) {
                        if (currentRemind == higher || remindTag.get(higher)) {
                            return;
                        }
                        currentRemind = higher;
                        remindTag.put(higher, true);
                        // 播报
                        TTSManager.getInstance().speek("当前体温高于所设的温度提醒" + higher + "°C，请注意查看护理!");
                        break;
                    }
                }
            }
            if (lowReminds != null && lowReminds.length > 0) {
                for (int i = 0; i < lowReminds.length; i++) {
                    float low = lowReminds[i];
                    if (temp <= low) {
                        if (currentRemind == low || remindTag.get(low)) {
                            return;
                        }
                        currentRemind = low;
                        remindTag.put(low, true);
                        // 播报
                        TTSManager.getInstance().speek("当前体温低于所设的温度提醒" + low + "°C，请注意查看护理!");
                        break;
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
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture
            lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture
            lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
    }

    @Override
    public void onNothingSelected() {
    }
}