package com.miyuan.smarthome.temp;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.miyuan.smarthome.temp.databinding.FragmentHomeShowBinding;
import com.miyuan.smarthome.temp.db.Entry;
import com.miyuan.smarthome.temp.db.History;
import com.miyuan.smarthome.temp.log.Log;
import com.miyuan.smarthome.temp.net.Response;
import com.miyuan.smarthome.temp.net.TempApiManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class HomeShowFragment extends Fragment implements View.OnClickListener {

    Handler handler = new Handler();
    private FragmentHomeShowBinding binding;
    private List<Entry> currentList = new ArrayList<>();
    private float lastTemp = 34;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // 发送请求
            getData();
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            Log.d("HomeShowFragment onCreateView");
            binding = FragmentHomeShowBinding.inflate(inflater, container, false);
        }
        initView();
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 0);//启动定时任务
        return binding.getRoot();
    }


    private void getData() {
        String deviceId = getArguments().getString("deviceId");
        MainActivity.executors.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> map = new HashMap<>();
                map.put("devicesID", deviceId);
                TempApiManager.getInstance().getRealTemp(map)
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Response<History>>() {
                            @Override
                            public void accept(Response<History> response) throws Exception {
                                Log.d(response.toString());
                                if ("000".equals(response.getStatus())) {
                                    History history = response.getDatas();
                                    dealWithHistory(history);
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

    private void dealWithHistory(History history) {

        binding.name.setText(history.getName() + "的体温");

        currentList.clear();
        long start = history.getTime();
        String temps1 = history.getTemps();
        String[] temps = temps1.substring(1, temps1.length() - 1).split(",");
        for (int i = 0; i < temps.length; i++) {
            Entry entry = new Entry(start + i * 10 * 1000, Float.valueOf(temps[i]));
            currentList.add(entry);
        }
        dealWithPointer(currentList.get(currentList.size() - 1).getTemp());
        setData(currentList);
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

    private void initView() {
        binding.history.setVisibility(View.INVISIBLE);
        binding.remind.setVisibility(View.INVISIBLE);
        binding.share.setVisibility(View.INVISIBLE);
        binding.share.setVisibility(View.INVISIBLE);
        binding.name.setVisibility(View.INVISIBLE);
        binding.charging.setVisibility(View.INVISIBLE);
        binding.triangle.setVisibility(View.INVISIBLE);
    }


    private void setData(List<Entry> values) {
        if (values == null) {
            return;
        }
        binding.lineChart.changeStyle(currentList, 4);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.name:
            case R.id.triangle:
            case R.id.history:
            case R.id.remind:
            case R.id.share:
            case R.id.second:
            case R.id.record:
                break;
        }
    }


    private void dealWithPointer(float temp) {
        binding.temp.setText(String.valueOf(temp));
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
}