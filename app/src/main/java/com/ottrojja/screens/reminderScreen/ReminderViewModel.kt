package com.ottrojja.screens.reminderScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.classes.ReminderRepeatType
import com.ottrojja.classes.ReminderScheduler
import com.ottrojja.room.entities.Reminder
import com.ottrojja.room.repositories.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReminderViewModel(private val repository: ReminderRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;
    private val scheduler = ReminderScheduler(context)

    private val _reminders = MutableStateFlow<List<ExpandableItem<Reminder>>>(emptyList())
    val reminders: StateFlow<List<ExpandableItem<Reminder>>> = _reminders

    private val _reminderDialog = mutableStateOf(false)
    var reminderDialog: Boolean
        get() = _reminderDialog.value
        set(value: Boolean) {
            _reminderDialog.value = value
        }

    private val _dialogMode = mutableStateOf(ModalFormMode.ADD)
    var dialogMode: ModalFormMode
        get() = _dialogMode.value
        set(value: ModalFormMode) {
            _dialogMode.value = value
        }

    private val _selectRepetitionDialog = mutableStateOf(false)
    var selectRepetitionDialog: Boolean
        get() = _selectRepetitionDialog.value
        set(value: Boolean) {
            _selectRepetitionDialog.value = value
        }

    private val _timePickerDialog = mutableStateOf(false)
    var timePickerDialog: Boolean
        get() = _timePickerDialog.value
        set(value: Boolean) {
            _timePickerDialog.value = value
        }


    val baseReminder = Reminder(title = "", customMessage = "", hour = 12, minute = 0, repeatType = ReminderRepeatType.ONCE)
    private val _reminderInWorks = mutableStateOf(baseReminder)
    var reminderInWorks: Reminder
        get() = _reminderInWorks.value
        set(value: Reminder) {
            _reminderInWorks.value = value
        }


    fun getReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllReminders().collect { newReminders ->
                _reminders.update { currentList ->
                    val currentMap = currentList.associateBy { it.data.id }

                    newReminders.map { reminder ->
                        val existing = currentMap[reminder.id]
                        if (existing != null) {
                            // Preserve expanded, but create new object for Flow/Compose to detect change
                            ExpandableItem(
                                data = reminder,
                                expanded = existing.expanded
                            )
                        } else {
                            ExpandableItem(reminder)
                        }
                    }
                }
            }
        }
    }


    fun toggleItemExpanded(id: Int) {
        _reminders.update { currentList ->
            currentList.map { item ->
                if (item.data.id == id) item.copy(expanded = !item.expanded)
                else item
            }
        }
    }

    fun toggleReminderEnabled(id: Int) {
        val reminder = _reminders.value.find { it.data.id == id }
        if (reminder == null) return
        val editedReminder = reminder.data.copy(isEnabled = !reminder.data.isEnabled)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateReminder(editedReminder)
                if (editedReminder.isEnabled) {
                    scheduler.scheduleReminder(editedReminder)
                } else {
                    scheduler.cancelReminder(editedReminder)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                    reportException(e, "ReminderViewModel", "error in toggleItemEnabled db update")
                }
            }
        }
    }

    fun invoekeAddDialog() {
        _dialogMode.value = ModalFormMode.ADD
        _reminderInWorks.value = baseReminder;
        _reminderDialog.value = true;

    }

    fun invokeEditDialog(reminder: Reminder) {
        _dialogMode.value = ModalFormMode.EDIT
        _reminderInWorks.value = reminder;
        _reminderDialog.value = true;
    }

    fun upsertReminder() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reminderId = repository.upsertReminder(_reminderInWorks.value)
                val newlyUpsertedReminder = repository.getById(reminderId.toInt())
                    ?: throw Exception("fetching of newlyUpsertedReminder return null")
                withContext(Dispatchers.Main) {
                    val toastMsg = if (_dialogMode.value == ModalFormMode.EDIT) "تم التعديل بنجاح" else "تمت الاضافة بنجاح";
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                    scheduler.scheduleReminder(newlyUpsertedReminder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reportException(e, "ReminderViewModel", "error upserting reminder")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        // cancel and delete reminder
        scheduler.cancelReminder(reminder)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteReminder(reminder)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "تم الحذف بنجاح", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                reportException(e, "ReminderViewModel", "Error while deleting reminder")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    fun insertMainReminder() {
        /* this is to insert the main daily reminder of the app, this reminder can not be deleted,
        can only be edited */
        val mainReminder = Reminder(
            id = 0,
            title = "المذكر اليومي",
            customMessage = DynamicReminderMessageProvider.getMessage(),
            hour = 12,
            minute = 0,
            repeatType = ReminderRepeatType.DAILY,
            isEnabled = true,
            isMain = true
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertReminder(mainReminder)
        }
    }


    /*
    *
    fun sendTestNotification() {
        NotificationHelper.showNotification(
            context,
            id = 999, // arbitrary test ID
            title = "Test Reminder",
            message = "This is an instant test notification."
        )
    }

    private val scheduler = ReminderScheduler(context)

    fun sendTestScheduledNotification() {
        // Create test reminder object

        println("creating test reminder")

        val reminder = Reminder(
            id = 999,
            title = "Test Reminder",
            customMessage = "This is a scheduled test notification.",
            hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            minute = Calendar.getInstance().get(Calendar.MINUTE) + 1,
            repeatType = ReminderRepeatType.DAILY,
            isEnabled = true,
            lastTriggered = 0
        )

        viewModelScope.launch(Dispatchers.IO) {
            println("inserting")
            println(reminder)
            println(repository.insertReminder(reminder))
            scheduler.scheduleReminder(reminder)
        }
    }*/

}


class ReminderViewModelFactory(
    private val repository: ReminderRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            return ReminderViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}