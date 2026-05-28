package com.mywallet.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mywallet.domain.model.TransactionType
import com.mywallet.domain.repository.TransactionRepository
import com.mywallet.domain.repository.UserRepository
import com.mywallet.data.remote.CurrencyService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: TransactionRepository,
    private val userRepository: UserRepository,
    private val currencyService: CurrencyService
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _exchangeRate = MutableStateFlow<Double?>(null)
    val exchangeRate: StateFlow<Double?> = _exchangeRate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow<String?>(null) // null for ALL, "INCOME", "EXPENSE"
    val filterType: StateFlow<String?> = _filterType.asStateFlow()

    private val _sortOrder = MutableStateFlow(true) // true for Newest, false for Oldest
    val sortOrder: StateFlow<Boolean> = _sortOrder.asStateFlow()

    val userName: StateFlow<String> = userRepository.profileState
        .map { it.name.split(" ").firstOrNull() ?: "User" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadTransactions()
        loadExchangeRate()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadExchangeRate()
            // Transactions are reactive from SQLDelight, but we could re-trigger if needed
            _isRefreshing.value = false
        }
    }

    private fun loadExchangeRate() {
        viewModelScope.launch {
            try {
                val response = currencyService.getExchangeRates()
                _exchangeRate.value = response.rates["USD"]
            } catch (e: Exception) {
                // Ignore network errors for exchange rate
            }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            combine(
                repository.getAllTransactions(),
                _searchQuery,
                _filterType,
                _sortOrder
            ) { transactions, query, type, newestFirst ->
                val filtered = transactions.filter {
                    val matchesQuery = it.title.contains(query, ignoreCase = true) || 
                                     it.amount.toString().contains(query)
                    val matchesType = type == null || it.type.name == type
                    matchesQuery && matchesType
                }.let {
                    if (newestFirst) it.sortedByDescending { t -> t.date } 
                    else it.sortedBy { t -> t.date }
                }
                
<<<<<<< Updated upstream
                val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                if (transactions.isEmpty()) {
                    HomeUiState.Empty
                } else {
                    HomeUiState.Success(
                        transactions = filtered,
                        balance = income - expense,
                        totalIncome = income,
                        totalExpense = expense
                    )
                }
=======
                val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                HomeUiState.Success(
                    transactions = filtered,
                    balance = income - expense,
                    totalIncome = income,
                    totalExpense = expense
                )
>>>>>>> Stashed changes
            }.catch { e -> 
                _uiState.value = HomeUiState.Error(e.message ?: "Terjadi kesalahan") 
            }.collect { 
                _uiState.value = it
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterTypeChange(type: String?) {
        _filterType.value = type
    }

    fun toggleSortOrder() {
        _sortOrder.value = !_sortOrder.value
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }
}
