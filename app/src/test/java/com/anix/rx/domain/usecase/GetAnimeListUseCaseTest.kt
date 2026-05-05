package com.anix.rx.domain.usecase`

import com.anix.rx.data.model.Anime`
import com.anix.rx.domain.repository.AnimeRepository`
import kotlinx.coroutines.test.runTest`
import org.junit.Assert.*`
import org.junit.Test`
import org.mockito.Mock`
import org.mockito.Mockito.*`
import org.mockito.junit.MockitoJUnitRunner`
import org.junit.runner.RunWith`

@RunWith(MockitoJUnitRunner::class)`
class GetAnimeListUseCaseTest {`

    @Mock`
    lateinit var animeRepository: AnimeRepository`

    @Test`
    fun `invoke should call repository getAnimeList`() = runTest {`
        // Given`
        animeRepository = mock(AnimeRepository::class.java)`
        val useCase = GetAnimeListUseCase(animeRepository)`

        // When`
        `when`(animeRepository.getAnimeList(any())).thenReturn(Result.success(emptyList()))`
        
        // Then`
        useCase(null)`
        verify(animeRepository).getAnimeList(null)`
    }`

    @Test`
    fun `invoke with query should call repository with query`() = runTest {`
        // Given`
        animeRepository = mock(AnimeRepository::class.java)`
        val useCase = GetAnimeListUseCase(animeRepository)`
        val query = "naruto"`

        // When`
        `when`(animeRepository.getAnimeList(query)).thenReturn(Result.success(emptyList()))`
        
        // Then`
        useCase(query)`
        verify(animeRepository).getAnimeList(query)`
    }`
}`
