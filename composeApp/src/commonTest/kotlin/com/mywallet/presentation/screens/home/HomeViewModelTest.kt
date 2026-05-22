package com.mywallet.presentation.screens.home

import app.cash.turbine.test
import com.mywallet.domain.model.Transaction
import com.mywallet.domain.model.TransactionType
import com.mywallet.fakes.FakeCurrencyService
import com.mywallet.fakes.FakeTransactionRepository
import com.mywallet.fakes.FakeUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HomeViewModel
    private lateinit var transactionRepo: FakeTransactionRepository
    private lateinit var userRepo: FakeUserRepository
    private lateinit var currencyService: FakeCurrencyService

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepo = FakeTransactionRepository()
        userRepo = FakeUserRepository()
        currencyService = FakeCurrencyService()
        viewModel = HomeViewModel(transactionRepo, userRepo, currencyService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadTransactions should calculate balance correctly`() = runTest {
        val testData = listOf(
            Transaction(1, "Income", 1000.0, TransactionType.INCOME, "Gaji", "2024-01-01", "10:00"),
            Transaction(2, "Expense", 400.0, TransactionType.EXPENSE, "Makan", "2024-01-02", "12:00")
        )
        transactionRepo.emit(testData)
        
        viewModel.uiState.test {
            val state = awaitItem()
            if (state is HomeUiState.Success) {
                assertEquals(600.0, state.balance)
                assertEquals(1000.0, state.totalIncome)
                assertEquals(400.0, state.totalExpense)
                assertEquals(2, state.transactions.size)
            } else {
                fail("Expected HomeUiState.Success, got $state")
            }
        }
    }

    @Test
    fun `onSearchQueryChange should filter transactions`() = runTest {
        val testData = listOf(
            Transaction(1, "Susu", 50.0, TransactionType.EXPENSE, "Makanan", "2024-01-01", "10:00"),
            Transaction(2, "Roti", 20.0, TransactionType.EXPENSE, "Makanan", "2024-01-01", "10:00")
        )
        transactionRepo.emit(testData)
        
        viewModel.onSearchQueryChange("Susu")
        
        viewModel.uiState.test {
            val state = awaitItem()
            if (state is HomeUiState.Success) {
                assertEquals(1, state.transactions.size)
                assertEquals("Susu", state.transactions[0].title)
            } else {
                fail("Expected Success")
            }
        }
    }

    @Test
    fun `onFilterTypeChange should filter by type`() = runTest {
        val testData = listOf(
            Transaction(1, "I", 100.0, TransactionType.INCOME, "Gaji", "D", "T"),
            Transaction(2, "E", 50.0, TransactionType.EXPENSE, "Makan", "D", "T")
        )
        transactionRepo.emit(testData)
        
        viewModel.onFilterTypeChange("INCOME")
        
        viewModel.uiState.test {
            val state = awaitItem()
            if (state is HomeUiState.Success) {
                assertEquals(1, state.transactions.size)
                assertEquals(TransactionType.INCOME, state.transactions[0].type)
            } else {
                fail("Expected Success")
            }
        }
    }
}
