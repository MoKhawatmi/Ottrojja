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
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ottrojja.R
import com.ottrojja.classes.Helpers
import com.ottrojja.classes.QuranPlayingParameters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import androidx.core.net.toUri
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.STATE_ENDED
import com.ottrojja.classes.ConnectivityMonitor
import com.ottrojja.classes.Helpers.repetitionOptionsMap
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.QuranListeningMode
import com.ottrojja.screens.mainScreen.ChapterData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

interface QuranServiceInterface {
    fun playTrack(parameters: QuranPlayingParameters)
    fun getPlayingState(): StateFlow<Boolean>
    fun pause()
    fun increaseSpeed()
    fun decreaseSpeed()
    fun getSliderPosition(): StateFlow<Float>
    fun getSliderMaxDuration(): StateFlow<Float>
    fun setSliderPosition(value: Float)
    fun getPaused(): StateFlow<Boolean>
    fun getDestroyed(): StateFlow<Boolean>
    fun playNextChapter()
    fun resumeClicked(): StateFlow<Int> //to serve as a notification to viewmodels that the resume was clicked in order to update UI playing status
    fun getPlaybackSpeed(): StateFlow<Float>
    fun getCurrentPlayingTitle(): StateFlow<String>
    fun setCurrentPlayingTitle(value: String)
    fun getCurrentPlayingParameters(): StateFlow<QuranPlayingParameters?>
    fun setCurrentPlayingParameters(value: QuranPlayingParameters)
}

class QuranPlayerService : Service(), QuranServiceInterface {

    var length: Long = 0;
    var currentlyPlaying = "";
    private var _playbackSpeed = MutableStateFlow(1.0f)
    private val _isPlaying = MutableStateFlow(false)
    private val _isPaused = MutableStateFlow(false)
    private val _sliderPosition = MutableStateFlow(0f)
    private val _maxDuration = MutableStateFlow(0f)
    private val _destroyed = MutableStateFlow(false)
    private val resumeFlag = MutableStateFlow(0)
    private val _currentPlayingTitle = MutableStateFlow("")
    private val _currentPlayingParameters = MutableStateFlow<QuranPlayingParameters?>(null)
    var surahRepeatedTimes = 0;
    var verseRepeatedTimes = 0;
    var rangeRepeatedTimes = 0;
    var currentPlayingIndex = 0;
    var currentPlayList = emptyList<MediaItem>();
    var networkConnected = false;

    lateinit var exoPlayer: ExoPlayer;

    private lateinit var notificationManager: NotificationManager

    private val handler = Handler(Looper.getMainLooper())

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var connectivityMonitor: ConnectivityMonitor

    override fun onCreate() {
        super.onCreate()
        connectivityMonitor = ConnectivityMonitor(this)
        connectivityMonitor.start()

        serviceScope.launch {
            connectivityMonitor.online.collect { isOnline ->
                println("online status $isOnline")
                networkConnected = isOnline;
            }
        }
    }


