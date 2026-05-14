# Android Tech

The five technical documents that anchor the app's on-device pipeline: three for the inference runtime (Google's LiteRT, formerly TFLite) and two for the camera capture layer (Jetpack CameraX, both the official configuration reference and a 2026 Compose-first practitioner guide). LiteRT is the de-facto choice for shipping a quantised classifier on Android, with GPU and NPU acceleration; CameraX is the modern Jetpack API that frees the app from Camera2 boilerplate while still supporting custom analysis pipelines. Together these define the runtime envelope: model formats (`.tflite`), delegates (GPU / NPU), capture use cases (Preview, ImageAnalysis, ImageCapture), and the Compose lifecycle wiring needed to glue them together.

---

### LiteRT: High-Performance On-Device Machine Learning Framework (Google AI for Developers)
- **Cache file:** `ai_google_dev_edge_litert.html`
- **Source:** https://ai.google.dev/edge/litert
- **Summary:** Google's official landing page for LiteRT (Lite Runtime), the next-generation rebrand of TensorFlow Lite and the production runtime for deploying GenAI and ML models on billions of mobile and embedded devices. Pitches LiteRT as the canonical Google framework for on-device inference across Android, iOS, and embedded Linux. The entry point the app's deployment plan should start from.
- **Key concepts:** LiteRT, on-device ML, edge AI, TensorFlow Lite, TFLite, .tflite model format, GenAI on device, mobile ML, runtime SDK, LiteRT CLI, Google AI Edge
- **Notable data:**
  - Page description: "Seamlessly deploy GenAI and ML models on billions of devices with Google's high-performance framework"
  - Keyword tags: litert, edgeai, ondeviceai, tflite, genai, tensorflowlite, aiagent, ml-runtime, edge-computing, mobile-ml
  - Positioned as "Google's next-gen runtime for on-device AI"
  - Two product lines surfaced: LiteRT CLI and LiteRT SDK
- **See also:** LiteRT Overview (this file); LiteRT: The Universal Framework for On-Device AI (this file); MobileNetV4 (`ml-research.md`); ECCV 2024 Paper 05647 (`ml-research.md`)

### LiteRT Overview (Google AI for Developers)
- **Cache file:** `ai_google_dev_edge_litert_overview.html`
- **Source:** https://ai.google.dev/edge/litert/overview
- **Summary:** The deeper "overview" page for LiteRT covering the runtime architecture: model conversion, supported operator coverage, hardware delegate strategy (CPU/GPU/NPU), and integration points across platforms. Companion to the LiteRT landing page; the page developers land on after deciding LiteRT is the right tool and now need integration steps.
- **Key concepts:** LiteRT runtime, model conversion, .tflite interpreter, GPU delegate, NPU delegate, XNNPACK, Android NNAPI, Java/Kotlin bindings, C++ bindings, model deployment, cross-platform inference
- **Notable data:**
  - Same canonical taxonomy as the landing page (edge-computing, mobile-ml)
  - Cross-references the GPU acceleration and integration sub-pages
  - Targets the same multi-platform deployment story (Android first, iOS and embedded Linux secondary)
- **See also:** LiteRT landing page (this file); LiteRT: The Universal Framework for On-Device AI (this file)

### LiteRT: The Universal Framework for On-Device AI (Google Developers Blog)
- **Cache file:** `developers_googleblog_com_litert-the-universal-framework-for-on-device-ai.html`
- **Source:** https://developers.googleblog.com/litert-the-universal-framework-for-on-device-ai/
- **Summary:** Google Developers Blog announcement positioning LiteRT — the evolution of TFLite — as the universal production framework for on-device AI. Emphasises performance jumps in the latest release (1.4× faster cross-platform GPU, new NPU support) and streamlined GenAI deployment for open models like Gemma. Concrete C++ snippets illustrate the `CompiledModel` API with `kLiteRtHwAcceleratorGpu`. The marketing-tier source that justifies LiteRT as the recommended target.
- **Key concepts:** LiteRT, TFLite evolution, universal on-device runtime, cross-platform GPU acceleration, NPU acceleration, GenAI on device, Gemma 3n, MediaTek Dimensity 9500 NPU, CompiledModel API, AHardwareBuffer, AI Edge Gallery
- **Notable data:**
  - "1.4× faster cross-platform GPU performance" claimed vs. prior TFLite
  - Streamlined NPU acceleration positioned as the headline new capability
  - Demos cited: real-time on-device Chinese assistant powered by Gemma 3n 2B on Vivo 300 Pro with MediaTek Dimensity 9500 NPU; AI Edge Gallery (TinyGarden, Mobile Actions) built with FunctionGemma
  - Sample C++: `auto compiled_model = CompiledModel::Create(env, "mymodel.tflite", kLiteRtHwAcceleratorGpu);`
- **See also:** LiteRT landing page (this file); LiteRT Overview (this file); MobileNetV4 (`ml-research.md`)

### CameraX Configuration (Android Developers)
- **Cache file:** `developer_android_com_media_camera_camerax_configuration.html`
- **Source:** https://developer.android.com/media/camera/camerax/configuration
- **Summary:** Android's official configuration reference for CameraX, the Jetpack library that wraps Camera2 with a lifecycle-aware, use-case-oriented API (Preview, ImageAnalysis, ImageCapture, VideoCapture). Documents the CameraXConfig pattern, the ProcessCameraProvider singleton, camera selectors, and per-use-case configuration (target resolution, target rotation, executor, lens facing). The canonical reference the app's camera capture layer should be built from.
- **Key concepts:** CameraX, Jetpack camera, CameraXConfig, ProcessCameraProvider, Preview use case, ImageAnalysis, ImageCapture, lifecycle-aware camera, CameraSelector, lens facing, target resolution, target rotation, camera executor
- **Notable data:**
  - Companion docs in the same nav: About CameraX, Architecture, Configuration, Use cases, Codelab: Getting Started with CameraX
  - Page is part of the Android `media/camera/camerax/` doc tree under "Build AI experiences"
  - Last-mile reference for configuring the analysis pipeline that feeds frames into a LiteRT classifier
- **See also:** Compose Native CameraX in 2026 (this file); LiteRT Overview (this file)

### Compose Native CameraX in 2026: The Complete Guide (proandroiddev.com)
- **Cache file:** `proandroiddev_com_compose-native-camerax-in-2026-the-complete-guide-bf36c76a78e9.html`
- **Source:** https://proandroiddev.com/compose-native-camerax-in-2026-the-complete-guide-bf36c76a78e9
- **Summary:** Practitioner long-form covering CameraX integration into a pure Jetpack Compose app as of 2026: composable preview surfaces, permission handling, lifecycle management via `LifecycleCameraController`, swapping front/back lenses, and wiring an analyzer to push frames into ML inference. The bridge document between the official CameraX configuration reference and an idiomatic Compose UI.
- **Key concepts:** Jetpack Compose, CameraX, composable preview, LifecycleCameraController, permission flow, image analysis pipeline, lens switching, Compose state hoisting, 2026 best practices
- **Notable data:**
  - Article reflects the Compose-first guidance that became standard in the 2025–2026 Android toolchain
  - Pairs with the AndroidX `camera-view` Compose preview integrations
- **See also:** CameraX Configuration (this file); LiteRT: The Universal Framework for On-Device AI (this file)
