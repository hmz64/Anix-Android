package com.anix.rx.repository`

import com.anix.rx.data.api.AniXApi`
import com.anix.rx.data.model.ApiResponse`
import kotlinx.coroutines.test.runTest`
import okhttp3.mockwebserver.MockWebServer`
import okhttp3.mockwebserver.MockResponse`
import org.junit.After`
import org.junit.Assert.*`
import org.junit.Before`
import org.junit.Test`
import retrofit2.Retrofit`
import retrofit2.converter.kotlinx.serialization.KotlinxSerializationConverterFactory`
import java.util.concurrent.TimeUnit`

class AnimeRepositoryImplTest {`

    lateinit var mockWebServer: MockWebServer`
    lateinit var repository: AnimeRepositoryImpl`

    @Before`
    fun setup() {`
        mockWebServer = MockWebServer()`
        mockWebServer.start()`

        val retrofit = Retrofit.Builder()`
            .baseUrl(mockWebServer.url("/"))`
            .addConverterFactory(KotlinxSerializationConverterFactory.create())`
            .build()`

        val api = retrofit.create(AniXApi::class.java)`
        repository = AnimeRepositoryImpl(api)`
    }`

    @After`
    fun tearDown() {`
        mockWebServer.shutdown()`
    }`

    @Test`
    fun `getAnimeList should return success when API returns 200`() = runTest {`
        // Given`
        val response = ApiResponse(`success` = true, `data` = emptyList<Any>(), message = "OK")`  
        mockWebServer.enqueue(`MockResponse()`  
            .setResponseCode(200)`  
            .setBody("""{\"success\": true, \"data\": [], \"message\": \"OK\"}"""))`

        // When`
        val result = repository.getAnimeList()`

        // Then`
        assertTrue(result.isSuccess)`  
        assertEquals(true, result.getOrNull()?.success)`  
    }`
}`
