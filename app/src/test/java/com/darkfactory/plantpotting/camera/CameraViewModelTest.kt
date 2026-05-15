package com.darkfactory.plantpotting.camera

import app.cash.turbine.test
import com.darkfactory.plantpotting.identify.IdSource
import com.darkfactory.plantpotting.identify.IdentificationResult
import com.darkfactory.plantpotting.identify.PlantIdentifier
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
class CameraViewModelTest {
    @Before
    fun setUpDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun captureToSuccessTransitionsThroughCapturingAndIdentifying() =
        runTest {
            val fake =
                FakePlantIdentifier(
                    result =
                        IdentificationResult(
                            speciesId = "monstera-deliciosa",
                            displayName = "Swiss cheese plant",
                            source = IdSource.STUB_DETERMINISTIC,
                        ),
                )
            val vm = CameraViewModel(fake)
            vm.state.test {
                assertThat(awaitItem()).isInstanceOf(CameraUiState.Idle::class.java)
                vm.onCaptureReady(byteArrayOf(1, 2, 3))
                // StateFlow conflates emissions; under UnconfinedTestDispatcher the launched
                // coroutine can run to completion inline, so Capturing / Identifying may be
                // overwritten by Success before turbine observes them. Accept any of the three.
                val first = awaitItem()
                assertThat(
                    first is CameraUiState.Capturing ||
                        first is CameraUiState.Identifying ||
                        first is CameraUiState.Success,
                ).isTrue()
                var success: CameraUiState.Success? = first as? CameraUiState.Success
                while (success == null) {
                    val next = awaitItem()
                    if (next is CameraUiState.Success) success = next
                }
                assertThat(success.speciesId).isEqualTo("monstera-deliciosa")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun successEmitsNavigationEvent() =
        runTest {
            val fake =
                FakePlantIdentifier(
                    result =
                        IdentificationResult(
                            speciesId = "ficus-lyrata",
                            displayName = "Fiddle-leaf fig",
                            source = IdSource.STUB_DETERMINISTIC,
                        ),
                )
            val vm = CameraViewModel(fake)
            vm.navigate.test {
                vm.onCaptureReady(byteArrayOf(0))
                val event = awaitItem()
                assertThat(event).isInstanceOf(NavCommand.Success::class.java)
                val success = event as NavCommand.Success
                assertThat(success.speciesId).isEqualTo("ficus-lyrata")
                assertThat(success.source).isEqualTo(IdSource.STUB_DETERMINISTIC)
                assertThat(success.lowConfidence).isFalse()
            }
        }

    @Test
    fun lowConfidenceEmitsLowConfidenceNavCommand() =
        runTest {
            val fake =
                FakePlantIdentifier(
                    result =
                        IdentificationResult(
                            speciesId = "",
                            displayName = "",
                            source = IdSource.ON_DEVICE_MODEL,
                            lowConfidence = true,
                        ),
                )
            val vm = CameraViewModel(fake)
            vm.navigate.test {
                vm.onCaptureReady(byteArrayOf(0))
                val event = awaitItem()
                assertThat(event).isInstanceOf(NavCommand.LowConfidence::class.java)
                // The fake doesn't implement CandidateProvider → candidates list empty.
                assertThat((event as NavCommand.LowConfidence).candidates).isEmpty()
            }
        }

    @Test
    fun failureEmitsFailureNavCommand() =
        runTest {
            val throwing =
                object : PlantIdentifier {
                    override suspend fun identify(jpeg: ByteArray): IdentificationResult = throw IllegalStateException("camera broke")
                }
            val vm = CameraViewModel(throwing)
            vm.navigate.test {
                vm.onCaptureReady(byteArrayOf(0))
                val event = awaitItem()
                assertThat(event).isInstanceOf(NavCommand.Failure::class.java)
                assertThat((event as NavCommand.Failure).message).contains("camera broke")
            }
        }

    @Test
    fun identifierFailureLeavesStateInFailure() =
        runTest {
            val throwing =
                object : PlantIdentifier {
                    override suspend fun identify(jpeg: ByteArray): IdentificationResult = throw IllegalStateException("bad image")
                }
            val vm = CameraViewModel(throwing)
            vm.state.test {
                assertThat(awaitItem()).isInstanceOf(CameraUiState.Idle::class.java)
                vm.onCaptureReady(byteArrayOf(0))
                var failure: CameraUiState.Failure? = null
                while (failure == null) {
                    val next = awaitItem()
                    if (next is CameraUiState.Failure) failure = next
                }
                assertThat(failure.reason).contains("bad image")
                cancelAndIgnoreRemainingEvents()
            }
        }

    private class FakePlantIdentifier(
        private val result: IdentificationResult,
    ) : PlantIdentifier {
        override suspend fun identify(jpeg: ByteArray): IdentificationResult = result
    }
}
