package com.example.practicefortoeictestrebuild.ui.realtest

import android.animation.ValueAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.practicefortoeictestrebuild.R
import com.example.practicefortoeictestrebuild.base.BaseFragment
import com.example.practicefortoeictestrebuild.databinding.DialogInternetErrorBinding
import com.example.practicefortoeictestrebuild.databinding.FragmentRealTestBinding
import com.example.practicefortoeictestrebuild.model.QuestionCard
import com.example.practicefortoeictestrebuild.ui.test.TestResultViewModel
import com.example.practicefortoeictestrebuild.ui.test.TestViewModel
import com.example.practicefortoeictestrebuild.utils.startLoading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class RealTestFragment : BaseFragment<FragmentRealTestBinding>(FragmentRealTestBinding::inflate) {

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

    private val realTestResultViewModel by lazy {
        activity?.let {
            ViewModelProvider(it)[RealTestViewModel::class.java]
        }
    }

    private val internetDialog by lazy { Dialog(requireContext()) }

    private val loadingDialog by lazy { context?.let { Dialog(it) } }

    private val dialogBinding by lazy { DialogInternetErrorBinding.inflate(layoutInflater) }

    private var index = 0

    private var mediaPlayer: MediaPlayer? = null

    private var targetHeight = 0

    override fun initData() {
        initDialog()
        testViewModel?.getDataWithOutProgress()
        testResultViewModel?.startTime = LocalDateTime.now()
    }

    override fun handleEvent() {
        startCountdown(2 * 60 * 60)
        clearCard()
        testResultViewModel?.resetListQuestions()

        binding.questionCard.result.btnContinue.setOnClickListener {
            nextQuestion()
        }

        binding.questionCard.btnNextQuestion.setOnClickListener {
            nextQuestion()
        }

        binding.questionCard.btnSubmit.setOnClickListener {
            testResultViewModel?.endTime = LocalDateTime.now()
            findNavController().navigate(R.id.action_realTestFragment_to_realTestResultFragment)
        }

        binding.questionCard.btnPlay.setOnClickListener {
            binding.questionCard.imgPlay.setImageResource(R.drawable.ic_stop_button)
            binding.questionCard.btnPlay.isEnabled = false

            mediaPlayer?.apply {
                start()
            }?.setOnCompletionListener {
                binding.questionCard.imgPlay.setImageResource(R.drawable.ic_play_button)
                binding.questionCard.btnPlay.isEnabled = true
            }

            val handler = Handler(Looper.getMainLooper())

            handler.post(object : Runnable {
                override fun run() {
                    val curDuration = mediaPlayer?.currentPosition?.toLong()
                    if (curDuration != null) {
                        val time = String.format(
                            "%02d:%02d ",
                            TimeUnit.MILLISECONDS.toMinutes(curDuration),
                            TimeUnit.MILLISECONDS.toSeconds(curDuration) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    curDuration
                                )
                            )
                        )
                        binding.questionCard.txtStartTime.text = time
                        binding.questionCard.playbarProgress.progress =
                            TimeUnit.MILLISECONDS.toSeconds(curDuration).toInt()
                    }

                    handler.postDelayed(this, 1000)
                }
            })
        }

        binding.questionCard.answer.aAnswer.setOnClickListener {
            chooseAnswer("A")
        }

        binding.questionCard.answer.bAnswer.setOnClickListener {
            chooseAnswer("B")
        }

        binding.questionCard.answer.cAnswer.setOnClickListener {
            chooseAnswer("C")
        }

        binding.questionCard.answer.dAnswer.setOnClickListener {
            chooseAnswer("D")
        }

        dialogBinding.btnRetry.setOnClickListener {
            internetDialog.dismiss()
            if (index >= testResultViewModel?.questions?.value!!.size)
                testResultViewModel?.getQuestion(index, internetDialog)
            else testResultViewModel?.resetListQuestions()
        }
    }

    override fun bindData() {
        testViewModel?.isLoading?.observe(viewLifecycleOwner) {
            if (it) {
                loadingDialog?.startLoading(false)
            } else {
                loadingDialog?.dismiss()
            }
        }

        testResultViewModel?.questions?.observe(viewLifecycleOwner) {
            if (index < testResultViewModel?.questions?.value!!.size) {
                fillCardData(it[index])
                fillCardSize()
                binding.txtProgress.text = "${index + 1}/${testResultViewModel?.getListSize()}"
            }
        }

        testViewModel?.topicName?.observe(viewLifecycleOwner) {
            binding.toolbar.title = it
        }

        testViewModel?.allQuestion?.observe(viewLifecycleOwner) {
            if (it.size > 0) {
                testResultViewModel?.setListIds(it)
                if (index >= testResultViewModel?.questions?.value!!.size)
                    testResultViewModel?.getQuestion(index, internetDialog)
                else testResultViewModel?.resetListQuestions()
            }
        }
    }

    private fun initDialog() {
        internetDialog.setContentView(dialogBinding.root)
        internetDialog.setCancelable(false)
        internetDialog.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            attributes.apply {
                gravity = Gravity.CENTER
            }
            setDimAmount(0.5F)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun nextQuestion() {
        destroyCurrentCard()
        index++
        if (index < testResultViewModel?.getListSize()!!) {
            if (index < testResultViewModel?.questions?.value!!.size) {
                testResultViewModel?.resetListQuestions()
            } else {
                testResultViewModel?.getQuestion(index, internetDialog)
            }
            clearCard()
        } else {
            testResultViewModel?.endTime = LocalDateTime.now()
            findNavController().navigate(R.id.action_realTestFragment_to_realTestResultFragment)
        }
        index = index.coerceAtMost(testResultViewModel?.getListSize()!! - 1)
        testResultViewModel?.resetListQuestions()
    }

    private fun startCountdown(maxTime: Int) {
        var remainingSeconds = maxTime
        CoroutineScope(Dispatchers.Main).launch {
            for (i in remainingSeconds downTo 0) {
                val hours = i / 3600
                val minutes = (i % 3600) / 60
                val seconds = i % 60

                binding.txtRemainingTime.text =
                    String.format("%02d:%02d:%02d", hours, minutes, seconds)

                delay(1_000)
            }
            testResultViewModel?.endTime = LocalDateTime.now()
            findNavController().navigate(R.id.action_realTestFragment_to_realTestResultFragment)
        }

    }

    private fun clearCard() {
        val view = binding.questionCard.lnQuestionCard

        val initialHeight = view.height
        targetHeight = initialHeight

        val animator = ValueAnimator.ofInt(initialHeight, 0)

        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.height = value
            view.layoutParams = layoutParams
            view.requestLayout()
        }
        animator.duration = 200
        animator.start()
    }

    private fun fillCardSize() {
        Handler(Looper.getMainLooper()).postDelayed({
            val view = binding.questionCard.lnQuestionCard

            if (targetHeight < 1) {
                view.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            } else {
                val animator = ValueAnimator.ofInt(0, targetHeight)
                animator.addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Int
                    val layoutParams = view.layoutParams
                    layoutParams.height = value
                    view.layoutParams = layoutParams
                    view.requestLayout()
                }
                animator.duration = 200
                animator.start()
            }
        }, 210)
    }

    private fun fillCardData(card: QuestionCard) {
        binding.questionCard.btnPlay.isEnabled = false
        enableChoiceButton(true)
        fillBlankAnswer()

        val image = card.children[0].question.image
        val sound = card.children[0].question.sound
        val hint = card.children[0].question.hint
        val content = card.children[0].question.content
        val userChoice = card.userChoice
        var correct = card.children[0].answer.answer[0].toString()
        if (correct != "A" && correct != "B" && correct != "C" && correct != "D")
            correct = card.children[0].answer.answer[1].toString()

        if (image.isNullOrEmpty())
            setViewHeight(binding.questionCard.imgImage, 0)
        else {
            fillImage(image)
            setViewHeight(binding.questionCard.imgImage, 500)
        }

        if (sound.isNullOrEmpty())
            setViewHeight(binding.questionCard.lnPlayBar, 0)
        else {
            fillSound(sound)
            setViewHeight(binding.questionCard.lnPlayBar, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        if (realTestResultViewModel?.status == "do-test") {
            setViewHeight(binding.questionCard.result.resultArea, 0)
            setViewHeight(binding.questionCard.layoutButton, LinearLayout.LayoutParams.WRAP_CONTENT)
        } else {
            enableChoiceButton(false)
            setViewHeight(binding.questionCard.layoutButton, 0)
            coloredAnswer(userChoice, correct)
            showResult()
        }

        binding.questionCard.txtContent.text = content
        binding.questionCard.result.txtExplanation.text = hint

        val choice = card.children[0].answer.choices
        val answer = binding.questionCard.answer
        answer.txtAAnswer.text = choice[0]
        if (choice.size > 1)
            answer.txtBAnswer.text = choice[1]
        if (choice.size > 2)
            answer.txtCAnswer.text = choice[2]
        if (choice.size > 3)
            answer.txtDAnswer.text = choice[3]
    }

    private fun fillImage(link: String) {
        val image = binding.questionCard.imgImage
        Glide.with(image.context).load(link).into(image)
    }

    private fun fillSound(link: String) {
        binding.questionCard.txtStartTime.text = "00:00"
        binding.questionCard.txtEndTime.text = "00:00"

        val media = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build()
            )
            setDataSource(link)
            binding.questionCard.lnLoading.alpha = 1.0F
            CoroutineScope(Dispatchers.Default).launch {
                prepare()
            }
        }.setOnPreparedListener {
            binding.questionCard.btnPlay.isEnabled = true
            mediaPlayer = it

            val duration: Long = it.duration.toLong()
            binding.questionCard.playbarProgress.max =
                TimeUnit.MILLISECONDS.toSeconds(duration).toInt()

            binding.questionCard.txtEndTime.text = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(duration)
                )
            )
            binding.questionCard.lnLoading.alpha = 0.0F
        }
    }

    private fun fillBlankAnswer() {
        binding.questionCard.answer.aAnswer.setBackgroundResource(R.drawable.ic_choice_unchecked)
        binding.questionCard.answer.bAnswer.setBackgroundResource(R.drawable.ic_choice_unchecked)
        binding.questionCard.answer.cAnswer.setBackgroundResource(R.drawable.ic_choice_unchecked)
        binding.questionCard.answer.dAnswer.setBackgroundResource(R.drawable.ic_choice_unchecked)
    }

    private fun fillResult(flag: Boolean, correct: String) {
        val result = binding.questionCard.result

        result.txtAnswer.text = "Answer: $correct"
        if (flag) {
            result.imgImage.setImageResource(R.drawable.img_hehe)
            result.txtResult.text = "Correct"
            result.txtResult.setTextColor(requireContext().getColor(R.color.green_200))
        } else {
            result.imgImage.setImageResource(R.drawable.img_huhu)
            result.txtResult.text = "Incorrect"
            result.txtResult.setTextColor(requireContext().getColor(carbon.R.color.carbon_red_400))
        }
        if (realTestResultViewModel?.status != "do-test")
            showResult()
    }

    private fun showResult() {
        setViewHeight(
            binding.questionCard.result.resultArea,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun destroyCurrentCard() {
        mediaPlayer?.reset()
        mediaPlayer?.stop()
    }

    private fun setViewHeight(view: View, height: Int) {
        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, height
        )
    }

    private fun enableChoiceButton(flag: Boolean) {
        binding.questionCard.answer.aAnswer.isEnabled = flag
        binding.questionCard.answer.bAnswer.isEnabled = flag
        binding.questionCard.answer.cAnswer.isEnabled = flag
        binding.questionCard.answer.dAnswer.isEnabled = flag
    }

    private fun chooseAnswer(choice: String) {
        val card = testResultViewModel?.questions?.value!![index]
        var correct = card.children[0].answer.answer[0].toString()
        if (correct != "A" && correct != "B" && correct != "C" && correct != "D")
            correct = card.children[0].answer.answer[1].toString()

        when (choice) {
            "A" -> card.userChoice = 1
            "B" -> card.userChoice = 2
            "C" -> card.userChoice = 3
            "D" -> card.userChoice = 4
        }

        coloredAnswer(card.userChoice, correct)

        if (choice != correct) {
            testViewModel?.removeCorrectCard(card.id)
            testResultViewModel?.updateCard(card.id, "false")
        } else {
            testViewModel?.removeWrongCard(card.id)
            testResultViewModel?.updateCard(card.id, "true")
        }
    }

    private fun coloredAnswer(userChoice: Int, correct: String) {
        var choice = ""
        if (realTestResultViewModel?.status == "do-test") {
            fillBlankAnswer()
            when (userChoice) {
                1 -> {
                    choice = "A"
                    binding.questionCard.answer.aAnswer.setBackgroundResource(R.drawable.ic_choice_correct)
                }
                2 -> {
                    choice = "B"
                    binding.questionCard.answer.bAnswer.setBackgroundResource(R.drawable.ic_choice_correct)
                }
                3 -> {
                    choice = "C"
                    binding.questionCard.answer.cAnswer.setBackgroundResource(R.drawable.ic_choice_correct)
                }
                4 -> {
                    choice = "D"
                    binding.questionCard.answer.dAnswer.setBackgroundResource(R.drawable.ic_choice_correct)
                }
            }
        } else {
            when (userChoice) {
                1 -> {
                    choice = "A"
                    binding.questionCard.answer.aAnswer.setBackgroundResource(R.drawable.ic_choice_wrong)
                }
                2 -> {
                    choice = "B"
                    binding.questionCard.answer.bAnswer.setBackgroundResource(R.drawable.ic_choice_wrong)
                }
                3 -> {
                    choice = "C"
                    binding.questionCard.answer.cAnswer.setBackgroundResource(R.drawable.ic_choice_wrong)
                }
                4 -> {
                    choice = "D"
                    binding.questionCard.answer.dAnswer.setBackgroundResource(R.drawable.ic_choice_wrong)
                }
            }

            when (correct) {
                "A" -> binding.questionCard.answer.aAnswer.setBackgroundResource(R.drawable.ic_choice_correct)
                "B" -> binding.questionCard.answer.bAnswer.setBackgroundResource(R.drawable.ic_choice_correct)
                "C" -> binding.questionCard.answer.cAnswer.setBackgroundResource(R.drawable.ic_choice_correct)
                "D" -> binding.questionCard.answer.dAnswer.setBackgroundResource(R.drawable.ic_choice_correct)
            }
        }
        fillResult(correct == choice, correct)
    }
}