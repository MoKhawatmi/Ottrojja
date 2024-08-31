package com.ottrojja.classes

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import com.ottrojja.MainActivity
import com.ottrojja.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Timer
import java.util.TimerTask

interface AudioServiceInterface {
    fun isServiceRunning(): Boolean
    fun getPlayingState(link: String = "", playingChapter: Boolean = false): StateFlow<Boolean>
    fun playChapter(link: String)
    fun playZiker(link: String)
    fun pause()
    fun increaseSpeed()
    fun decreaseSpeed()
    fun getSliderPosition(): StateFlow<Float>
    fun getSliderMaxDuration(): StateFlow<Float>
    fun setSliderPosition(value: Float)
    fun getPaused(): StateFlow<Boolean>
    fun getDestroyed(): StateFlow<Boolean>
    fun getIsChapterPlaying(): StateFlow<Boolean>
    fun getIsZikrPlaying(): StateFlow<Boolean>
    fun getSelectedChapterId(): StateFlow<String>
    fun setSelectedChapterId(id: String)
    fun playNextChapter()
    fun playPreviousChapter()
    fun resumeClicked(): StateFlow<Int> //to serve as a notification to viewmodels that the resume was clicked, nothing more
}


class MediaPlayerService : Service(), AudioServiceInterface {
    var mediaPlayer: MutableState<MediaPlayer>? = null
    var length = 0;
    var currentlyPlaying = "";
    val playbackParams = PlaybackParams()
    private var _playbackSpeed by mutableStateOf(1.0f)
    lateinit var timer: Timer
    private val _isPlaying = MutableStateFlow<Boolean>(false)
    private val _isPaused = MutableStateFlow<Boolean>(false)
    private val _sliderPosition = MutableStateFlow<Float>(0f)
    private val _maxDuration = MutableStateFlow<Float>(0f)
    private val _selectedChapterId = MutableStateFlow<String>("")
    private val _playingChapter = MutableStateFlow<Boolean>(false)
    private val _playingZikr = MutableStateFlow<Boolean>(false)
    private val _destroyed = MutableStateFlow<Boolean>(false)
    private val resumeFlag = MutableStateFlow<Int>(0)


    inner class YourBinder : Binder() {
        fun getService(): AudioServiceInterface {
            return this@MediaPlayerService;
        }
    }

    override fun getSelectedChapterId(): StateFlow<String> {
        return _selectedChapterId;
    }

    override fun setSelectedChapterId(id: String) {
        _selectedChapterId.value = id;
        println("set chapter ${_selectedChapterId.value}")
    }

    override fun getSliderPosition(): StateFlow<Float> {
        return _sliderPosition;
    }

    override fun getPaused(): StateFlow<Boolean> {
        return _isPaused;
    }

    override fun getDestroyed(): StateFlow<Boolean> {
        return _destroyed;
    }

    override fun getIsChapterPlaying(): StateFlow<Boolean> {
        return _playingChapter;
    }

    override fun getIsZikrPlaying(): StateFlow<Boolean> {
        return _playingZikr;
    }


    override fun getSliderMaxDuration(): StateFlow<Float> {
        println("max duratuin $_maxDuration")
        return _maxDuration
    }

    override fun setSliderPosition(value: Float) {
        _sliderPosition.value = value;
        mediaPlayer!!.value.pause()
        mediaPlayer!!.value.seekTo(value.toInt());
        mediaPlayer!!.value.start();
    }

    override fun isServiceRunning(): Boolean {
        return false
    }

    override fun getPlayingState(link: String, playingChapter: Boolean): StateFlow<Boolean> {
        println("media player playing ${mediaPlayer!!.value.isPlaying}")
        if (mediaPlayer!!.value.isPlaying) {
            _isPlaying.value =
                (!currentlyPlaying.contains("azkar") && playingChapter) || (currentlyPlaying == link)
        } else {
            _isPlaying.value = false;
        }
        return _isPlaying
    }


    override fun playChapter(link: String) {
        if (_playingZikr.value) {
            _playingZikr.value = false;
            _playingChapter.value = true;
            playNewTrack(link)
        } else if (!_playingChapter.value && !_playingZikr.value) {
            _playingZikr.value = false;
            _playingChapter.value = true;
            playNewTrack(link)
        } else if (_playingChapter.value && _isPaused.value && length != 0 && currentlyPlaying == link) {
            resumeTrack();
        } else {
            _playingZikr.value = false;
            _playingChapter.value = true;
            playNewTrack(link)
        }
        _isPlaying.value = true;
        _isPaused.value = false;
    }

    override fun playZiker(link: String) {
        if (_playingChapter.value) {
            _playingZikr.value = true;
            _playingChapter.value = false;
            playNewTrack(link)
        } else if (!_playingChapter.value && !_playingZikr.value) {
            _playingZikr.value = true;
            _playingChapter.value = false;
            playNewTrack(link)
        } else if (_playingZikr.value && _isPaused.value && length != 0 && currentlyPlaying == link) {
            resumeTrack();
        } else {
            _playingZikr.value = true;
            _playingChapter.value = false;
            playNewTrack(link)
        }
        _selectedChapterId.value = "";
        _isPlaying.value = true;
        _isPaused.value = false;
    }

    private fun resumeTrack() {
        println("resuming")
        mediaPlayer!!.value.seekTo(length);
        mediaPlayer!!.value.start();
    }

