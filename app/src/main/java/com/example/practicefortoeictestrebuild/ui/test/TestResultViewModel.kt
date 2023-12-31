package com.example.practicefortoeictestrebuild.ui.test

import android.app.Dialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.practicefortoeictestrebuild.MyApplication
import com.example.practicefortoeictestrebuild.api.ApiHelper
import com.example.practicefortoeictestrebuild.api.ApiResponse
import com.example.practicefortoeictestrebuild.api.ApiService
import com.example.practicefortoeictestrebuild.base.BaseViewModel
import com.example.practicefortoeictestrebuild.model.QuestionCard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime

class TestResultViewModel : BaseViewModel() {

    var startTime: LocalDateTime? = null

    var endTime: LocalDateTime? = null

    fun updateCard(cardId: String, answer: String) {
        updateStatusCard(cardId, answer)
        updateProgressCalendar(cardId)
    }

    private fun updateStatusCard(cardId: String, answer: String) {
        val apiService = ApiHelper.getInstance().create(ApiService::class.java)
        apiService.updateProgressCard(MyApplication.getToken(), cardId, "0", answer)
            .enqueue(object : Callback<ApiResponse<Any>> {
                override fun onResponse(
                    call: Call<ApiResponse<Any>>,
                    response: Response<ApiResponse<Any>>
                ) {

                }

                override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {

                }
            })
    }

    private fun updateProgressCalendar(cardId: String) {
        val localDate = LocalDate.now()
        val year = localDate.year
        val month = localDate.month.value
        val day = localDate.dayOfMonth

        val apiService = ApiHelper.getInstance().create(ApiService::class.java)
        apiService.updateProgressCalendar(MyApplication.getToken(), year, month, day, cardId)
            .enqueue(object : Callback<ApiResponse<Any>> {
                override fun onResponse(
                    call: Call<ApiResponse<Any>>,
                    response: Response<ApiResponse<Any>>
                ) {

                }

                override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {

                }
            })
    }
}