    inner class YourBinder : Binder() {
        fun getService(): QuranServiceInterface {
            return this@QuranPlayerService;
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

                    if (playbackState == STATE_ENDED) {
                        println("ExoPlayer State Ended")

                        if (_currentPlayingParameters.value!!.listeningMode == QuranListeningMode.سورة_كاملة) {
                            val currentIndex = exoPlayer.currentMediaItemIndex
                            if (surahRepeatedTimes < repetitionOptionsMap.getOrDefault(
                                    _currentPlayingParameters.value?.surahRepetitions, 0
                                )) {
                                surahRepeatedTimes++;
                                exoPlayer.seekTo(currentIndex, 0)
                                exoPlayer.play()
                            } else {
                                prepareForNewTrack()
                                if (_currentPlayingParameters.value!!.continuousChapterPlaying && _currentPlayingParameters.value?.selectedSurah?.surahId != 114) {
                                    playNextChapter();
                                }
                            }

                        } else if (_currentPlayingParameters.value!!.listeningMode == QuranListeningMode.مقطع_ايات) {
                            if (verseRepeatedTimes < repetitionOptionsMap.getOrDefault(
                                    _currentPlayingParameters.value?.verseRepetitions, 0
                                )
                                && _currentPlayingParameters.value!!.playListItems!!.get(
                                    currentPlayingIndex
                                ).split("/").last() != "basmalah.mp3") {
                                verseRepeatedTimes++;
                                exoPlayer.seekTo(0)
                                exoPlayer.play()
                            } else {
                                verseRepeatedTimes = 0;
                                length = 0;
                                if (currentPlayingIndex != currentPlayList.lastIndex) {
                                    currentPlayingIndex++;
                                    playItem(currentPlayList.get(currentPlayingIndex));
                                } else {
                                    if (rangeRepeatedTimes < repetitionOptionsMap.getOrDefault(
                                            _currentPlayingParameters.value?.verseRangeRepetitions,
                                            0
                                        )) {
                                        rangeRepeatedTimes++;
                                        currentPlayingIndex = 0;
                                        playItem(currentPlayList.get(currentPlayingIndex));
                                    } else {
                                        prepareForNewTrack()
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
                    reportException(exception = error, file = "QuranPlayerService")
                    Toast.makeText(context, "حصل خطأ، يرجى المحاولة مجددا", Toast.LENGTH_LONG)
                        .show()
                }
            }
        )
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


    override fun getSliderMaxDuration(): StateFlow<Float> {
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
        _currentPlayingTitle.value = value;
        updateNotification()
    }

    override fun getCurrentPlayingTitle(): StateFlow<String> {
        return _currentPlayingTitle;
    }

    override fun getPlayingState(): StateFlow<Boolean> {
        println("exo player playing ${exoPlayer.isPlaying}")
        _isPlaying.value = exoPlayer.isPlaying;
        return _isPlaying
    }


    private fun resumeTrack() {
        println("resuming")
        _isPaused.value = false;
        _isPlaying.value = true;
        exoPlayer.play()
    }

    override fun playTrack(parameters: QuranPlayingParameters) {
        println("playing parameters")
        println(parameters)
        //this would check if the listening mode was switched if new playing parameters were set so that we may play a new track or resume the current track
        if (_isPaused.value && parameters == _currentPlayingParameters.value) {
            resumeTrack()
        } else {
            _currentPlayingParameters.value = parameters;
            playNewTrack(parameters)
        }
    }

    private fun playNewTrack(parameters: QuranPlayingParameters) {
        println("playing new track")
        prepareForNewTrack();
        currentlyPlaying = "${parameters.startingSurah?.chapterName} ${parameters.startingVerse} - ${parameters.endSurah?.chapterName} ${parameters.endVerse}";
        updatePlaybackSpeed()

        val externalFilesDir = this.getExternalFilesDir(null);

        val mediaItems = parameters.playListItems!!.map { url ->
            //check offline playing possiblity
            val path = url.split("/").last()
            val localFile = File(externalFilesDir, path)
            if (!localFile.exists() && !networkConnected) {
                Toast.makeText(this, "لا يوجد اتصال بالانترنت", Toast.LENGTH_LONG).show()
                prepareForNewTrack();
                return;
            }
            if (localFile.exists()) {
                println("file found ${localFile.path}")
                MediaItem.fromUri(Uri.fromFile(localFile))
            } else {
                MediaItem.fromUri(url.toUri())
            }
        }

        currentPlayList = mediaItems;

        _isPlaying.value = true;
        playItem(currentPlayList.get(currentPlayingIndex));
    }

    fun playItem(item: MediaItem) {
        println("play item")

        exoPlayer.apply {
            setMediaItem(item)
            prepare()
            playWhenReady = true
            play()
        }
    }


    private fun prepareForNewTrack() {
        handler.removeCallbacksAndMessages(null)
        length = 0;
        surahRepeatedTimes = 0;
        rangeRepeatedTimes = 0;
        verseRepeatedTimes = 0;
        _maxDuration.value = 0f;
        _sliderPosition.value = 0f;
        _playbackSpeed.value = 1.0f
        currentPlayingIndex = 0;
        currentPlayList = emptyList<MediaItem>();
        _isPlaying.value = false;
        _isPaused.value = false;
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

    override fun getCurrentPlayingParameters(): StateFlow<QuranPlayingParameters?> {
        return _currentPlayingParameters;
    }

    override fun setCurrentPlayingParameters(value: QuranPlayingParameters) {
        _currentPlayingParameters.value = value;
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
                        _currentPlayingParameters.value = null;
                        _isPlaying.value = false;
                        _isPaused.value = false;
                    }

                    Actions.TERMINATE.toString() -> {
                        println("stopping self")
                        handler.removeCallbacksAndMessages(null)
                        if (::exoPlayer.isInitialized) {
                            releasePlayer()
                        }
                        _currentPlayingParameters.value = null;
                        _isPlaying.value = false;
                        _isPaused.value = false;
                        _destroyed.value = true;
                        connectivityMonitor.stop()
                        serviceScope.cancel()
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
                reportException(exception = e, file = "PagePlayerService")
            }
        }

        return android.app.Service.START_REDELIVER_INTENT
    }

    fun startService() {
        println("starting quran player service")

        if (!::notificationManager.isInitialized) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE
            ) as NotificationManager
        }

        if (::exoPlayer.isInitialized) {
            releasePlayer()
        }
        initializePlayer()

        val notificationLayout = buildNotificationLayout(_currentPlayingTitle.value)

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
        val notificationLayout = buildNotificationLayout(_currentPlayingTitle.value)

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
        if (_currentPlayingParameters.value?.selectedSurah?.surahId != null && _currentPlayingParameters.value?.selectedSurah?.surahId != 114) {
            playTrack(
                _currentPlayingParameters.value!!.copy(
                    playListItems = mutableListOf(
                        "https://ottrojja.fra1.cdn.digitaloceanspaces.com/chapters/${_currentPlayingParameters.value?.selectedSurah?.surahId!! + 1}.mp3"
                    ),
                    listeningMode = QuranListeningMode.سورة_كاملة,
                    selectedSurah = ChapterData("", "",
                        _currentPlayingParameters.value?.selectedSurah?.surahId!! + 1, "", 0
                    )
                )
            )
        }
    }


    enum class Actions {
        START, TERMINATE, STOP, NOTI_PLAY, NOTI_PAUSE
    }

    private fun getPendingIntentForAction(action: String): PendingIntent {
        val intent = Intent(this, QuranPlayerService::class.java)
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