    private fun playNewTrack(link: String) {
        println("playing new track")
        println(link)
        prepareForNewTrack();
        currentlyPlaying = link;
        playbackParams.speed = _playbackSpeed
        mediaPlayer!!.value.apply {
            reset()
            setDataSource(link)
            setPlaybackParams(playbackParams)
            prepareAsync()
            setOnPreparedListener {
                println("on prepared")
                _maxDuration.value = duration.toFloat()
                println("starting")
                it.start();
                timer = Timer();
                timer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        _sliderPosition.value = mediaPlayer!!.value.currentPosition.toFloat();
                    }
                }, 0, 400)
                println("on prepared done")
            }
            setOnCompletionListener {
                reset();
                length = 0;
                _isPlaying.value = false;
                if (::timer.isInitialized) {
                    timer.cancel();
                    timer.purge();
                }
                if (_playingChapter.value) {
                    if (_selectedChapterId.value != "114") {
                        playNextChapter();
                    } else {
                        prepareForNewTrack()
                    }
                }
            }
        }
    }

    private fun prepareForNewTrack() {
        if (::timer.isInitialized) {
            timer.cancel();
            timer.purge();
        }
        length = 0;
        _maxDuration.value = 0f;
        _sliderPosition.value = 0f;
        _playbackSpeed=1.0f
        mediaPlayer!!.value.reset();
    }

    override fun pause() {
        length = mediaPlayer!!.value.currentPosition;
        _isPlaying.value = false;
        _isPaused.value = true;
        mediaPlayer!!.value.pause()
    }

    override fun increaseSpeed() {
        if (_playbackSpeed < 2.0f) {
            this._playbackSpeed += 0.25f;
            updatePlaybackSpeed()
        }
    }

    override fun decreaseSpeed() {
        if (_playbackSpeed > 0.25f) {
            this._playbackSpeed -= 0.25f;
            updatePlaybackSpeed()
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return YourBinder();
    }


    fun updatePlaybackSpeed() {
        val playbackParams = PlaybackParams()
        playbackParams.speed = this._playbackSpeed
        mediaPlayer!!.value.playbackParams = playbackParams;
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent!!.action) {

            Actions.START.toString() -> {
                _destroyed.value = false;
                startService()
            }

            Actions.STOP.toString() -> {
                println("stopping media inside app")
                if (::timer.isInitialized) {
                    timer.cancel();
                    timer.purge();
                }
                mediaPlayer?.value?.reset();
                _isPlaying.value = false;
                _isPaused.value = false;
                _playingChapter.value = false;
                _playingZikr.value = false;
                _selectedChapterId.value = "";
            }

            Actions.TERMINATE.toString() -> {
                println("stopping self")
                if (::timer.isInitialized) {
                    timer.cancel();
                    timer.purge();
                }
                if (mediaPlayer != null) {
                    mediaPlayer!!.value.reset();
                    mediaPlayer!!.value.release();
                }
                mediaPlayer = null;
                _isPlaying.value = false;
                _isPaused.value = false;
                _playingChapter.value = false;
                _playingZikr.value = false;
                _selectedChapterId.value = "";
                _destroyed.value = true;
                stopForeground(true)
                stopSelf()
            }

            Actions.NOTI_PLAY.toString() -> {
                if (!_isPlaying.value) {
                    resumeTrack()
                    resumeFlag.value++;
                    // getPlayingState(currentlyPlaying, _playingChapter.value)
                }
            }

            Actions.NOTI_PAUSE.toString() -> {
                if (mediaPlayer!!.value.isPlaying) {
                    pause()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun startService() {
        println("starting service")
        if (mediaPlayer != null) {
            mediaPlayer!!.value.reset();
            mediaPlayer!!.value.release();
        }
        mediaPlayer = mutableStateOf(MediaPlayer())

        val notificationLayout = buildNotificationLayout()

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }


        val notification = NotificationCompat.Builder(this, "PLAYER_CHANNEL")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("تطبيق اترجة")
            .setContentText("")
            // .setContentIntent(pendingIntent)
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setSilent(true)
            .build()

        startForeground(1, notification);
    }

    override fun playNextChapter() {
        if (_selectedChapterId.value != "114") {
            _selectedChapterId.value = "${_selectedChapterId.value.toInt() + 1}"
            playChapter("https://ottrojja.fra1.cdn.digitaloceanspaces.com/chapters/${_selectedChapterId.value}.mp3")
        }
    }

    override fun playPreviousChapter() {
        if (_selectedChapterId.value != "1") {
            _selectedChapterId.value = "${_selectedChapterId.value.toInt() - 1}"
            playChapter("https://ottrojja.fra1.cdn.digitaloceanspaces.com/chapters/${_selectedChapterId.value}.mp3")
        }
    }


    enum class Actions {
        START, TERMINATE, STOP, NOTI_PLAY, NOTI_PAUSE
    }

    private fun getPendingIntentForAction(action: String): PendingIntent {
        val intent = Intent(this, MediaPlayerService::class.java)
        intent.action = action
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }


    private fun buildNotificationLayout(): RemoteViews {
        val remoteViews = RemoteViews(packageName, R.layout.notification_layout)

        // Set up onClick listeners for media control buttons
        remoteViews.setOnClickPendingIntent(
            R.id.play_button,
            getPendingIntentForAction("NOTI_PLAY")
        )
        remoteViews.setOnClickPendingIntent(
            R.id.pause_button,
            getPendingIntentForAction("NOTI_PAUSE")
        )
        remoteViews.setOnClickPendingIntent(
            R.id.close_button,
            getPendingIntentForAction("TERMINATE")
        )

        return remoteViews
    }

    override fun resumeClicked(): StateFlow<Int> {
        return resumeFlag;
    }

}