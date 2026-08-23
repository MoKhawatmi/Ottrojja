package com.ottrojja.screens.Khitmah.khitmahListScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.room.repositories.ReminderRepository
import com.ottrojja.screens.Khitmah.KhitmahForm.KhitmahFormState
import com.ottrojja.screens.Khitmah.classes.KhitmahListItem
import com.ottrojja.screens.reminderScreen.classes.ReminderManager
import com.ottrojja.screens.reminderScreen.classes.ReminderRepeatType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KhitmahListViewModel(private val quranRepository: QuranRepository,
                           private val reminderRepository: ReminderRepository,
                           application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;

    private val _khitmahList = mutableStateOf(emptyList<KhitmahListItem>())
    var khitmahList: List<KhitmahListItem>
        get() = _khitmahList.value
        set(value) {
            _khitmahList.value = value;
        }

    private val _khitmahDialog = mutableStateOf(false)
    var khitmahDialog: Boolean
        get() = _khitmahDialog.value
        set(value) {
            _khitmahDialog.value = value;
        }


    fun fetchKhitmahList() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                quranRepository.getKhitmahList().collect { state ->
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

    fun createKhitmah(formState: KhitmahFormState) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reminderId = if (formState.reminderEnabled) {
                    val (hour, minute) = formState.reminderTime
                        .split(":")
                        .map(String::toInt)

                    ReminderManager(reminderRepository, context).createReminder(
                        title = "مذكر ختمة ${formState.khitmah.title}",
                        customMessage = "موعدك للاستمرار في ختمة ${formState.khitmah.title}",
                        hour = hour,
                        minute = minute,
                        repeatType = ReminderRepeatType.DAILY
                    )
                } else {
                    null
                }

                quranRepository.insertKhitmah(
                    formState.khitmah.copy(reminderId = reminderId)
                )

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
