package com.miyuan.smarthome.temp.net;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.miyuan.smarthome.temp.db.History;
import com.miyuan.smarthome.temp.db.Nurse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

    public Flowable<Response<List<History>>> getRealTemp(@NonNull Map<String, String> params) {
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

    public Flowable<Response<List<History>>> getHistory(@NonNull Map<String, String> params) {
        return iTempApi.getHistory(getRequestBody(params));
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
        RequestBody requestBody = new FormBody.Builder().add("params", encrypt(requestData.toString())).build();
        return requestBody;
    }

    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @return
     * @since v1.0
     */
    private String encrypt(String content) {
        try {
            IvParameterSpec ips = new IvParameterSpec("06E81ac8BE275800".getBytes());
            SecretKeySpec sks = new SecretKeySpec("06E81ac8BE275800".getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sks, ips);
            byte[] encryptedData = cipher.doFinal(content.getBytes());

            String res = parseByte2HexStr(encryptedData);
            return encodeData(res); // 加密
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     * @since v1.0
     */
    private String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 对给定的字符串进行base64加密操作
     *
     * @param inputData
     * @return
     * @throws UnsupportedEncodingException
     */
    private String encodeData(String inputData) throws UnsupportedEncodingException {
        if (null == inputData) {
            return null;
        }
        String string = Base64.encodeToString(inputData.getBytes(), Base64.DEFAULT);
        return string;
    }

    public static class InstanceHolder {
        private static final TempApiManager INSTANCE = new TempApiManager();
    }


}
