package com.ottrojja.screens.khitmahListScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.room.entities.Khitmah
import com.ottrojja.room.entities.Reminder
import com.ottrojja.room.repositories.ReminderRepository
import com.ottrojja.screens.khitmahListScreen.classes.KhitmahFormMode
import com.ottrojja.screens.khitmahListScreen.classes.KhitmahFormState
import com.ottrojja.screens.reminderScreen.classes.ReminderManager
import com.ottrojja.screens.reminderScreen.classes.ReminderRepeatType
import com.ottrojja.screens.reminderScreen.classes.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KhitmahListViewModel(private val quranRepository: QuranRepository,
                           private val reminderRepository: ReminderRepository,
                           application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;

    private val _khitmahList = mutableStateOf(emptyList<Khitmah>())
    var khitmahList: List<Khitmah>
        get() = _khitmahList.value
        set(value) {
            _khitmahList.value = value;
        }

    private val _formState = MutableStateFlow(KhitmahFormState())
    val formState = _formState.asStateFlow()


    fun updateKhitmahInWork(khitmah: Khitmah) {
        _formState.value = _formState.value.copy(
            khitmah = khitmah
        )
    }

    fun updateReminderEnabled(enabled: Boolean) {
        _formState.value = _formState.value.copy(
            reminderEnabled = enabled
        )
    }

    fun updateReminderTime(time: String) {
        _formState.value = _formState.value.copy(
            reminderTime = time
        )
    }

    fun setFormMode(mode: KhitmahFormMode) {
        _formState.value = _formState.value.copy(
            mode = mode
        )
    }

    fun resetForm() {
        _formState.value = KhitmahFormState()
    }

    fun fetchKhitmahList() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                quranRepository.getAllKhitmah().collect { state ->
                    withContext(Dispatchers.Main) {
                        _khitmahList.value = state;
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reportException(exception = e, file = "KhitmahListViewModel")
            Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
        }
    }

    fun createKhitmah() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val form = _formState.value

                quranRepository.insertKhitmah(
                    form.khitmah.copy()
                )

                if (form.reminderEnabled) {
                    val reminderManager = ReminderManager(reminderRepository, context)
                    reminderManager.createReminder(
                        title = "مذكر ختمة ${form.khitmah.title}",
                        customMessage = "موعدك للاستمرار في ختمة ${form.khitmah.title}",
                        hour = form.reminderTime.split(":").get(0).toInt(),
                        minute = form.reminderTime.split(":").get(1).toInt(),
                        repeatType = ReminderRepeatType.DAILY
                    )
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "تمت الإضافة بنجاح",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(
                    exception = e,
                    file = "KhitmahListViewModel"
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

}

class KhitmahListViewModelFactory(
    private val quranRepository: QuranRepository,
    private val reminderRepository: ReminderRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KhitmahListViewModel::class.java)) {
            return KhitmahListViewModel(quranRepository, reminderRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
