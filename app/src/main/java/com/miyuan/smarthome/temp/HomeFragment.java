package com.miyuan.smarthome.temp;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.blue.BoxEvent;
import com.miyuan.smarthome.temp.blue.ProtocolUtils;
import com.miyuan.smarthome.temp.databinding.FragmentHomeBinding;
import com.miyuan.smarthome.temp.db.CurrentTemp;
import com.miyuan.smarthome.temp.db.HistoryTemp;
import com.miyuan.smarthome.temp.db.Member;
import com.miyuan.smarthome.temp.db.TempInfo;
import com.miyuan.smarthome.temp.log.Log;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        Log.d("HomeFragment onCreateView");
        if (binding == null) {
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            initView();
        }
        return binding.getRoot();
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            BlueManager.getInstance().send(ProtocolUtils.getCurrentTemp());
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("HomeFragment onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HomeFragment onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("HomeFragment onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("HomeFragment onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("HomeFragment onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("HomeFragment onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        binding = null;
        Log.d("HomeFragment onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("HomeFragment onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("HomeFragment onCreate");
    }

    private void initView() {
        binding.history.setOnClickListener(this);
        binding.remind.setOnClickListener(this);
        binding.share.setOnClickListener(this);
        binding.record.setOnClickListener(this);
        binding.second.setOnClickListener(this);
        binding.second.setSelected(true);
        binding.six.setOnClickListener(this);
        binding.twelve.setOnClickListener(this);
        binding.twenty.setOnClickListener(this);
        binding.triangle.setOnClickListener(this);
        binding.name.setOnClickListener(this);

        binding.scan.setImageResource(R.drawable.scan_bg);
        AnimationDrawable drawable = (AnimationDrawable) binding.scan.getDrawable();
        drawable.start();

        BlueManager.getInstance().init(getActivity());

        BlueManager.connectStatusLiveData.observe(this.getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                Log.d("HomeFragment connectStatusLiveData onChanged " + integer);
                switch (integer) {
                    case BoxEvent.BLUE_CONNECTED:
                        binding.scanlayout.setVisibility(View.GONE);
                        binding.tempLayout.setVisibility(View.VISIBLE);
                        Log.d("HomeFragment onChanged send ");
                        BlueManager.getInstance().send(ProtocolUtils.getTempStatus(System.currentTimeMillis()));
                        break;
                    case BoxEvent.BLUE_SCAN_FAILED:
                        Log.d("HomeFragment onChanged go ScanFailFragment");
                        Navigation.findNavController(getView()).navigate(R.id.action_HomeFragment_to_ScanFailFragment);
                        break;

                }
            }
        });

        BlueManager.tempInfoLiveData.observe(this.getViewLifecycleOwner(), new Observer<TempInfo>() {
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
                // 定时获取实时温度
                handler.postDelayed(runnable, 0);//启动定时任务
//                handler.removeCallbacks(runnable);
                initUI(info, null);
            }
        });

        BlueManager.currentTempLiveData.observe(this.getViewLifecycleOwner(), new Observer<CurrentTemp>() {
            @Override
            public void onChanged(CurrentTemp temp) {
                Log.d("HomeFragment currentTempLiveData onChanged ");
                initUI(null, temp);
            }
        });

        BlueManager.historyTempLiveData.observe(this.getViewLifecycleOwner(), new Observer<HistoryTemp>() {
            @Override
            public void onChanged(HistoryTemp historyTemp) {
                Log.d("HomeFragment historyTempLiveData onChanged ");

            }
        });
    }

    private void updateName() {
        TempInfo tempInfo = BlueManager.tempInfoLiveData.getValue();
        if (null != tempInfo && tempInfo.getMemberId() != 0) {
            for (Member member : tempInfo.getMembers()) {
                if (member.getMemberId() == tempInfo.getMemberId()) {
                    binding.name.setText(member.getName());
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.name:
                TempInfo tempInfo = BlueManager.tempInfoLiveData.getValue();
                if (null != tempInfo && tempInfo.getMemberCount() > 0) {
                    Log.d("HomeFragment onClick go FamilyMemberListFragment");
                    Navigation.findNavController(v).navigate(R.id.action_HomeFragment_to_FamilyMemberListFragment);
                }
            case R.id.triangle:
                break;
            case R.id.history:
                break;
            case R.id.remind:
                Log.d("HomeFragment onClick go TempRemindListFragment");
                Navigation.findNavController(v).navigate(R.id.action_HomeFragment_to_TempRemindListFragment);
                break;
            case R.id.share:
                break;
            case R.id.second:
                binding.second.setSelected(true);
                binding.six.setSelected(false);
                binding.twelve.setSelected(false);
                binding.twenty.setSelected(false);
                break;
            case R.id.six:
                binding.second.setSelected(false);
                binding.six.setSelected(true);
                binding.twelve.setSelected(false);
                binding.twenty.setSelected(false);
                break;
            case R.id.twelve:
                binding.second.setSelected(false);
                binding.six.setSelected(false);
                binding.twelve.setSelected(true);
                binding.twenty.setSelected(false);
                break;
            case R.id.twenty:
                binding.second.setSelected(false);
                binding.six.setSelected(false);
                binding.twelve.setSelected(false);
                binding.twenty.setSelected(true);
                break;
            case R.id.record:
                Navigation.findNavController(v).navigate(R.id.action_HomeFragment_to_NurseFragment);
                break;
        }
    }

    private void initUI(TempInfo info, CurrentTemp temp) {
        updateName();
        if (info != null) {
            binding.charging.setPower(info.getCharging());
        }
        if (temp != null) {
            binding.temp.setText(String.valueOf(temp.getTemp()));
            binding.charging.setPower(temp.getCharging());
        }
    }
}