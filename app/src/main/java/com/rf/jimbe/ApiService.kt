package com.rf.jimbe

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("api/v1/exercises")
    fun getWorkoutKatalog(
        @Query("limit") limit: Int = 20
    ): Call<ExerciseWrapper>
}