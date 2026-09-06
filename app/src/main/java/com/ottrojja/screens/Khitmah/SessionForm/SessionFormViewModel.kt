package com.ottrojja.screens.Khitmah.SessionForm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.FormValidationResult
import com.ottrojja.classes.Helpers.getCurrentIsoDate
import com.ottrojja.classes.Helpers.reportException
import com.ottrojja.composables.quranRangeSelector.QuranRangeState
import com.ottrojja.room.entities.KhitmahSession
import com.ottrojja.room.repositories.QuranRepository
import com.ottrojja.screens.Khitmah.FormValidation.validateSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.Int

class SessionFormViewModel(
    private val quranRepository: QuranRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<SessionFormState?>(null)
    val uiState: StateFlow<SessionFormState?> = _uiState.asStateFlow()

    fun reset() {
        _uiState.value = null
    }

    fun initialize(
        khitmahId: Long,
        startPage: Int? = null,
        endPage: Int? = null,
        session: KhitmahSession? = null
    ) {
        // Don't reinitialize on recomposition
        if (_uiState.value != null) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val initialState =
                    if (session != null) {
                        createEditState(session)
                    } else {

                        createNewState(
                            khitmahId = khitmahId,
                            startPage = requireNotNull(startPage),
                            endPage = requireNotNull(endPage)
                        )

                    }
                _uiState.value = initialState

            } catch (e: Exception) {
                e.printStackTrace()

                reportException(
                    exception = e,
                    file = "SessionFormViewModel"
                )
            }
        }
    }


    private suspend fun createNewState(
        khitmahId: Long,
        startPage: Int,
        endPage: Int
    ): SessionFormState {

        val range = quranRepository.getQuranRangeFromPages(
            pages = setOf(startPage, endPage)
        )

        return SessionFormState(
            khitmahId = khitmahId,
            startPage = startPage,
            endPage = endPage,
            quranRange = range
        )
    }


    private suspend fun createEditState(
        session: KhitmahSession
    ): SessionFormState {


        val range = quranRepository.getQuranRangeFromDetails(
            startSurahId = session.startSurahId,
            startVerseId = session.startVerseId,
            endSurahId = session.endSurahId,
            endVerseId = session.endVerseId
        )

        return SessionFormState(
            khitmahId = session.khitmahId,
            session = session,
            startPage = session.startPage,
            endPage = session.endPage,
            quranRange = range,
            reflection = session.reflection.orEmpty()
        )
    }


    fun updateRange(range: QuranRangeState) {
        _uiState.update { state ->
            state?.copy(
                quranRange = range,
                startPage = range.startVerse?.pageNum?.toInt(),
                endPage = range.endVerse?.pageNum?.toInt()
            )?.also {
                validateSessionForm(it)
            }
        }
    }


    fun updateReflection(reflection: String) {
        _uiState.update {
            it?.copy(
                reflection = reflection
            )?.also {
                validateSessionForm(it)
            }
        }
    }

    private val _validationResult = MutableStateFlow(FormValidationResult(true, emptyMap()))
    val validationResult: StateFlow<FormValidationResult> = _validationResult.asStateFlow()

    fun validateSessionForm(state: SessionFormState) {
        _validationResult.value = validateSession(state)
    }


    fun save(
        onSuccess: () -> Unit
    ) {

        val state = _uiState.value ?: return

        val validationResult = validateSession(state)
        _validationResult.value = validationResult

        if (!validationResult.isValid) {
            return
        }


        val range = state.quranRange ?: return
        val startPage = state.startPage ?: return
        val endPage = state.endPage ?: return



        viewModelScope.launch(Dispatchers.IO) {
            try {
                val session = KhitmahSession(
                    id = state.session?.id ?: 0,
                    khitmahId = state.khitmahId,
                    startPage = startPage,
                    startSurahId = range.startSurah?.surahId ?: 1,
                    startVerseId = range.startVerse?.id ?: 1,
                    endPage = endPage,
                    endSurahId = range.endSurah?.surahId ?: 1,
                    endVerseId = range.endVerse?.id ?: 1,
                    timestamp = state.session?.timestamp ?: System.currentTimeMillis(),
                    readingDate = state.session?.readingDate ?: getCurrentIsoDate(),
                    reflection = state.reflection
                        .trim()
                        .takeIf { it.isNotEmpty() }
                )

                if (state.isEditMode) {
                    quranRepository.updateSession(session)
                } else {
                    quranRepository.insertSession(session)
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                e.printStackTrace()

                reportException(
                    exception = e,
                    file = "SessionFormViewModel"
                )
            }
        }
    }
}

class SessionFormViewModelFactory(
    private val quranRepository: QuranRepository,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionFormViewModel::class.java)) {
            return SessionFormViewModel(quranRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}