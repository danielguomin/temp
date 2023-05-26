package com.miyuan.smarthome.temp;

import android.Manifest;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.databinding.ActivityMainBinding;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private List<String> mPermissionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initPermission();
    }

    private void initPermission() {
        mPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        mPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        mPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }

        if (mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, mPermissionList.toArray(new String[0]), 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 有权限没有通过
        boolean hasPermissionDismiss = false;
        if (1001 == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                    break;
                }
            }
        }
        if (hasPermissionDismiss) {
            // 有权限未通过的处理
        } else {
            //权限全部通过的处理
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            NavGraph graph = navController.getNavInflater().inflate(R.navigation.nav_graph);
            MMKV mmkv = MMKV.defaultMMKV();
            if (mmkv.getBoolean("no_prompt", false)) {
                graph.setStartDestination(R.id.HomeFragment);
            } else {
                graph.setStartDestination(R.id.DisclaimerFragment);
            }
            navController.setGraph(graph);
        }
    }
}