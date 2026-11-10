package com.nebula.app.network;

import com.nebula.app.data.model.IssPosition;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IssApiService {
    @GET("v1/satellites/25544")
    Call<IssPosition> getIssPosition();

    /** timestamps: comma separated unix seconds (max 10). */
    @GET("v1/satellites/25544/positions")
    Call<List<IssPosition>> getPositions(@Query("timestamps") String timestamps);
}
