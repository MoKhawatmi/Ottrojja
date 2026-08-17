package com.ottrojja.screens.reminderScreen

import android.app.Application
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.DataStore.DataStoreRepository
import com.ottrojja.classes.ExpandableItem
import com.ottrojja.classes.FormValidationResult
import com.ottrojja.classes.Helpers.formatDateTime
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.classes.ModalFormMode
import com.ottrojja.screens.reminderScreen.classes.ReminderRepeatType
import com.ottrojja.screens.reminderScreen.classes.ReminderScheduler
import com.ottrojja.room.entities.Reminder
import com.ottrojja.room.repositories.ReminderRepository
import com.ottrojja.screens.reminderScreen.classes.ReminderManager
import com.ottrojja.services.OverlayService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ReminderViewModel(private val repository: ReminderRepository, application: Application) :
    AndroidViewModel(application) {
    val context = application.applicationContext;
    private val reminderManager = ReminderManager(
        repository = repository,
        context = context
    )

    private val _reminders = MutableStateFlow<List<ExpandableItem<Reminder>>>(emptyList())
    val reminders: StateFlow<List<ExpandableItem<Reminder>>> = _reminders

    val enabledFloatingAzakar = DataStoreRepository.floatingAzkarFlow

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


    val baseReminder = Reminder(title = "", customMessage = "", hour = 12, minute = 0, repeatType = ReminderRepeatType.DAILY, nextTrigger = 0L)
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
                withContext(Dispatchers.Main) {
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
        val reminder = _reminders.value
            .find { it.data.id == id }
            ?.data
            ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                reminderManager.setReminderEnabled(
                    reminder = reminder,
                    enabled = !reminder.isEnabled
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "حصل خطأ يرجى المحاولة لاحقا",
                        Toast.LENGTH_LONG
                    ).show()

                    reportException(
                        e,
                        "ReminderViewModel",
                        "error toggling reminder"
                    )
                }
            }
        }
    }

    fun invokeAddDialog() {
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
                val reminder = _reminderInWorks.value
                val mode = _dialogMode.value

                reminderManager.upsertReminder(reminder)

                withContext(Dispatchers.Main) {
                    val toastMsg =
                        if (mode == ModalFormMode.EDIT)
                            "تم التعديل بنجاح"
                        else
                            "تمت الاضافة بنجاح"

                    Toast.makeText(
                        context,
                        toastMsg,
                        Toast.LENGTH_SHORT
                    ).show()

                    dismissReminderForm()
                }
            } catch (e: Exception) {
                e.printStackTrace()

                reportException(
                    e,
                    "ReminderViewModel",
                    "error upserting reminder"
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


    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                reminderManager.deleteReminder(reminder)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "تم الحذف بنجاح",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()

                reportException(
                    e,
                    "ReminderViewModel",
                    "Error while deleting reminder"
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

    fun insertMainReminder() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                reminderManager.insertMainReminderIfNotExists()
            } catch (e: Exception) {
                reportException(
                    e,
                    "ReminderViewModel",
                    "Error inserting/checking main reminder"
                )
            }
        }
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

    fun activateFloatingAzkarService() {
        val intent = Intent(context, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopFloatingAzkarService() {
        context.stopService(Intent(context, OverlayService::class.java))
    }

    private val _overlayPermissionHandler = MutableStateFlow<Boolean>(false)
    var overlayPermissionHandler: StateFlow<Boolean> = _overlayPermissionHandler


    fun toggleOverlayPermissionHandler(state: Boolean) {
        _overlayPermissionHandler.value = state;
    }

    fun toggleFloatingAzkar(value: Boolean) {
        viewModelScope.launch {
            DataStoreRepository.setFloatingAzkar(value);
        }
        if (value) {
            if (!Settings.canDrawOverlays(context)) toggleOverlayPermissionHandler(true)
            else activateFloatingAzkarService()
        } else {
            stopFloatingAzkarService()
        }
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