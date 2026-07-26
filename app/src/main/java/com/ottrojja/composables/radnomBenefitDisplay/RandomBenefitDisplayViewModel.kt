package com.ottrojja.composables.radnomBenefitDisplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ottrojja.classes.Helpers
import com.ottrojja.room.repositories.QuranRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RandomBenefitDisplayViewModel(private val repository: QuranRepository) : ViewModel() {

    private var _benefit = MutableStateFlow<String>("")
    var benefit: StateFlow<String> = _benefit;
    private var _pageNumber = MutableStateFlow<String?>(null)
    var pageNumber: StateFlow<String?> = _pageNumber;



    fun fetchBenefit() {
        viewModelScope.launch(Dispatchers.IO) {
            val page = repository.getRandomPageWithBenefits()
            _benefit.value = page.benefits.random()
            _pageNumber.value= page.pageNum
        }
    }
}

class RandomBenefitDisplayModelFactory(
    private val repository: QuranRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RandomBenefitDisplayViewModel::class.java)) {
            return RandomBenefitDisplayViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}