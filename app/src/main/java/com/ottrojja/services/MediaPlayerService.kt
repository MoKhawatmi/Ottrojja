package com.ottrojja.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface AudioServiceInterface {
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
    fun getPlaybackSpeed(): StateFlow<Float>
    fun setCurrentPlayingTitle(value: String)
}


class MediaPlayerService : Service(), AudioServiceInterface {
    var length: Long = 0;
    var currentlyPlaying = "";
    private var _playbackSpeed = MutableStateFlow<Float>(1.0f)

    private val _isPlaying = MutableStateFlow<Boolean>(false)
    private val _isPaused = MutableStateFlow<Boolean>(false)
    private val _sliderPosition = MutableStateFlow<Float>(0f)
    private val _maxDuration = MutableStateFlow<Float>(0f)
    private val _selectedChapterId = MutableStateFlow<String>("")
    private val _playingChapter = MutableStateFlow<Boolean>(false)
    private val _playingZikr = MutableStateFlow<Boolean>(false)
    private val _destroyed = MutableStateFlow<Boolean>(false)
    private val resumeFlag = MutableStateFlow<Int>(0)
    private var _currentPlayingTitle: String = "";
    lateinit var exoPlayer: ExoPlayer;

    private lateinit var notificationManager: NotificationManager

    private val handler = Handler(Looper.getMainLooper())


    inner class YourBinder : Binder() {
        fun getService(): AudioServiceInterface {
            return this@MediaPlayerService;
        }
    }

    fun initializePlayer() {
        val context = this
        exoPlayer = ExoPlayer.Builder(this).build();

        exoPlayer.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    println("Listener is Playing $isPlaying")
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)

                    if (playbackState == Player.STATE_READY) {
                        println("ExoPlayer State Ready")

                        _maxDuration.value = exoPlayer.duration.toFloat()
                        println("track duration ${_maxDuration.value}")
                        val updateProgressAction = object : Runnable {
                            override fun run() {
                                val currPosition = exoPlayer.currentPosition.toFloat()
                                _sliderPosition.value = currPosition;
                                handler.postDelayed(this, 400)
                            }
                        }
                        handler.postDelayed(updateProgressAction, 400) // Repeat every 400ms
                    }

