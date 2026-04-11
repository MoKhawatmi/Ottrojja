package com.ottrojja.screens.reminderScreen

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.FormValidationResult
import com.ottrojja.classes.Helpers.formatDateTime
import com.ottrojja.classes.Helpers.formatTime
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.screens.reminderScreen.classes.ReminderRepeatType
import com.ottrojja.screens.reminderScreen.classes.ReminderScheduler
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

    fun updateReminder(value: Reminder) {
        _reminderInWorks.value = value
        validateReminder()
    }


    fun getReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
            } catch (e: Exception) {
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "حصل خطأ يرجى المحاولة لاحقا", Toast.LENGTH_SHORT).show()
                    reportException(e, "ReminderViewModel", "error fetching reminders")
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
                val result = repository.upsertReminder(_reminderInWorks.value)
                val reminderId =
                    if (_dialogMode.value == ModalFormMode.EDIT) _reminderInWorks.value.id else result
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
            customMessage = null,
            hour = 12,
            minute = 0,
            repeatType = ReminderRepeatType.DAILY,
            isEnabled = true,
            isMain = true
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mainExists = repository.getMainReminder()
                if (mainExists == null) {
                    repository.insertReminder(mainReminder)
                }
            } catch (e: Exception) {
                reportException(e, "ReminderViewModel", "Error inserting/checking main reminder")
            }
        }
    }

    fun getNextTriggerTime(reminder: Reminder): String {
        if (reminder.repeatType == ReminderRepeatType.ONCE) {
            return "-";
        }
        return formatDateTime(scheduler.calculateNextTrigger(reminder))
    }

    fun dismissReminderForm() {
        _reminderDialog.value = false;
        _formValidationResult.value = baseResult;
        _reminderInWorks.value = baseReminder;
    }

    val baseResult = FormValidationResult(true, emptyMap());
    private val _formValidationResult = mutableStateOf(baseResult)
    var formValidationResult: FormValidationResult
        get() = _formValidationResult.value
        set(value: FormValidationResult) {
            _formValidationResult.value = value
        }

    fun handleReminderFormSubmission() {
        validateReminder()
        if (_formValidationResult.value.isValid) {
            upsertReminder()
        }
    }

    fun validateReminder() {
        val errors = mutableMapOf<String, String?>()

        if (_reminderInWorks.value.title.isBlank()) {
            errors["title"] = "يرجى التحقق من ادخال العنوان بشكل صحيح";
        }

        if (_reminderInWorks.value.hour !in 0..23 || _reminderInWorks.value.minute !in 0..59) {
            errors["time"] = "يرجى التحقق من ادخال الوقت بشكل صحيح"
        }
        _formValidationResult.value = FormValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
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