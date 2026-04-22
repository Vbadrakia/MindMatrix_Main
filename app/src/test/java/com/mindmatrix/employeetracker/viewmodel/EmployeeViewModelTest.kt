package com.mindmatrix.employeetracker.viewmodel

import com.mindmatrix.employeetracker.data.model.Employee
import com.mindmatrix.employeetracker.data.model.UserRole
import com.mindmatrix.employeetracker.data.repository.IEmployeeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class EmployeeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: IEmployeeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(IEmployeeRepository::class.java)
        `when`(repository.getAllEmployees()).thenReturn(MutableStateFlow(emptyList()))
        `when`(repository.getEmployeeById(anyString())).thenReturn(flowOf(null))
        runTest { `when`(repository.syncEmployees()).thenReturn(Result.success(Unit)) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filterByRole applies lead department filter`() = runTest {
        val employees = listOf(
            Employee(id = "1", name = "A", department = "Engineering"),
            Employee(id = "2", name = "B", department = "HR")
        )
        `when`(repository.getAllEmployees()).thenReturn(MutableStateFlow(employees))

        val vm = EmployeeViewModel(repository)
        vm.filterByRole(Employee(id = "lead", role = UserRole.LEAD, department = "Engineering"))

        assertEquals(1, vm.state.value.filteredEmployees.size)
        assertEquals("Engineering", vm.state.value.filteredEmployees.first().department)
    }

    @Test
    fun `filterByRole clears department filter for admin`() = runTest {
        val employees = listOf(
            Employee(id = "1", department = "Engineering"),
            Employee(id = "2", department = "HR")
        )
        `when`(repository.getAllEmployees()).thenReturn(MutableStateFlow(employees))

        val vm = EmployeeViewModel(repository)
        vm.filterByDepartment("Engineering")
        vm.filterByRole(Employee(id = "admin", role = UserRole.ADMIN))

        assertNull(vm.state.value.selectedDepartment)
        assertEquals(2, vm.state.value.filteredEmployees.size)
    }
}
