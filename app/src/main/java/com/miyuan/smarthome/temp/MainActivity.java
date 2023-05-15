package com.miyuan.smarthome.temp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;

import com.miyuan.smarthome.temp.databinding.ActivityMainBinding;
import com.tencent.mmkv.MMKV;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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