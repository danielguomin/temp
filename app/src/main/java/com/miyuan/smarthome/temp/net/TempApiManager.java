package com.miyuan.smarthome.temp.net;

import androidx.annotation.NonNull;

import com.miyuan.smarthome.temp.db.History;
import com.miyuan.smarthome.temp.db.Nurse;
import com.miyuan.smarthome.temp.log.Log;
import com.miyuan.smarthome.temp.utils.AesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class TempApiManager {

    private ITempApi iTempApi;
    private Retrofit tempRetrofit;


    private TempApiManager() {
        initRetrofit();
        iTempApi = tempRetrofit.create(ITempApi.class);
    }

    public static TempApiManager getInstance() {
        return TempApiManager.InstanceHolder.INSTANCE;
    }

    private OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLogger());
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .retryOnConnectionFailure(false) //失败重连
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    private void initRetrofit() {
        tempRetrofit = new Retrofit.Builder()
                .baseUrl("http://wt.iccm.cn/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getOkHttpClient()) //使用自己创建的OkHttp
                .build();
    }

    public Flowable<Response<String>> updateRealTemp(@NonNull Map<String, String> params) {
        return iTempApi.updateRealTemp(getRequestBody(params));
    }

    public Flowable<Response<History>> getRealTemp(@NonNull Map<String, String> params) {
        return iTempApi.getRealTemp(getRequestBody(params));
    }


    public Flowable<Response<String>> updateNurseInfo(@NonNull Map<String, String> params) {
        return iTempApi.updateNurseInfo(getRequestBody(params));
    }

    public Flowable<Response<List<Nurse>>> getNurseInfo(@NonNull Map<String, String> params) {
        return iTempApi.getNurseInfo(getRequestBody(params));
    }


    public Flowable<Response<String>> updateHistory(@NonNull Map<String, String> params) {
        return iTempApi.updateHistory(getRequestBody(params));
    }

    public Flowable<Response<String>> updateHistoryList(@NonNull Map<String, String> params) {
        return iTempApi.updateHistories(getRequestBody(params));
    }


    public Flowable<Response<List<History>>> getHistory(@NonNull Map<String, String> params) {
        return iTempApi.getHistory(getRequestBody(params));
    }

    public Flowable<Response<String>> getHistoryCount(@NonNull Map<String, String> params) {
        return iTempApi.getHistoryCount(getRequestBody(params));
    }


    private RequestBody getRequestBody(@NonNull Map<String, String> map) {
        JSONObject requestData = new JSONObject();
        for (String key : map.keySet()) {
            try {
                requestData.put(key, map.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(requestData.toString());
        RequestBody requestBody = new FormBody.Builder().add("params", AesUtils.encrypt(requestData.toString())).build();
        return requestBody;
    }

    public static class InstanceHolder {
        private static final TempApiManager INSTANCE = new TempApiManager();
    }


}
