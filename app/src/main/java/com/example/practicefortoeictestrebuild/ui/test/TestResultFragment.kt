package com.example.practicefortoeictestrebuild.ui.test

import android.app.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.practicefortoeictestrebuild.R
import com.example.practicefortoeictestrebuild.base.BaseFragment
import com.example.practicefortoeictestrebuild.databinding.FragmentTestResultBinding
import java.time.Duration
import java.time.LocalDateTime

class TestResultFragment :
    BaseFragment<FragmentTestResultBinding>(FragmentTestResultBinding::inflate) {

    private val testViewModel by lazy {
        activity?.let {
            ViewModelProvider(it)[TestViewModel::class.java]
        }
    }

    private val testResultViewModel by lazy {
        activity?.let {
            ViewModelProvider(it)[TestResultViewModel::class.java]
        }
    }

    private val loadingDialog by lazy { context?.let { Dialog(it) } }

    private var countAllQuestion = 0

    private var countCorrectQuestion = 0

    private var countWrongQuestion = 0

    override fun initData() {
        testResultViewModel?.endTime = LocalDateTime.now()
    }

    override fun handleEvent() {
        binding.testDetailCard.btnReview.setOnClickListener {
            testViewModel?.resetListQuestions()
            findNavController().navigate(R.id.action_testResultFragment_to_testFragment)
        }

        binding.testDetailCard.btnReplay.setOnClickListener {
            testViewModel?.clearQuestions()
            findNavController().navigate(R.id.action_testResultFragment_to_testFragment)
        }

        binding.testResultBar.btnNextPractice.setOnClickListener {
            testViewModel?.indexPlusOne()
            findNavController().popBackStack()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun bindData() {
        val startTime = testResultViewModel?.startTime
        val endTime = testResultViewModel?.endTime
        val duration = Duration.between(startTime, endTime)
        binding.testDetailCard.txtCountTotalTime.text = "${formattedTime(duration)}"

        testViewModel?.questions?.observe(viewLifecycleOwner) {
            countAllQuestion = it.size
            binding.testDetailCard.txtCountTotalQuestion.text = "${it.size}"
            binding.testResultBar.txtCountAll.text = "${it.size}"
            bindProgress()
        }

        testViewModel?.correctQuestion?.observe(viewLifecycleOwner) {
            countCorrectQuestion = it.size
            binding.testDetailCard.txtCountCorrect.text = "${it.size}"
            binding.testResultBar.txtCountCorrect.text = "${it.size}"
            bindProgress()
        }

        testViewModel?.incorrectQuestion?.observe(viewLifecycleOwner) {
            countWrongQuestion = it.size
            binding.testDetailCard.txtCountIncorrect.text = "${it.size}"
            binding.testResultBar.txtCountIncorrect.text = "${it.size}"
            bindProgress()
        }
    }

    override fun onResume() {
        super.onResume()
        bindData()
    }

    private fun formattedTime(duration: Duration): String {
        val hours = duration.toHours().toString().padStart(2, '0')
        val minutes = (duration.toMinutes() % 60).toString().padStart(2, '0')
        val seconds = (duration.seconds % 60).toString().padStart(2, '0')

        if (hours == "00")
            return "$minutes:$seconds"
        return "$hours:$minutes:$seconds"
    }

    private fun bindProgress() {
        binding.testResultBar.txtCountNew.text =
            "${countAllQuestion - countCorrectQuestion - countWrongQuestion}"

        if (countAllQuestion > 0) {
            val progress: Int = (100.0 * countCorrectQuestion / countAllQuestion).toInt()
            binding.testDetailCard.txtProgress.text = "$progress%"
        }
    }
}