package com.miyuan.smarthome.temp.net;

import com.miyuan.smarthome.temp.db.History;
import com.miyuan.smarthome.temp.db.Nurse;

import java.util.List;

import io.reactivex.Flowable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ITempApi {

    @POST("service/data/add")
    Flowable<Response<String>> updateRealTemp(@Body RequestBody body);

    @POST("service/data/getData")
    Flowable<Response<History>> getRealTemp(@Body RequestBody body);

    @POST("service/checkUp/add")
    Flowable<Response<String>> updateNurseInfo(@Body RequestBody body);

    @POST("service/checkUp/getCheckUp")
    Flowable<Response<List<Nurse>>> getNurseInfo(@Body RequestBody body);

    @POST("service/history/add")
    Flowable<Response<String>> updateHistory(@Body RequestBody body);

    @POST("service/history/adds")
    Flowable<Response<String>> updateHistories(@Body RequestBody body);

    @POST("service/history/getData")
    Flowable<Response<List<History>>> getHistory(@Body RequestBody body);
}
