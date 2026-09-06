package com.ottrojja.screens.Khitmah.khitmahScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.FormValidationResult
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.composables.quranRangeSelector.QuranRangeState
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahSession
import com.ottrojja.room.entities.Reminder
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.room.repositories.ReminderRepository
import com.ottrojja.screens.Khitmah.KhitmahForm.KhitmahFormState
import com.ottrojja.screens.Khitmah.classes.DayReadingInfo
import com.ottrojja.screens.Khitmah.classes.SurahProgress
import com.ottrojja.screens.Khitmah.classes.SurahVerseRange
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.screens.reminderScreen.classes.ReminderManager
import com.ottrojja.screens.reminderScreen.classes.ReminderRepeatType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.max
import kotlin.math.min

class KhitmahViewModel(
    private val quranRepository: QuranRepository,
    private val reminderRepository: ReminderRepository,
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext


    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private val _khitmah = MutableStateFlow<Khitmah?>(null)
    val khitmah: StateFlow<Khitmah?> = _khitmah.asStateFlow()

    private val _reminder = MutableStateFlow<Reminder?>(null)
    val reminder: StateFlow<Reminder?> = _reminder.asStateFlow()

    private val _sessions = MutableStateFlow<List<KhitmahSession>>(emptyList())
    val sessions: StateFlow<List<KhitmahSession>> = _sessions.asStateFlow()

    private val _parts = MutableStateFlow<List<PartData>>(emptyList())
    val parts: StateFlow<List<PartData>> = _parts.asStateFlow()

    private val _surahProgress =
        MutableStateFlow<List<SurahProgress>>(emptyList())
    val surahProgress = _surahProgress.asStateFlow()

    private val _calendarMonthProgress = MutableStateFlow(0)
    val calendarMonthProgress = _calendarMonthProgress.asStateFlow()

    private val _selectedSession = MutableStateFlow<SelectedSessionState?>(null)
    val selectedSession: StateFlow<SelectedSessionState?> = _selectedSession.asStateFlow()


    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------
    val reminderManager = ReminderManager(reminderRepository, context)

    init {
        loadParts()
    }


    // -------------------------------------------------------------------------
    // Loading
    // -------------------------------------------------------------------------

    private fun loadParts() {
        viewModelScope.launch(Dispatchers.IO) {
            _parts.value = quranRepository.getAllParts()
        }
    }

    fun fetchKhitmah(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val khitmahWithSessions = quranRepository.getKhitmah(id)
                    ?: throw Exception("Khitmah not found")

                val fetchedKhitmah = khitmahWithSessions.khitmah

                val fetchedReminder = fetchedKhitmah.reminderId?.let { reminderId ->
                    reminderRepository.getById(reminderId)
                }

                _khitmah.value = fetchedKhitmah
                _sessions.value = khitmahWithSessions.sessions
                _reminder.value = fetchedReminder

            } catch (e: Exception) {
                e.printStackTrace()

                reportException(
                    exception = e,
                    file = "KhitmahViewModel"
                )

                showErrorToast()
            }
        }
    }

    fun loadSurahProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val chapters = quranRepository.getAllChapters()
                val surahRanges = quranRepository.getSurahVerseRanges()
                val sessions = _sessions.value

                val rangesBySurah = surahRanges.associateBy {
                    it.surahNum
                }

                val progress = chapters.mapNotNull { chapter ->
                    val range = rangesBySurah[chapter.surahId]
                        ?: return@mapNotNull null

                    SurahProgress(
                        chapter = chapter,
                        progress = calculateSurahProgress(
                            range = range,
                            sessions = sessions
                        )
                    )
                }

                _surahProgress.value = progress

            } catch (e: Exception) {
                e.printStackTrace()

                reportException(
                    exception = e,
                    file = "KhitmahViewModel"
                )
            }
        }
    }


    // -------------------------------------------------------------------------
    // Progress
    // -------------------------------------------------------------------------

    fun calculatePartProgress(part: PartData): Float {
        val totalPages =
            part.partLastPage.toInt() -
                    part.partStartPage.toInt() + 1

        if (totalPages <= 0) {
            return 0f
        }

        val completedPages =
            (part.partStartPage.toInt()..part.partLastPage.toInt())
                .count { page ->
                    _sessions.value.any { session ->
                        page in session.startPage..session.endPage
                    }
                }

        return completedPages.toFloat() / totalPages
    }

    private fun calculateSurahProgress(
        range: SurahVerseRange,
        sessions: List<KhitmahSession>
    ): Float {
        val surahStart = range.firstVerseId
        val surahEnd = range.lastVerseId

        if (surahStart > surahEnd) {
            return 0f
        }

        val coveredRanges = sessions
            .mapNotNull { session ->
                val start = max(session.startVerseId, surahStart)
                val end = min(session.endVerseId, surahEnd)

                if (start <= end) {
                    start..end
                } else {
                    null
                }
            }
            .sortedBy { it.first }

        if (coveredRanges.isEmpty()) {
            return 0f
        }

        var coveredStart = coveredRanges.first().first
        var coveredEnd = coveredRanges.first().last

        var coveredLength = 0

        for (currentRange in coveredRanges.drop(1)) {
            if (currentRange.first <= coveredEnd + 1) {
                coveredEnd = max(coveredEnd, currentRange.last)
            } else {
                coveredLength += coveredEnd - coveredStart + 1
                coveredStart = currentRange.first
                coveredEnd = currentRange.last
            }
        }

        coveredLength += coveredEnd - coveredStart + 1

        val surahLength = surahEnd - surahStart + 1

        return (coveredLength.toFloat() / surahLength)
            .coerceIn(0f, 1f)
    }

    // -------------------------------------------------------------------------
    // Calendar
    // -------------------------------------------------------------------------

    fun calendarMonthChanged(month: YearMonth) {
        val sessionCount = _sessions.value.count { session ->
            runCatching {
                val date = LocalDate.parse(session.readingDate)

                date.year == month.year &&
                        date.month == month.month
            }.getOrDefault(false)
        }

        _calendarMonthProgress.value = sessionCount
    }


    // -------------------------------------------------------------------------
    // Khitmah operations
    // -------------------------------------------------------------------------

    fun deleteKhitmah() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val khitmah = _khitmah.value ?: return@launch

                khitmah.reminderId?.let { reminderId ->
                    reminderRepository.getById(reminderId)?.let { reminder ->
                        reminderManager.deleteReminder(reminder)
                    }
                }

                quranRepository.deleteKhitmah(khitmah)

                showToast("تم الحذف بنجاح")

            } catch (e: Exception) {
                e.printStackTrace()

                reportException(
                    exception = e,
                    file = "KhitmahViewModel"
                )

                showErrorToast()
            }
        }
    }

    fun updateKhitmah(form: KhitmahFormState) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                var updatedKhitmah = form.khitmah
                val existingReminder = _reminder.value

                if (form.reminderEnabled) {

                    if (existingReminder == null) {
                        val reminderId = reminderManager.createReminder(
                            title = "مذكر ختمة ${form.khitmah.title}",
                            customMessage =
                                "موعدك للاستمرار في ختمة ${form.khitmah.title}",
                            hour = form.reminderTime
                                .split(":")[0]
                                .toInt(),
                            minute = form.reminderTime
                                .split(":")[1]
                                .toInt(),
                            repeatType = ReminderRepeatType.DAILY
                        )

                        updatedKhitmah = updatedKhitmah.copy(
                            reminderId = reminderId
                        )

                    } else {
                        val updatedReminder = existingReminder.copy(
                            title = "مذكر ختمة ${form.khitmah.title}",
                            customMessage =
                                "موعدك للاستمرار في ختمة ${form.khitmah.title}",
                            hour = form.reminderTime
                                .split(":")[0]
                                .toInt(),
                            minute = form.reminderTime
                                .split(":")[1]
                                .toInt(),
                            repeatType = ReminderRepeatType.DAILY
                        )

                        reminderManager.upsertReminder(updatedReminder)

                        updatedKhitmah = updatedKhitmah.copy(
                            reminderId = existingReminder.id
                        )
                    }

                } else if (existingReminder != null) {

                    reminderManager.deleteReminder(existingReminder)

                    updatedKhitmah = updatedKhitmah.copy(
                        reminderId = null
                    )
                }

                quranRepository.updateKhitmah(updatedKhitmah)

                _khitmah.value = updatedKhitmah

                _reminder.value =
                    updatedKhitmah.reminderId?.let { reminderId ->
                        reminderRepository.getById(reminderId)
                    }

                showToast("تم التعديل بنجاح")

            } catch (e: Exception) {
                e.printStackTrace()

                reportException(
                    exception = e,
                    file = "KhitmahViewModel"
                )

                showErrorToast()
            }
        }
    }


    // -------------------------------------------------------------------------
    // Session operations
    // -------------------------------------------------------------------------

    fun deleteSession() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                quranRepository.deleteSession(_selectedSession.value!!.session)
                fetchKhitmah(_khitmah.value!!.id)
            } catch (e: Exception) {
                e.printStackTrace()

                reportException(
                    exception = e,
                    file = "KhitmahViewModel"
                )
                showErrorToast()

            }
        }
    }

    fun selectSession(session: KhitmahSession) {
        if (_selectedSession.value?.session?.id == session.id) {
            _selectedSession.value = null
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _selectedSession.value = SelectedSessionState(
                session = session,
                isLoading = true
            )

            val range = quranRepository.getQuranRangeFromDetails(
                startSurahId = session.startSurahId,
                startVerseId = session.startVerseId,
                endSurahId = session.endSurahId,
                endVerseId = session.endVerseId
            )

            _selectedSession.value = SelectedSessionState(
                session = session,
                range = range,
                isLoading = false
            )
        }
    }

    fun unselectSession() {
        _selectedSession.value = null
    }

    fun toggleKhitmahStatus() {
        // Intentionally left unused for now.
    }


    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private suspend fun showErrorToast() {
        showToast("حصل خطأ يرجى المحاولة لاحقا")
    }
}


class KhitmahViewModelFactory(
    private val quranRepository: QuranRepository,
    private val reminderRepository: ReminderRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(KhitmahViewModel::class.java)) {
            return KhitmahViewModel(
                quranRepository = quranRepository,
                reminderRepository = reminderRepository,
                application = application
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}