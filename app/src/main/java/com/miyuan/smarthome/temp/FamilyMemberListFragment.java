package com.miyuan.smarthome.temp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.miyuan.smarthome.temp.blue.BlueManager;
import com.miyuan.smarthome.temp.blue.ProtocolUtils;
import com.miyuan.smarthome.temp.databinding.FragmentFamilyMemberListBinding;
import com.miyuan.smarthome.temp.db.Member;
import com.miyuan.smarthome.temp.db.TempInfo;
import com.miyuan.smarthome.temp.log.Log;
import com.miyuan.smarthome.temp.view.MemberAdapter;

import java.util.List;

public class FamilyMemberListFragment extends Fragment implements View.OnClickListener {

    private FragmentFamilyMemberListBinding binding;

    private List<Member> members;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentFamilyMemberListBinding.inflate(inflater, container, false);
        }
        initView();
        return binding.getRoot();
    }

    private void initView() {
        binding.titlelayout.back.setOnClickListener(this);
        binding.titlelayout.title.setText("成员列表");
        binding.titlelayout.back.setOnClickListener(this);
        binding.add.setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.members.setLayoutManager(layoutManager);
        members = BlueManager.tempInfoLiveData.getValue().getMembers();
        for (Member member : members) {
            if (member.getMemberId() == BlueManager.tempInfoLiveData.getValue().getMemberId()) {
                member.setChoice(true);
            }
        }
        MemberAdapter memberAdapter = new MemberAdapter(members);
        binding.members.setAdapter(memberAdapter);
        memberAdapter.setOnItemClickListerner(new MemberAdapter.OnItemClickListerner() {
            @Override
            public void onItemClick(int position) {
                ChangeMemberFragment.newInstance(position).show(getParentFragmentManager(), "");
            }
        });
        BlueManager.tempInfoLiveData.observe(getViewLifecycleOwner(), new Observer<TempInfo>() {
            @Override
            public void onChanged(TempInfo tempInfo) {
                Log.d("FamilyMemberListFragment onChanged send updateMember");
                members = tempInfo.getMembers();
                for (Member member : members) {
                    if (member.getMemberId() == tempInfo.getMemberId()) {
                        member.setChoice(true);
                    }
                }
                memberAdapter.setmList(members);
                memberAdapter.notifyDataSetChanged();
            }
        });

        BlueManager.memberLiveData.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                Log.d("FamilyMemberListFragment membearLiveData onChanged ");
                if (success) {
                    Log.d("FamilyMemberListFragment onChanged  navigateUp");
                    Navigation.findNavController(getView()).navigateUp();
                    Log.d("FamilyMemberListFragment onChanged send getTempStatus");
                    BlueManager.getInstance().send(ProtocolUtils.getTempStatus(System.currentTimeMillis()));
                } else {
                    Toast.makeText(getActivity(), "操作失败，请重试！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ChangeMemberFragment.confirmLiveData.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                int position = integer;
                if (position != -1) {
                    for (int i = 0; i < members.size(); i++) {
                        if (position == i) {
                            TempApplication._currentMemberLiveData.setValue(members.get(i));
                        }
                        members.get(i).setChoice(position == i);
                    }
                    BlueManager.getInstance().send(ProtocolUtils.updateMember(false, members.get(position).getMemberId(), ""));
                    memberAdapter.notifyDataSetChanged();
                }
            }
        });

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
                Log.d("FamilyMemberListFragment onClick navigateUp");
                Navigation.findNavController(v).navigateUp();
                break;
            case R.id.add:
                Log.d("onClick go CreateFamilyMemberFragment");
                Navigation.findNavController(v).navigate(R.id.action_FamilyMemberListFragment_to_CreateFamilyMemberFragment);
                break;
        }
    }
}