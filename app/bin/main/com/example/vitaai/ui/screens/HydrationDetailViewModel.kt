package com.example.vitaai.ui.screens

import androidx.lifecycle.ViewModel
import com.example.vitaai.data.HydrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class HydrationDetailViewModel @Inject constructor(
    private val repository: HydrationRepository
) : ViewModel() {

    val state: StateFlow<HydrationRepository.HydrationState> = repository.state

    fun addWater(ml: Int) = repository.addWater(ml)
    fun removeLast() = repository.removeLastEntry()
}
