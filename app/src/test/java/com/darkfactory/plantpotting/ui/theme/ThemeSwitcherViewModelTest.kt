package com.darkfactory.plantpotting.ui.theme

import app.cash.turbine.test
import com.darkfactory.plantpotting.persistence.FakePlantLogStore
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeSwitcherViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultsToLeaf() =
        runTest {
            val vm = ThemeSwitcherViewModel(FakePlantLogStore())
            vm.selected.test {
                assertThat(awaitItem()).isEqualTo(ThemeCandidate.LEAF)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun selectPersistsAndReflects() =
        runTest {
            val store = FakePlantLogStore()
            val vm = ThemeSwitcherViewModel(store)
            vm.select(ThemeCandidate.TERRACOTTA)
            assertThat(store.snapshot().themeCandidate).isEqualTo("TERRACOTTA")
            vm.selected.test {
                var v = awaitItem()
                while (v != ThemeCandidate.TERRACOTTA) v = awaitItem()
                assertThat(v).isEqualTo(ThemeCandidate.TERRACOTTA)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
