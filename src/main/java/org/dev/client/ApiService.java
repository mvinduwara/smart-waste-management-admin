package org.dev.client;

import retrofit2.http.POST;
import org.dev.dto.LoggingRequestDTO;
import org.dev.dto.TokenDTO;
import retrofit2.Call;
import retrofit2.http.Body;

public interface ApiService {
    @POST("auth/login")
    Call<TokenDTO> login(@Body LoggingRequestDTO request);

    @POST("auth/refresh")
    Call<TokenDTO> refreshToken(@Body TokenDTO tokenDto);
}
