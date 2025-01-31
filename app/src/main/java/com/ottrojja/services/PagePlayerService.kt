package com.ottrojja.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.NotificationUtil.createNotificationChannel
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ottrojja.R
import com.ottrojja.classes.PageContent
import com.ottrojja.classes.PageContentItemType
import com.ottrojja.screens.quranScreen.RepetitionTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface PageServiceInterface {
    fun pause()
    fun getPlaying(): StateFlow<Boolean>
    fun getPaused(): StateFlow<Boolean>
    fun getContinuousPlay(): StateFlow<Boolean>
    fun getDestroyed(): StateFlow<Boolean>
    fun getPlayingIndex(): StateFlow<Int> //is it needed to be passed to vm?
    fun getStartPlayingIndex(): StateFlow<Int?>
    fun getStartPlayingItem(): StateFlow<PageContent?>
    fun getEndPlayingIndex(): StateFlow<Int?>
    fun getEndPlayingItem(): StateFlow<PageContent?>
    fun getPlaybackSpeed(): StateFlow<Float>
    fun getSelectedRepetitionTab(): StateFlow<RepetitionTab>
    fun getSelectedRepetition(): StateFlow<String>
    fun getPlayNextPage(): StateFlow<Boolean>
    fun getPlayingPageNum(): String?
    fun playNextVerse()
    fun playPreviousVerse()
    fun decreaseSpeed()
    fun increaseSpeed()
    fun playAudio()
    fun resetUIStates()
    fun resetPlayer()
    fun releasePlayer()
    fun verseHasBeenSelected()
    fun setPlayingIndex(index: Int)
    fun setStartPlayingIndex(index: Int)
    fun setStartPlayingItem(item: PageContent?)
    fun setEndPlayingIndex(index: Int?)
    fun setEndPlayingItem(item: PageContent?)
    fun setVersesPlayList(versesPlayList: Array<PageContent>)
    fun setPlayingPageNum(value: String)
    fun setContinuousPlay(value: Boolean)
    fun setSelectedRepetitionTab(value: RepetitionTab)
    fun setSelectedRepetition(value: String)
    fun setSelectedMappedRepetition(value: Int)
    fun setPlayNextPage(value: Boolean)
}

class PagePlayerService : Service(), PageServiceInterface {
    lateinit var _exoPlayer: ExoPlayer;
    var length: Long = 0;
    var repeatedTimes = 0;
    var currentPlayingPageNum: String? = null;
    var selectedMappedRepetitions: Int = 0;
    private var _versesPlayList: Array<PageContent> = emptyArray()
    private var _playbackSpeed = MutableStateFlow<Float>(1.0f)
    private val _isPlaying = MutableStateFlow<Boolean>(false)
    private val _isPaused = MutableStateFlow<Boolean>(false)
    private val _continuousPlay = MutableStateFlow<Boolean>(false)
    private val _destroyed = MutableStateFlow<Boolean>(false)
    private val _currentPlayingIndex = MutableStateFlow<Int>(0)
    private val _startPlayingIndex = MutableStateFlow<Int?>(null)
    private val _startPlayingItem = MutableStateFlow<PageContent?>(null)
    private val _endPlayingIndex = MutableStateFlow<Int?>(null)
    private val _endPlayingItem = MutableStateFlow<PageContent?>(null)
    private val _selectedRepetition = MutableStateFlow<String>("0")
    private val _selectedRepetitionTab = MutableStateFlow<RepetitionTab>(RepetitionTab.الاية)
    private val _playNextPage = MutableStateFlow<Boolean>(false) //this will act as a flag, when changed it shall signal the viewmodel to play next page

    private lateinit var notificationManager: NotificationManager

    inner class PageServiceBinder : Binder() {
        fun getService(): PageServiceInterface {
            return this@PagePlayerService;
        }
    }

