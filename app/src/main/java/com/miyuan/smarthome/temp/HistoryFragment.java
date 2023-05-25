package com.miyuan.smarthome.temp;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.databinding.FragmentHistoryBinding;
import com.miyuan.smarthome.temp.db.History;
import com.miyuan.smarthome.temp.db.TempDataBase;
import com.miyuan.smarthome.temp.log.Log;
import com.miyuan.smarthome.temp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoryFragment extends Fragment implements View.OnClickListener, DatePicker.OnDateChangedListener {

    private FragmentHistoryBinding binding;

    private TempDataBase db;
    private List<History> historyList;
    private List<Entry> entryList;
    private XAxis xAxis;                //X轴
    private YAxis leftYAxis;            //左侧Y轴
    private YAxis rightYaxis;           //右侧Y轴
    private Legend legend;              //图例

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
            binding = FragmentHistoryBinding.inflate(inflater, container, false);
            initView();
            initChart(binding.lineChart);
            db = Room.databaseBuilder(getContext(), TempDataBase.class, "database_temp").allowMainThreadQueries().build();
            getHistory();
        return binding.getRoot();
    }

    private void getHistory() {
        int memberId = getArguments().getInt("memberId");
        String deviceId = BlueManager.tempInfoLiveData.getValue().getDeviceId();
        historyList = db.getHistoryDao().getAll(deviceId, memberId);
        Log.d("HistoryFragment " + historyList.size());
        isSameDay(new Date(System.currentTimeMillis()));
    }

    private void isSameDay(Date date) {
        if (historyList == null || historyList.size() <= 0) {
            return;
        }
        entryList = new ArrayList<>();
        for (History history : historyList) {
            if (TimeUtils.isSameDay(new Date(history.getTime()), date)) {
                Log.d("isSameDay ");
                long start = history.getTime();
                String temps1 = history.getTemps();
                String[] temps = temps1.substring(1, temps1.length() - 1).split(",");
                for (int i = 0; i < temps.length; i++) {
                    Entry entry = new Entry(TimeUtils.getSecondForDate(start + i * 10), Float.valueOf(temps[i]));
                    entryList.add(entry);
                }
            }
        }
        setData(entryList);
    }

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

    private void initChart(LineChart lineChart) {
        /***图表设置***/
        //是否展示网格线
        lineChart.setDrawGridBackground(false);
        //是否显示边界
        lineChart.setDrawBorders(true);
        //是否可以拖动
        lineChart.setDragEnabled(false);
        //是否有触摸事件
        lineChart.setTouchEnabled(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.setScaleEnabled(true);
        //设置XY轴动画效果
        lineChart.animateY(1000);
        lineChart.animateX(1000);
        /***XY轴的设置***/
        xAxis = lineChart.getXAxis();
        leftYAxis = lineChart.getAxisLeft();
        rightYaxis = lineChart.getAxisRight();
        rightYaxis.setEnabled(false);
        //X轴设置显示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setSpaceMin(1);
        xAxis.setSpaceMax(1);
        xAxis.setAxisMaximum(24 * 60 * 60);
//        xAxis.setLabelCount(5, true);
        //保证Y轴从0开始，不然会上移一点
        leftYAxis.setAxisMinimum(35);
        leftYAxis.setAxisMaximum(42);
        leftYAxis.setLabelCount(5);
        leftYAxis.setSpaceMax(1);
        leftYAxis.setSpaceMin(1);
        setHightLimitLine(38.5f);
        setLowLimitLine(37.3f);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) (value / 3600) + "时";
            }
        });
        leftYAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(value) + "°C";
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

    private void setData(List<Entry> values) {
        LineData data1 = binding.lineChart.getData();
        if (null != data1) {
            binding.lineChart.clear();
        }
        xAxis.setAxisMaximum(24 * 60 * 60);
        // 创建一个数据集,并给它一个类型
        LineDataSet lineDataSet = new LineDataSet(values, "");
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
    }
}