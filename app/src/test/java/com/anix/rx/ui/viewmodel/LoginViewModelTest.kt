package com.anix.rx.ui.viewmodel`

import androidx.arch.core.executor.testing.InstantTaskExecutorRule`
import androidx.lifecycle.SavedStateHandle`
import com.anix.rx.domain.usecase.LoginUseCase`
import com.anix.rx.domain.repository.AuthRepository`
import kotlinx.coroutines.Dispatchers`
import kotlinx.coroutines.test.UnconfinedTestDispatcher`
import kotlinx.coroutines.test.resetMain`
import kotlinx.coroutines.test.runTest`
import kotlinx.coroutines.test.setMain`
import org.junit.After`
import org.junit.Assert.*`
import org.junit.Before`
import org.junit.Rule`
import org.junit.Test`
import org.mockito.Mock`
import org.mockito.Mockito.*`
import org.mockito.junit.MockitoJUnitRunner`
import org.junit.runner.RunWith`

@RunWith(MockitoJUnitRunner::class)`
class LoginViewModelTest {`

    @get:Rule`
    val instantTaskExecutorRule = InstantTaskExecutorRule()`

    private lateinit var loginUseCase: LoginUseCase`
    private lateinit var viewModel: LoginViewModel`

    private val testDispatcher = UnconfinedTestDispatcher()`

    @Before`
    fun setup() {`
        Dispatchers.setMain(testDispatcher)`
        loginUseCase = mock(LoginUseCase::class.java)`
        viewModel = LoginViewModel(loginUseCase)`
    }`

    @After`
    fun tearDown() {`
        Dispatchers.resetMain()`
    }`

    @Test`
    fun `when login with empty fields, state should have error`() = runTest {`
        // When`
        viewModel.login("", "password")`

        // Then`
        assertTrue(viewModel.state.value.error != null)`
        assertFalse(viewModel.state.value.isLoading)`
    }`

    @Test`
    fun `when login succeeds, state should have success`() = runTest {`
        // Given`
        `when`(loginUseCase.invoke(anyString(), anyString())).thenAnswer { invocation ->`
            val callback = invocation.getArgument<(suspend () -> Result<com.anix.rx.data.model.AuthResponse>)>(2)`
            callback()`
        }`

        // When`
        viewModel.login("test@test.com", "password123")`

        // Then`
        assertTrue(viewModel.state.value.isSuccess)`
        assertFalse(viewModel.state.value.isLoading)`
    }`
}`