                    if (playbackState == Player.STATE_ENDED) {
                        println("ExoPlayer State Ended")
                        length = 0;
                        resetPlayer()
                        _isPlaying.value = false;
                        handler.removeCallbacksAndMessages(null)
                        if (_playingChapter.value) {
                            if (_selectedChapterId.value != "114") {
                                playNextChapter();
                            } else {
                                prepareForNewTrack()
                            }
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    println(error.printStackTrace())
                    Toast.makeText(context, "حصل خطأ، يرجى المحاولة مجددا", Toast.LENGTH_LONG)
                        .show()
                }
            }
        )
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

    override fun getPlaybackSpeed(): StateFlow<Float> {
        return _playbackSpeed
    }

    override fun setSliderPosition(value: Float) {
        _sliderPosition.value = value;
        exoPlayer.seekTo(value.toLong())
    }

    override fun setCurrentPlayingTitle(value: String) {
        _currentPlayingTitle = value;
        updateNotification()
    }

    override fun getPlayingState(link: String, playingChapter: Boolean): StateFlow<Boolean> {
        //println("media player playing ${mediaPlayer?.value?.isPlaying}")
        println("exo player playing ${exoPlayer.isPlaying}")
        if (/*mediaPlayer?.value?.isPlaying*/ exoPlayer.isPlaying == true) {
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
        } else if (_playingChapter.value && _isPaused.value && length != 0L && currentlyPlaying == link) {
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
        } else if (_playingZikr.value && _isPaused.value && length != 0L && currentlyPlaying == link) {
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

    private fun resetAll() {
        _isPlaying.value = false;
        _isPaused.value = false;
        _playingZikr.value = false;
        _playingChapter.value = false;
        _sliderPosition.value = 0F;
        _selectedChapterId.value = "";
        resetPlayer()
    }

    private fun resumeTrack() {
        println("resuming")
        exoPlayer.play()
    }

    private fun playNewTrack(link: String) {
        println("playing new track")
        println(link)
        prepareForNewTrack();
        currentlyPlaying = link;
        updatePlaybackSpeed()
        var mediaSrc: Uri;

        //check offline playing possiblity
        val path = link.split("/").last()
        val localFile = File(this.getExternalFilesDir(null), path)
        if (!localFile.exists() && !Helpers.checkNetworkConnectivity(this)) {
            Toast.makeText(this, "لا يوجد اتصال بالانترنت", Toast.LENGTH_LONG).show()
            resetAll();
            return;
        }
        if (localFile.exists()) {
            mediaSrc = Uri.fromFile(localFile)
            println("file found ${localFile.path}")
        } else {
            mediaSrc = Uri.parse(link)
        }

        exoPlayer.apply {
            val mediaItem = MediaItem.fromUri(mediaSrc)
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    private fun prepareForNewTrack() {
        handler.removeCallbacksAndMessages(null)
        length = 0;
        _maxDuration.value = 0f;
        _sliderPosition.value = 0f;
        _playbackSpeed.value = 1.0f
        resetPlayer()
    }

    override fun pause() {
        length = exoPlayer.currentPosition
        _isPlaying.value = false;
        _isPaused.value = true;
        exoPlayer.pause()
    }

    override fun increaseSpeed() {
        if (_playbackSpeed.value < 2.0f) {
            _playbackSpeed.value += 0.25f;
            updatePlaybackSpeed()
        }
    }

    override fun decreaseSpeed() {
        if (_playbackSpeed.value > 0.25f) {
            _playbackSpeed.value -= 0.25f;
            updatePlaybackSpeed()
        }
    }

    fun updatePlaybackSpeed() {
        exoPlayer.playbackParameters = PlaybackParameters(_playbackSpeed.value)
    }


    override fun onBind(p0: Intent?): IBinder? {
        return YourBinder();
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            try {
                when (intent.action) {
                    Actions.START.toString() -> {
                        _destroyed.value = false;
                        startService()
                    }

                    Actions.STOP.toString() -> {
                        println("stopping media inside app")
                        handler.removeCallbacksAndMessages(null)
                        resetPlayer()
                        _isPlaying.value = false;
                        _isPaused.value = false;
                        _playingChapter.value = false;
                        _playingZikr.value = false;
                        _selectedChapterId.value = "";
                    }

                    Actions.TERMINATE.toString() -> {
                        println("stopping self")
                        handler.removeCallbacksAndMessages(null)
                        if (::exoPlayer.isInitialized) {
                            releasePlayer()
                        }
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
                        }
                    }

                    Actions.NOTI_PAUSE.toString() -> {
                        if (exoPlayer.isPlaying == true) {
                            pause()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return android.app.Service.START_REDELIVER_INTENT
    }

    fun startService() {
        println("starting service")

        if (!::notificationManager.isInitialized) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        if (::exoPlayer.isInitialized) {
            releasePlayer()
        }
        initializePlayer()

        val notificationLayout = buildNotificationLayout(_currentPlayingTitle)

        val notification = NotificationCompat.Builder(this, "PLAYER_CHANNEL")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("تطبيق اترجة")
            .setContentText("")
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setSilent(true)
            .build()

        startForeground(1, notification);
    }

    fun updateNotification() {
        val notificationLayout = buildNotificationLayout(_currentPlayingTitle)

        val notification = NotificationCompat.Builder(this, "PLAYER_CHANNEL")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("تطبيق اترجة")
            .setContentText("")
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setSilent(true)
            .build()

        println("noti manager ${::notificationManager.isInitialized}")

        if (::notificationManager.isInitialized) {
            notificationManager.notify(1, notification)
        }
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


    private fun buildNotificationLayout(title: String): RemoteViews {
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

        remoteViews.apply {
            setTextViewText(R.id.playing_title, title)
        }

        return remoteViews
    }

    override fun resumeClicked(): StateFlow<Int> {
        return resumeFlag;
    }

    fun resetPlayer() {
        exoPlayer.stop()
        exoPlayer.seekTo(0)
        exoPlayer.clearMediaItems()
    }

    fun releasePlayer() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.release()
    }

}