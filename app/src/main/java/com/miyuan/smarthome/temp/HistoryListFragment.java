package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.databinding.FragmentHistoryListBinding;
import com.miyuan.smarthome.temp.db.Member;
import com.miyuan.smarthome.temp.view.HistoryAdapter;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.List;

public class HistoryListFragment extends Fragment implements View.OnClickListener {

    private FragmentHistoryListBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentHistoryListBinding.inflate(inflater, container, false);
        }
        initView();
        return binding.getRoot();
    }

    List<Member> members = new ArrayList<>();

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("历史体温");
        MMKV mmkv = MMKV.defaultMMKV();
        String devicesID = mmkv.getString("devicesID", "");
        String memberName = mmkv.getString("memberName", "");
        int memberID = mmkv.getInt("memberID", 0);
        if (!TextUtils.isEmpty(devicesID)) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            binding.members.setLayoutManager(layoutManager);
            if (BlueManager.tempInfoLiveData.getValue() != null) {
                members = TempApplication._memberesLiveData.getValue();
            } else {
                members.clear();
                Member member = new Member();
                member.setName(memberName);
                member.setMemberId(memberID);
                members.add(member);
            }
            HistoryAdapter memberAdapter = new HistoryAdapter(members);
            binding.members.setAdapter(memberAdapter);
            memberAdapter.setOnItemClickListerner(new HistoryAdapter.OnItemClickListerner() {
                @Override
                public void onItemClick(int position) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("memberId", members.get(position).getMemberId());
                    Navigation.findNavController(getView()).navigate(R.id.action_HistoryListFragment_to_HistoryTempListFragment, bundle);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                Navigation.findNavController(v).navigateUp();
                break;
        }
    }
}