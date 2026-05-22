package com.mywallet.presentation.screens.savings

import app.cash.turbine.test
import com.mywallet.domain.model.SavingsGoal
import com.mywallet.domain.model.TransactionType
import com.mywallet.fakes.FakeSavingsGoalRepository
import com.mywallet.fakes.FakeTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class SavingsGoalViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SavingsGoalViewModel
    private lateinit var goalRepo: FakeSavingsGoalRepository
    private lateinit var transactionRepo: FakeTransactionRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        goalRepo = FakeSavingsGoalRepository()
        transactionRepo = FakeTransactionRepository()
        viewModel = SavingsGoalViewModel(goalRepo, transactionRepo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addGoal should insert goal correctly`() = runTest {
        viewModel.addGoal("Mobil", 5000.0, "Kendaraan", "#FF0000", null)
        advanceUntilIdle()
        
        viewModel.goals.test {
            val goals = awaitItem()
            assertEquals(1, goals.size)
            assertEquals("Mobil", goals[0].title)
        }
    }

    @Test
    fun `updateCurrentAmount should create an expense transaction when saving more`() = runTest {
        // Arrange
        val goal = SavingsGoal(1, "Laptop", 1000.0, 0.0, "Elektronik", "#00FF00", null)
        goalRepo.insertGoal(goal)
        advanceUntilIdle()

        // Act
        viewModel.updateCurrentAmount(1, 200.0) // Saving 200
        advanceUntilIdle()

        // Assert
        viewModel.goals.test {
            val goals = awaitItem()
            assertEquals(200.0, goals[0].currentAmount)
        }

        transactionRepo.getAllTransactions().test {
            val transactions = awaitItem()
            assertEquals(1, transactions.size)
            assertEquals("Tabungan: Laptop", transactions[0].title)
            assertEquals(200.0, transactions[0].amount)
            assertEquals(TransactionType.EXPENSE, transactions[0].type)
        }
    }

    @Test
    fun `deleteGoal should remove goal correctly`() = runTest {
        viewModel.addGoal("Test", 100.0, "Other", "#000", null)
        advanceUntilIdle()
        
        val id = viewModel.goals.value[0].id
        viewModel.deleteGoal(id)
        advanceUntilIdle()
        
        viewModel.goals.test {
            val goals = awaitItem()
            assertTrue(goals.isEmpty())
        }
    }
}
