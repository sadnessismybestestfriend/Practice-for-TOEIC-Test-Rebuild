package com.example.practicefortoeictestrebuild.ui.vocabulary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.practicefortoeictestrebuild.MyApplication
import com.example.practicefortoeictestrebuild.api.ApiHelper
import com.example.practicefortoeictestrebuild.api.ApiResponse
import com.example.practicefortoeictestrebuild.api.ApiService
import com.example.practicefortoeictestrebuild.base.BaseViewModel
import com.example.practicefortoeictestrebuild.base.DataResult
import com.example.practicefortoeictestrebuild.model.DataOverview
import com.example.practicefortoeictestrebuild.model.FlashCard
import com.example.practicefortoeictestrebuild.model.TopicVocabulary
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class VocabularyViewModel : BaseViewModel() {

    private val _topicId = MutableLiveData<String>().apply {
        value = ""
    }

    private val _topicName = MutableLiveData<String>().apply {
        value = ""
    }

    private val _allCard = MutableLiveData<MutableList<FlashCard>>().apply {
        value = mutableListOf()
    }

    private val _newCard = MutableLiveData<MutableList<FlashCard>>().apply {
        value = mutableListOf()
    }

    private val _memorizedCard = MutableLiveData<MutableList<FlashCard>>().apply {
        value = mutableListOf()
    }

    private val _unmemorizedCard = MutableLiveData<MutableList<FlashCard>>().apply {
        value = mutableListOf()
    }

    private val _listCard = MutableLiveData<MutableList<FlashCard>>().apply {
        value = mutableListOf()
    }

    val allCard: LiveData<MutableList<FlashCard>> get() = _allCard

    val newCard: LiveData<MutableList<FlashCard>> get() = _newCard

    val memorizedCard: LiveData<MutableList<FlashCard>> get() = _memorizedCard

    val unmemorizedCard: LiveData<MutableList<FlashCard>> get() = _unmemorizedCard

    val listCard: LiveData<MutableList<FlashCard>> get() = _listCard

    val topicId: LiveData<String> get() = _topicId

    val topicName: LiveData<String> get() = _topicName

    fun setTopicId(id: String?) {
        _topicId.value = id
    }

    private suspend fun getFlashCard(): DataResult<MutableList<FlashCard>> {
        val apiService = ApiHelper.getInstance().create(ApiService::class.java)

        val allCard = mutableListOf<FlashCard>()
        val newCard = mutableListOf<FlashCard>()
        val memorizedCard = mutableListOf<FlashCard>()
        val unmemorizedCard = mutableListOf<FlashCard>()

        val x = suspendCoroutine { continuation ->
            apiService.getFlashCard(
                MyApplication.getToken(),
                topicId.value
            ).enqueue(object : Callback<ApiResponse<TopicVocabulary>> {
                override fun onResponse(
                    call: Call<ApiResponse<TopicVocabulary>>,
                    response: Response<ApiResponse<TopicVocabulary>>
                ) {
                    if (response.isSuccessful && response.body()?.data != null) {
                        _topicName.value = response.body()?.data!!.name
                        val list = response.body()?.data!!.cards
                        list.forEach {
                            allCard.add(it)
                        }
                        continuation.resume(1)
                    } else continuation.resume(0)
                }

                override fun onFailure(
                    call: Call<ApiResponse<TopicVocabulary>>,
                    t: Throwable
                ) {
                    continuation.resume(0)
                }
            })
        }

        val y = suspendCoroutine { continuation ->
            apiService.getProgressCard(
                MyApplication.getToken(),
                topicId.value
            ).enqueue(object : Callback<ApiResponse<MutableList<DataOverview>>> {
                override fun onResponse(
                    call: Call<ApiResponse<MutableList<DataOverview>>>,
                    response: Response<ApiResponse<MutableList<DataOverview>>>
                ) {
                    if (response.isSuccessful && response.body()?.data != null) {
                        val progress = response.body()?.data
                        allCard.forEach { card ->
                            var flag = false
                            progress?.forEach { data ->
                                if (card.id == data.cardId && data.status == 1) {
                                    unmemorizedCard.add(card)
                                    flag = true
                                } else if (card.id == data.cardId && data.status == 2) {
                                    memorizedCard.add(card)
                                    flag = true
                                }
                            }
                            if (!flag) newCard.add(card)
                        }
                        continuation.resume(1)
                    } else continuation.resume(0)
                }

                override fun onFailure(
                    call: Call<ApiResponse<MutableList<DataOverview>>>,
                    t: Throwable
                ) {
                    continuation.resume(0)
                }
            })
        }

        _allCard.value = allCard
        _newCard.value = newCard
        _memorizedCard.value = memorizedCard
        _unmemorizedCard.value = unmemorizedCard
        return DataResult.Success(allCard)
    }

    fun getData() {
        executeTask(
            request = {
                getFlashCard()
            },
            onSuccess = {
                _listCard.value = it
            },
            onError = {
            }
        )
    }

    fun updateCard(cardId: String, answer: String) {
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

    fun setListCard(selectedCategory: String?) {
        when (selectedCategory) {
            "Default" -> _listCard.value = _allCard.value
            "New" -> _listCard.value = _newCard.value
            "Memorized" -> _listCard.value = _memorizedCard.value
            "Unmemorized" -> _listCard.value = _unmemorizedCard.value
        }
    }

    fun memorizeCard(card: FlashCard) {
        _unmemorizedCard.value?.remove(card)
        _newCard.value?.remove(card)
        if (_memorizedCard.value?.contains(card) == false) {
            _memorizedCard.value?.add(card)
        }
        resetList()
    }

    fun unMemorizeCard(card: FlashCard) {
        _memorizedCard.value?.remove(card)
        _newCard.value?.remove(card)
        if (_unmemorizedCard.value?.contains(card) == false) {
            _unmemorizedCard.value?.add(card)
        }
        resetList()
    }

    private fun resetList() {
        _memorizedCard.value = _memorizedCard.value
        _unmemorizedCard.value = _unmemorizedCard.value
        _newCard.value = _newCard.value
    }
}