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
import androidx.room.Room;

import com.miyuan.smarthome.temp.databinding.FragmentTempRemindListBinding;
import com.miyuan.smarthome.temp.db.Remind;
import com.miyuan.smarthome.temp.db.TempDataBase;
import com.miyuan.smarthome.temp.log.Log;
import com.miyuan.smarthome.temp.view.RemindAdapter;

import java.util.ArrayList;
import java.util.List;

public class TempRemindListFragment extends Fragment implements View.OnClickListener {

    private FragmentTempRemindListBinding binding;
    private RemindAdapter remindAdapter;
    private TempDataBase db;
    private List<Remind> reminds;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        if (binding == null) {
            binding = FragmentTempRemindListBinding.inflate(inflater, container, false);
        }
        initView();
        return binding.getRoot();
    }

    private void initView() {
        binding.back.setOnClickListener(this);
        binding.add.setOnClickListener(this);
        binding.edit.setOnClickListener(this);
        binding.cancel.setOnClickListener(this);
        binding.confirm.setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.reminds.setLayoutManager(layoutManager);
        remindAdapter = new RemindAdapter();
        binding.reminds.setAdapter(remindAdapter);
        db = Room.databaseBuilder(getContext(), TempDataBase.class, "database_temp").allowMainThreadQueries().build();
        reminds = db.getRemindDao().getAll();
        binding.edit.setVisibility(reminds.size() > 0 ? View.VISIBLE : View.INVISIBLE);
        remindAdapter.setmList(reminds);
        remindAdapter.setOnItemClickListerner(new RemindAdapter.OnItemClickListerner() {
            @Override
            public void onItemClick(int position) {
                // 编辑模式
                Remind remind = reminds.get(position);
                if (remind.isLock()) {
                    Toast.makeText(getActivity(), "默认提示数据无法编辑！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (binding.edit.isSelected()) {
                    remind.setChoice(!remind.isChoice());
                    remindAdapter.notifyItemChanged(position);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("remind", remind);
                    Navigation.findNavController(getView()).navigate(R.id.action_TempRemindListFragment_to_TempRemindAddFragment, bundle);
                }
            }
        });
        TempPickerFragment.tempLiveData.observe(getViewLifecycleOwner(), new Observer<Remind>() {
            @Override
            public void onChanged(Remind remind) {
                try {
                    Log.d(remind.toString());
                    db.getRemindDao().update(remind);
                    remindAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "更新失败！", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        db.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                Navigation.findNavController(v).navigateUp();
                break;
            case R.id.edit:
            case R.id.cancel:
                boolean edit = binding.edit.isSelected();
                if (edit) {
                    binding.edit.setText("编辑");
                    binding.add.setVisibility(View.VISIBLE);
                    binding.btnLayout.setVisibility(View.GONE);
                } else {
                    binding.edit.setText("完成");
                    binding.add.setVisibility(View.GONE);
                    binding.btnLayout.setVisibility(View.VISIBLE);
                }
                binding.edit.setSelected(!edit);
                remindAdapter.setEdit(binding.edit.isSelected());
                remindAdapter.notifyDataSetChanged();
                break;
            case R.id.add:
                Navigation.findNavController(v).navigate(R.id.action_TempRemindListFragment_to_TempRemindAddFragment);
                break;
            case R.id.confirm:
                List<Remind> delete = new ArrayList<>();
                for (Remind remind : reminds) {
                    if (remind.isChoice() && !remind.isLock()) {
                        delete.add(remind);
                    }
                }
                reminds.removeAll(delete);
                remindAdapter.notifyDataSetChanged();
                db.getRemindDao().delete(delete);
                if (reminds.size() == 0) {
                    binding.edit.setText("编辑");
                    binding.edit.setSelected(false);
                    binding.edit.setVisibility(View.INVISIBLE);
                    binding.add.setVisibility(View.VISIBLE);
                    binding.btnLayout.setVisibility(View.GONE);
                }
                break;
        }
    }
}