    fun initializePlayer() {
        val context = this
        _exoPlayer = ExoPlayer.Builder(this).build();

        _exoPlayer.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        _isPlaying.value = true;
                        _isPaused.value = false;
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_ENDED) {
                        length = 0;
                        val repeatActive = selectedMappedRepetitions > 0;
                        // println("repeated times $repeatedTimes")

                        if (_isPlaying.value) {
                            /*println("current index: ${_currentPlayingIndex.value}")
                            println("size - 1: ${_versesPlayList.size - 1}")*/
                            // Repetition logic

                            if (repeatActive && _versesPlayList[_currentPlayingIndex.value].type == PageContentItemType.verse) {
                                if (_selectedRepetitionTab.value == RepetitionTab.الاية) {
                                    if (repeatedTimes < selectedMappedRepetitions) {
                                        repeatedTimes++;
                                        playAudio()
                                    } else if (_currentPlayingIndex.value < _versesPlayList.size - 1) {
                                        if (_currentPlayingIndex.value != _endPlayingIndex.value) {
                                            repeatedTimes = 0;
                                            playNextVerse()
                                        } else {
                                            resetPlayer();
                                            resetUIStates();
                                        }
                                    } else {
                                        // done playing, done looping
                                        resetPlayer()
                                        resetUIStates();
                                        if (currentPlayingPageNum != "604" && _continuousPlay.value) {
                                            //playNextPage()
                                            _playNextPage.value = true;
                                        }
                                    }
                                } else if (_selectedRepetitionTab.value == RepetitionTab.المقطع) {
                                    if (_currentPlayingIndex.value != _endPlayingIndex.value) {
                                        if (_currentPlayingIndex.value < _versesPlayList.size - 1) {
                                            playNextVerse();
                                        } else {
                                            resetPlayer()
                                            resetUIStates();
                                            if (currentPlayingPageNum != "604" && _continuousPlay.value) {
                                                //playNextPage()
                                                _playNextPage.value = true;
                                            }
                                        }
                                    } else if (repeatedTimes < selectedMappedRepetitions) {
                                        repeatedTimes++;
                                        _currentPlayingIndex.value = _startPlayingIndex.value ?: 0;
                                        playAudio()
                                        println("repeat times increased $repeatedTimes")
                                    } else {
                                        // done playing, done looping
                                        resetPlayer()
                                        resetUIStates();
                                    }
                                }
                            }
                            // Play next verse
                            else if (_currentPlayingIndex.value < _versesPlayList.size - 1) {
                                if (_currentPlayingIndex.value != _endPlayingIndex.value) {
                                    playNextVerse();
                                } else {
                                    resetPlayer();
                                    resetUIStates();
                                }
                            }
                            // End page media logic
                            else {
                                resetPlayer()
                                resetUIStates();
                                if (currentPlayingPageNum != "604" && _continuousPlay.value) {
                                    //playNextPage()
                                    _playNextPage.value = true;
                                }
                            }
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    println("Error in player; PagePlayerService")
                    error.printStackTrace()
                    Toast.makeText(context, "حصل خطأ، يرجى المحاولة مجددا", Toast.LENGTH_LONG)
                        .show()
                }
            }
        )
    }

    override fun getPlaying(): StateFlow<Boolean> {
        return _isPlaying;
    }

    override fun getPaused(): StateFlow<Boolean> {
        return _isPaused;
    }

    override fun getContinuousPlay(): StateFlow<Boolean> {
        return _continuousPlay
    }

    override fun getDestroyed(): StateFlow<Boolean> {
        return _destroyed;
    }

    override fun pause() {
        length = _exoPlayer.currentPosition
        _isPlaying.value = false;
        _isPaused.value = true;
        _exoPlayer.pause()
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

    override fun verseHasBeenSelected() {
        _exoPlayer.stop();
        _isPlaying.value = false;
        length = 0;
    }

    override fun getPlayingIndex(): StateFlow<Int> {
        return _currentPlayingIndex;
    }

    override fun setPlayingIndex(index: Int) {
        _currentPlayingIndex.value = index;
    }

    override fun getStartPlayingIndex(): StateFlow<Int?> {
        return _startPlayingIndex;
    }

    override fun setStartPlayingIndex(index: Int) {
        _startPlayingIndex.value = index;
    }

    override fun getStartPlayingItem(): StateFlow<PageContent?> {
        return _startPlayingItem;
    }

    override fun setStartPlayingItem(item: PageContent?) {
        _startPlayingItem.value = item;
    }

    override fun getEndPlayingIndex(): StateFlow<Int?> {
        return _endPlayingIndex;
    }

    override fun setEndPlayingIndex(index: Int?) {
        _endPlayingIndex.value = index;
    }

    override fun getEndPlayingItem(): StateFlow<PageContent?> {
        return _endPlayingItem;
    }

    override fun setEndPlayingItem(item: PageContent?) {
        _endPlayingItem.value = item;
    }

    override fun getPlaybackSpeed(): StateFlow<Float> {
        return _playbackSpeed;
    }

    override fun setVersesPlayList(versesPlayList: Array<PageContent>) {
        println("updating verses playlist")
        _versesPlayList = versesPlayList;
        println("new size ${_versesPlayList.size}")
    }

    override fun setPlayingPageNum(value: String) {
        println("service playing page num $value")
        currentPlayingPageNum = value;
        updateNotification()
    }

    override fun setContinuousPlay(value: Boolean) {
        _continuousPlay.value = value;
    }

    override fun getSelectedRepetitionTab(): StateFlow<RepetitionTab> {
        return _selectedRepetitionTab;
    }

    override fun setSelectedRepetitionTab(value: RepetitionTab) {
        _selectedRepetitionTab.value = value;
    }

    override fun getSelectedRepetition(): StateFlow<String> {
        return _selectedRepetition;
    }

    override fun setSelectedRepetition(value: String) {
        _selectedRepetition.value = value;
    }

    override fun setSelectedMappedRepetition(value: Int) {
        selectedMappedRepetitions = value;
    }

    override fun getPlayingPageNum(): String? {
        return currentPlayingPageNum;
    }

    override fun getPlayNextPage(): StateFlow<Boolean> {
        return _playNextPage;
    }

    override fun setPlayNextPage(value: Boolean) {
        _playNextPage.value = value;
    }

    override fun playAudio() {
        println("play audio")
        if (_isPaused.value && length > 0) {
            resumeTrack()
        } else {
            val context: Context = this;
            val item: PageContent = _versesPlayList[_currentPlayingIndex.value]
            var urlParam: String;
            if (item.type == PageContentItemType.surah) {
                urlParam = "1-1-1.mp3"
            } else {
                urlParam = "${currentPlayingPageNum}-${item.surahNum}-${item.verseNum}.mp3"
            }

            if (item.type == PageContentItemType.surah && (item.surahNum == "1" || item.surahNum == "9")) {
                //skip basmallah for surah 1 and 9
                _currentPlayingIndex.value++;
                playAudio()
            } else {
                try {
                    _exoPlayer.apply {
                        val mediaItem =
                            MediaItem.fromUri(
                                Uri.fromFile(
                                    File(
                                        context.getExternalFilesDir(null),
                                        urlParam
                                    )
                                )
                            )
                        setMediaItem(mediaItem)
                        prepare()
                        play()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun resetAll() {
        resetPlayer()
        resetUIStates()
    }

    private fun resumeTrack() {
        println("resuming")
        _exoPlayer.play()
        _isPlaying.value = true;
        _isPaused.value = false;
    }

    fun updatePlaybackSpeed() {
        _exoPlayer.playbackParameters = PlaybackParameters(_playbackSpeed.value)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            try {
                when (intent.action) {
                    Actions.START.toString() -> {
                        _destroyed.value = false;
                        setPlayingPageNum(intent.getStringExtra("playingPageNum")!!);
                        startService()
                    }

                    Actions.STOP.toString() -> {
                        println("stopping media inside app")
                        resetPlayer()
                        resetUIStates()
                        _isPlaying.value = false;
                        _isPaused.value = false;
                    }

                    Actions.TERMINATE.toString() -> {
                        println("stopping self")
                        if (::_exoPlayer.isInitialized) {
                            releasePlayer()
                        }
                        _isPlaying.value = false;
                        _isPaused.value = false;
                        _destroyed.value = true;
                        stopForeground(true)
                        stopSelf()
                    }

                    Actions.NOTI_PLAY.toString() -> {
                        if (!_isPlaying.value) {
                            resumeTrack()
                            //resumeFlag.value++;
                        }
                    }

                    Actions.NOTI_PAUSE.toString() -> {
                        if (_exoPlayer.isPlaying == true) {
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
        if(!::notificationManager.isInitialized){
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        if (::_exoPlayer.isInitialized) {
            releasePlayer()
        }
        initializePlayer()

        val notificationLayout = buildNotificationLayout("الصفحة $currentPlayingPageNum من القرآن الكريم")


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

    fun updateNotification(){
        val notificationLayout = buildNotificationLayout("الصفحة $currentPlayingPageNum من القرآن الكريم")


        val notification = NotificationCompat.Builder(this, "PLAYER_CHANNEL")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("تطبيق اترجة")
            .setContentText("")
            .setCustomContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setSilent(true)
            .build()

        if(::notificationManager.isInitialized){
            notificationManager.notify(1, notification)
        }
    }

    override fun playNextVerse() {
        if (_currentPlayingIndex.value == _versesPlayList.size - 1) {
            return;
        }
        _currentPlayingIndex.value++;
        playAudio()
    }

    override fun playPreviousVerse() {
        if (_currentPlayingIndex.value == 0) {
            return;
        }
        _currentPlayingIndex.value--;
        playAudio()

    }

    override fun resetUIStates() {
        _currentPlayingIndex.value = 0;
        _endPlayingIndex.value = null;
        _startPlayingIndex.value = 0;
        _startPlayingItem.value = emptyPageObject;
        _endPlayingItem.value = emptyPageObject;
        _selectedRepetition.value = "0";
        selectedMappedRepetitions = 0;
        repeatedTimes = 0;
        length = 0;
    }

    override fun resetPlayer() {
        _exoPlayer.stop();
        _exoPlayer.seekTo(0);
        _exoPlayer.clearMediaItems();
        _isPlaying.value = false;
        _isPaused.value = false;
    }

    override fun releasePlayer() {
        _exoPlayer.stop()
        _exoPlayer.clearMediaItems()
        _exoPlayer.release()
    }


    enum class Actions {
        START, TERMINATE, STOP, NOTI_PLAY, NOTI_PAUSE
    }

    private fun getPendingIntentForAction(action: String): PendingIntent {
        val intent = Intent(this, PagePlayerService::class.java)
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

    val emptyPageObject: PageContent = PageContent(
        PageContentItemType.EMPTY,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
    )


    override fun onBind(p0: Intent?): IBinder {
        return PageServiceBinder();
    }


}