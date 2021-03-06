/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.libs.db.AppDatabase
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class BookmarkInitializerTest {

    @InjectMockKs
    private lateinit var bookmarkInitializer: BookmarkInitializer

    @MockK
    private lateinit var favicons: FilesDir

    @MockK
    private lateinit var databaseFinder: DatabaseFinder

    @MockK
    private lateinit var appDatabase: AppDatabase

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var onComplete: () -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(Dispatchers.Unconfined)

        every { databaseFinder.invoke(any()) }.returns(appDatabase)
        every { appDatabase.bookmarkRepository() }.returns(bookmarkRepository)
        coEvery { bookmarkRepository.add(any()) }.answers { Unit }
        coEvery { onComplete.invoke() }.answers { Unit }

        val file = mockk<File>()
        coEvery { file.getAbsolutePath() }.returns("/test/test")
        coEvery { favicons.assignNewFile(any<String>()) }.returns(file)

        val uri = mockk<Uri>()
        coEvery { uri.getHost() }.returns("test")
        mockkStatic(Uri::class)
        coEvery { Uri.parse(any()) }.returns(uri)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun invoke() {
        bookmarkInitializer.invoke(context, onComplete)

        verify(exactly = 1) { databaseFinder.invoke(any()) }
        verify(exactly = 1) { appDatabase.bookmarkRepository() }
        coVerify(atLeast = 1) { bookmarkRepository.add(any()) }
        coVerify(atLeast = 1) { favicons.assignNewFile(any<String>()) }
    }

}