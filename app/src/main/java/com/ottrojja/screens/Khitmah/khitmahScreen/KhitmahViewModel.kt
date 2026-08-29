package com.ottrojja.screens.Khitmah.khitmahScreen

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.composables.quranRangeSelector.QuranRangeState
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.KhitmahSession
import com.ottrojja.room.entities.Reminder
import com.ottrojja.room.repositories.ReminderRepository
import com.ottrojja.screens.Khitmah.KhitmahForm.KhitmahFormState
import com.ottrojja.screens.Khitmah.classes.DayReadingInfo
import com.ottrojja.screens.mainScreen.PartData
import com.ottrojja.screens.reminderScreen.classes.ReminderManager
import com.ottrojja.screens.reminderScreen.classes.ReminderRepeatType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

class KhitmahViewModel(private val quranRepository: QuranRepository,
                       private val reminderRepository: ReminderRepository,
                       application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;

    private val _parts = MutableStateFlow<List<PartData>>(emptyList())
    val parts: StateFlow<List<PartData>> = _parts.asStateFlow()

    private val _khitmah = MutableStateFlow<Khitmah?>(null)
    val khitmah: StateFlow<Khitmah?> = _khitmah

    private val _reminder = MutableStateFlow<Reminder?>(null)
    val reminder: StateFlow<Reminder?> = _reminder

    private val _sessions = MutableStateFlow<List<KhitmahSession>>(emptyList())
    val sessions: StateFlow<List<KhitmahSession>> = _sessions

    private val _selectedSession = MutableStateFlow<SelectedSessionState?>(null)
    val selectedSession: StateFlow<SelectedSessionState?> = _selectedSession

    private val _selectedSessionRange = MutableStateFlow<QuranRangeState?>(null)
    val selectedSessionRange: StateFlow<QuranRangeState?> = _selectedSessionRange

    private val _selectedSessionRangeLoading = MutableStateFlow<Boolean>(false)
    val selectedSessionRangeLoading: StateFlow<Boolean> = _selectedSessionRangeLoading

    private val _selectedDay = MutableStateFlow<LocalDate?>(null)
    val selectedDay = _selectedDay.asStateFlow()

    private val _selectedDayReading = MutableStateFlow<DayReadingInfo?>(null)
    val selectedDayReading = _selectedDayReading.asStateFlow()

    private val _calendarMonthProgress = MutableStateFlow(0)
    val calendarMonthProgress = _calendarMonthProgress.asStateFlow()

    init {
        loadParts()
    }


    private fun loadParts() {
        viewModelScope.launch {
            _parts.value = quranRepository.getAllParts()
        }
    }

    fun calculatePartProgress(part: PartData): Float {
        val totalPages = part.partLastPage.toInt() - part.partStartPage.toInt() + 1

        if (totalPages <= 0) {
            return 0f
        }

        val completedPages = (part.partStartPage.toInt()..part.partLastPage.toInt()).count { page ->
            sessions.value.any { session ->
                page in session.startPage..session.endPage
            }
        }

        return completedPages.toFloat() / totalPages
    }


    fun calendarMonthChanged(month: YearMonth) {
        val sessionCount = _sessions.value.count { session ->
            runCatching {
                val date = LocalDate.parse(session.readingDate)

                date.year == month.year &&
                        date.month == month.month
            }.getOrDefault(false)
        }

        _calendarMonthProgress.value = sessionCount;
    }

    fun fetchKhitmah(id: Int) {
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
                println(khitmahWithSessions.sessions)
                println(_sessions.value)

            } catch (e: Exception) {
                e.printStackTrace()

                reportException(
                    exception = e,
                    file = "KhitmahViewModel"
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "حصل خطأ يرجى المحاولة لاحقا",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    fun deleteKhitmah() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                quranRepository.deleteKhitmah(_khitmah.value!!)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "تم الحذف بنجاح", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(exception = e, file = "KhitmahViewModel")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun updateKhitmah(form: KhitmahFormState) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reminderManager = ReminderManager(reminderRepository, context)
                var updatedKhitmah = form.khitmah
                val existingReminder = _reminder.value

                if (form.reminderEnabled) {

                    if (existingReminder == null) {
                        // User enabled a reminder that didn't previously exist.

                        val reminderId = reminderManager.createReminder(
                            title = "مذكر ختمة ${form.khitmah.title}",
                            customMessage = "موعدك للاستمرار في ختمة ${form.khitmah.title}",
                            hour = form.reminderTime.split(":")[0].toInt(),
                            minute = form.reminderTime.split(":")[1].toInt(),
                            repeatType = ReminderRepeatType.DAILY
                        )

                        updatedKhitmah = updatedKhitmah.copy(
                            reminderId = reminderId
                        )

                    } else {
                        // Reminder already exists, so update it.

                        val updatedReminder = existingReminder.copy(
                            title = "مذكر ختمة ${form.khitmah.title}",
                            customMessage = "موعدك للاستمرار في ختمة ${form.khitmah.title}",
                            hour = form.reminderTime.split(":")[0].toInt(),
                            minute = form.reminderTime.split(":")[1].toInt(),
                            repeatType = ReminderRepeatType.DAILY
                        )

                        reminderManager.upsertReminder(updatedReminder)

                        updatedKhitmah = updatedKhitmah.copy(
                            reminderId = existingReminder.id
                        )
                    }

                } else if (existingReminder != null) {

                    // User disabled an existing reminder.

                    reminderManager.deleteReminder(existingReminder)

                    updatedKhitmah = updatedKhitmah.copy(
                        reminderId = null
                    )
                }

                quranRepository.updateKhitmah(updatedKhitmah)

                _khitmah.value = updatedKhitmah

                // If we deleted the reminder, this becomes null.
                // If we created/updated one, fetch it again or assign the updated object.
                _reminder.value =
                    if (updatedKhitmah.reminderId != null) {
                        reminderRepository.getById(
                            updatedKhitmah.reminderId!!
                        )
                    } else {
                        null
                    }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "تم التعديل بنجاح",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()

                reportException(
                    exception = e,
                    file = "KhitmahViewModel"
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "حصل خطأ يرجى المحاولة لاحقا",
                        Toast.LENGTH_LONG
                    ).show()
                }
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

    fun toggleKhitmahStatus() {
        /* viewModelScope.launch(Dispatchers.IO) {
             try {
                 repository.updateKhitmah(
                     _khitmah.value!!.copy(isComplete = !_khitmah.value!!.isComplete)
                 )
                 withContext(Dispatchers.Main) {
                     Toast.makeText(context, "تم التحديث بنجاح", Toast.LENGTH_LONG).show()
                 }
                 fetchKhitmah(_khitmah.value!!.id)
             } catch (e: Exception) {
                 e.printStackTrace()
                 reportException(exception = e, file = "KhitmahViewModel")
                 withContext(Dispatchers.Main) {
                     Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                 }
             }
         }*/
    }


}

class KhitmahViewModelFactory(
    private val quranRepository: QuranRepository,
    private val reminderRepository: ReminderRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KhitmahViewModel::class.java)) {
            return KhitmahViewModel(quranRepository, reminderRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
