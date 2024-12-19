package com.ottrojja.classes

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ottrojja.MainActivity
import com.ottrojja.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
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
    fun getPlaybackSpeed(): StateFlow<Float>
}


class MediaPlayerService : Service(), AudioServiceInterface {
    //var mediaPlayer: MutableState<MediaPlayer>? = null
    var length: Long = 0;
    var currentlyPlaying = "";
    val playbackParams = PlaybackParams()
    private var _playbackSpeed = MutableStateFlow<Float>(1.0f)

    //lateinit var timer: Timer
    private val _isPlaying = MutableStateFlow<Boolean>(false)
    private val _isPaused = MutableStateFlow<Boolean>(false)
    private val _sliderPosition = MutableStateFlow<Float>(0f)
    private val _maxDuration = MutableStateFlow<Float>(0f)
    private val _selectedChapterId = MutableStateFlow<String>("")
    private val _playingChapter = MutableStateFlow<Boolean>(false)
    private val _playingZikr = MutableStateFlow<Boolean>(false)
    private val _destroyed = MutableStateFlow<Boolean>(false)
    private val resumeFlag = MutableStateFlow<Int>(0)
    lateinit var exoPlayer: ExoPlayer;

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
                        /*timer = Timer();
                        timer.schedule(object : TimerTask() {
                            override fun run() {
                                val currPosition = exoPlayer.currentPosition.toFloat() //mediaPlayer?.value?.currentPosition!!.toFloat();
                                // temp solution to the position decreasing for an ununderstandable reason on higher level apis,
                                // switch to exoplayer later
                                if (currPosition > _sliderPosition.value) {
                                    _sliderPosition.value = currPosition;
                                }
                            }
                        }, 0, 400)*/
                        val updateProgressAction = object : Runnable {
                            override fun run() {
                                val currPosition = exoPlayer.currentPosition.toFloat()
                                println("i run $currPosition")
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
                        /*if (::timer.isInitialized) {
                            timer.cancel();
                            timer.purge();
                        }*/
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
        /*mediaPlayer?.value?.pause()
        mediaPlayer?.value?.seekTo(value.toInt());
        mediaPlayer?.value?.start();*/
        exoPlayer.seekTo(value.toLong())
    }

    override fun isServiceRunning(): Boolean {
        return false
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
        //mediaPlayer?.value?.reset();
        resetPlayer()
    }

    private fun resumeTrack() {
        println("resuming")
        /*mediaPlayer?.value?.seekTo(length);
        mediaPlayer?.value?.start();*/
        exoPlayer.play()
    }

    private fun playNewTrack(link: String) {
        println("playing new track")
        println(link)
        prepareForNewTrack();
        currentlyPlaying = link;
        //playbackParams.speed = _playbackSpeed.value
        updatePlaybackSpeed()
        var mediaSrc: Uri;

        //check offline playing possiblity
        val path = link.split("/").last()
        val localFile = File(this.getExternalFilesDir(null), path)
        if (!localFile.exists() && !Helpers.checkNetworkConnectivity(this)) {
            Toast
                .makeText(
                    this,
                    "لا يوجد اتصال بالانترنت",
                    Toast.LENGTH_LONG
                )
                .show()
            resetAll();
            return;
        }
        if (localFile.exists()) {
            //mediaSrc = localFile.absolutePath
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


        /*mediaPlayer?.value?.apply {
            reset()
            setDataSource(mediaSrc)
            setPlaybackParams(playbackParams)
            prepareAsync()
            setOnPreparedListener {
                println("on prepared")
                _maxDuration.value = duration.toFloat()
                println("starting")
                it.start();
                timer = Timer();
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        val currPosition = mediaPlayer?.value?.currentPosition!!.toFloat();
                        // temp solution to the position decreasing for an ununderstandable reason on higher level apis,
                        // switch to exoplayer later
                        if (currPosition > _sliderPosition.value) {
                            _sliderPosition.value = currPosition;
                        }
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
        }*/
    }

    private fun prepareForNewTrack() {
        /*if (::timer.isInitialized) {
            timer.cancel();
            timer.purge();
        }*/
        handler.removeCallbacksAndMessages(null)
        length = 0;
        _maxDuration.value = 0f;
        _sliderPosition.value = 0f;
        _playbackSpeed.value = 1.0f
        //mediaPlayer?.value?.reset();
        resetPlayer()
    }

    override fun pause() {
        //length = mediaPlayer?.value?.currentPosition!!;
        length = exoPlayer.currentPosition
        _isPlaying.value = false;
        _isPaused.value = true;
        exoPlayer.pause()
        //mediaPlayer?.value?.pause()
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

    override fun onBind(p0: Intent?): IBinder? {
        return YourBinder();
    }


    fun updatePlaybackSpeed() {
        /*try {
            val playbackParams = PlaybackParams()
            playbackParams.speed = _playbackSpeed.value
            mediaPlayer?.value?.playbackParams = playbackParams;
        } catch (e: Exception) {
            e.printStackTrace()
            _playbackSpeed.value = 1.0F
            val playbackParams = PlaybackParams()
            playbackParams.speed = 1.0F
            mediaPlayer?.value?.playbackParams = playbackParams;
        }*/
        exoPlayer.playbackParameters = PlaybackParameters(_playbackSpeed.value)
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
                        /*if (::timer.isInitialized) {
                            timer.cancel();
                            timer.purge();
                        }*/
                        handler.removeCallbacksAndMessages(null)
                        //mediaPlayer?.value?.reset();
                        resetPlayer()
                        _isPlaying.value = false;
                        _isPaused.value = false;
                        _playingChapter.value = false;
                        _playingZikr.value = false;
                        _selectedChapterId.value = "";
                    }

                    Actions.TERMINATE.toString() -> {
                        println("stopping self")
                        /*if (::timer.isInitialized) {
                            timer.cancel();
                            timer.purge();
                        }*/
                        handler.removeCallbacksAndMessages(null)
                        /*if (mediaPlayer != null) {
                            mediaPlayer?.value?.reset();
                            mediaPlayer?.value?.release();
                        }
                        mediaPlayer = null;*/
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
        /*if (mediaPlayer != null) {
            mediaPlayer?.value?.reset();
            mediaPlayer?.value?.release();
        }
        mediaPlayer = mutableStateOf(MediaPlayer())*/

        if (::exoPlayer.isInitialized) {
            releasePlayer()
        }
        initializePlayer()

        val notificationLayout = buildNotificationLayout()

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