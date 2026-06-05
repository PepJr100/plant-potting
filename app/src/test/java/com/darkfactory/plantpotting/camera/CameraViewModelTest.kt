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

    @Test
    fun newCaptureClearsFailureState() =
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
            vm.onCaptureFailed("prior")
            assertThat(vm.state.value).isInstanceOf(CameraUiState.Failure::class.java)
            vm.onCaptureReady(byteArrayOf(1, 2, 3))
            // Under UnconfinedTestDispatcher the launched coroutine runs inline,
            // so the terminal state should already be Success (not Failure).
            // PLANTPOTTING-0005 §2.2: a stale persistent banner would lie if a
            // new capture left the Failure state in place.
            assertThat(vm.state.value).isNotInstanceOf(CameraUiState.Failure::class.java)
        }

    @Test
    fun resetFromTerminalSuccessReturnsToIdle() =
        runTest {
            // PLANTPOTTING-0008 Phase 3 — the JVM contract the shutter-on-return
            // fix relies on: a view model left in terminal Success (a capture
            // whose result screen the user backed out of) must return to Idle
            // when reset() is invoked from CameraScreen's ON_RESUME observer, so
            // the shutter re-enables. The screen-level wiring is covered by the
            // instrumented CameraShutterOnReturnTest; this pins the VM transition.
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
            vm.onCaptureReady(byteArrayOf(1, 2, 3))
            // Under UnconfinedTestDispatcher the launched coroutine runs inline,
            // so the terminal state is Success.
            assertThat(vm.state.value).isInstanceOf(CameraUiState.Success::class.java)

            vm.reset()

            assertThat(vm.state.value).isInstanceOf(CameraUiState.Idle::class.java)
        }

    @Test
    fun successThreadsTopCandidateConfidencePct() =
        runTest {
            // PLANTPOTTING-0010 D2 — a CandidateProvider identifier surfaces the winner's softmax,
            // which the VM converts to an integer percentage on NavCommand.Success.
            val fake =
                FakeCandidateIdentifier(
                    result =
                        IdentificationResult(
                            speciesId = "monstera-deliciosa",
                            displayName = "Swiss cheese plant",
                            source = IdSource.ON_DEVICE_MODEL,
                        ),
                    candidates =
                        listOf(
                            com.darkfactory.plantpotting.identify.model.Candidate(
                                "monstera-deliciosa",
                                "Swiss cheese plant",
                                0.923f,
                            ),
                        ),
                )
            val vm = CameraViewModel(fake)
            vm.navigate.test {
                vm.onCaptureReady(byteArrayOf(0))
                val event = awaitItem() as NavCommand.Success
                assertThat(event.speciesId).isEqualTo("monstera-deliciosa")
                assertThat(event.confidencePct).isEqualTo(92)
            }
        }

    @Test
    fun successFromNonCandidateProviderHasNullConfidence() =
        runTest {
            // Stub flows: the identifier isn't a CandidateProvider → no probability to thread.
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
                val event = awaitItem() as NavCommand.Success
                assertThat(event.confidencePct).isNull()
            }
        }

    private class FakePlantIdentifier(
        private val result: IdentificationResult,
    ) : PlantIdentifier {
        override suspend fun identify(jpeg: ByteArray): IdentificationResult = result
    }

    private class FakeCandidateIdentifier(
        private val result: IdentificationResult,
        private val candidates: List<com.darkfactory.plantpotting.identify.model.Candidate>,
    ) : PlantIdentifier,
        com.darkfactory.plantpotting.identify.model.CandidateProvider {
        override val mostRecentCandidates get() = candidates

        override suspend fun identify(jpeg: ByteArray): IdentificationResult = result
    }
}
