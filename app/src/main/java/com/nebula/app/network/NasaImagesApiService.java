package com.nebula.app.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/** NASA Image and Video Library. Free, HTTPS, no API key needed. */
public interface NasaImagesApiService {
    @GET("search")
    Call<NasaImagesResponse> search(@Query("q") String query,
                                    @Query("media_type") String mediaType,
                                    @Query("page_size") int pageSize);
}
