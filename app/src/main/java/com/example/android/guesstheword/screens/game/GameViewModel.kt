package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

class GameViewModel: ViewModel() {

    // These are the three different types of buzzing in the game. Buzz pattern is the number of
    // milliseconds each interval of buzzing and non-buzzing takes.
    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    //Time variables
    companion object {
        //When the time is over
        private const val DONE = 0L
        //Number of milliseconds in a second
        private const val ONE_SECOND = 1000L
        //Total time of the game
        private const val COUNTDOWN_TIME = 30000L
        // This is the time when the phone will start buzzing each second
        private const val COUNTDOWN_PANIC_SECONDS = 10L
    }
    private val timer: CountDownTimer

    //The current time
    private var _time = MutableLiveData<Long>()
    val time: LiveData<Long>
        get() = _time

    // The String version of the current time
    val currentTime = Transformations.map(_time) {time ->
        DateUtils.formatElapsedTime(time)
    }

    // The current word
    private var _word = MutableLiveData<String>()
    val word: LiveData<String>
        get() = _word

    // The current score
    private var _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score


    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    //Finish game LiveData
    private var _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish: LiveData<Boolean>
        get() = _eventGameFinish


    // Event that triggers the phone to buzz using different patterns, determined by BuzzType
    private val _eventBuzz = MutableLiveData<BuzzType>()
    val eventBuzz: LiveData<BuzzType>
        get() = _eventBuzz


    init {
        resetList()
        nextWord()
        _score.value = 0

        //Crear timer
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND){

            override fun onTick(millisUntilFinished: Long) {
                _time.value = millisUntilFinished/ ONE_SECOND
                if (millisUntilFinished / ONE_SECOND <= COUNTDOWN_PANIC_SECONDS) {
                    _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
                }
            }

            override fun onFinish() {
                _time.value = DONE
                _eventGameFinish.value = true
                _eventBuzz.value = BuzzType.GAME_OVER
            }
        }
        timer.start()
    }


    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = (score.value)?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _score.value = (score.value)?.plus(1)
        _eventBuzz.value = BuzzType.CORRECT
        nextWord()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        //Select and remove a word from the list
        if (wordList.isEmpty()) {
            resetList()
        }
        _word.value = wordList.removeAt(0)
    }

    /**   Methods for completed events
     *Indicar al VIewModel que el evento ya ocurrió una vez
     * de lo contrario cada vez que exista un cambio de configuración, como la rotación de pantalla,
     * el LiveData disparará el evento
     */

    fun onGameFinishComplete(){
        _eventGameFinish.value = false
    }
    fun onBuzzComplete() {
        _eventBuzz.value = BuzzType.NO_BUZZ
    }


    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
                "queen",
                "hospital",
                "basketball",
                "cat",
                "change",
                "snail",
                "soup",
                "calendar",
                "sad",
                "desk",
                "guitar",
                "home",
                "railway",
                "zebra",
                "jelly",
                "car",
                "crow",
                "trade",
                "bag",
                "roll",
                "bubble"
        )
        wordList.shuffle()
    }




    //Este método se ejecuta justo antes que la Activity/Fragment
    //asociada a este ViewModel, se destruya completamente
    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

}