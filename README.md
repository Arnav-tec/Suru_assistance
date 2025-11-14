Suru Assistant - Android project skeleton
========================================

What's included:
- Minimal Android Studio project skeleton with:
  - MainActivity (start/stop service)
  - AssistantService (foreground service, audio loop, TTS)
  - WakeWordEngine (placeholder - replace with Porcupine or other)
  - STTManager (Android SpeechRecognizer usage)
  - IntentHandler (basic command parsing)
  - Layout & manifest

Important notes:
1) This project is a starting point. The wake-word detection uses a CRUDE energy heuristic.
   For a real assistant you MUST integrate a proper wake-word engine:
     - Picovoice Porcupine (recommended): https://picovoice.ai
     - OR a custom Vosk model / neural wake detector

2) To add Porcupine:
   - Register at Picovoice console and create a keyword "suru" or "hey suru".
   - Download the .ppn keyword file and native libraries (.so).
   - Place .ppn in app/src/main/assets/ and .so in app/src/main/jniLibs/<abi>/
   - Replace WakeWordEngine.feed() logic with Porcupine processing.

3) STT:
   - Currently uses Android SpeechRecognizer. It may use Google's online STT or on-device depending on device.
   - For offline STT, consider integrating Vosk (adds model files of tens-hundreds MB).

4) Build & run:
   - Open this project in Android Studio Arctic Fox or newer.
   - Let Gradle sync.
   - Grant RECORD_AUDIO & other permissions on app launch.
   - Start the assistant via UI.

5) Testing wake-word:
   - Because current detector is just energy-based, noises may trigger it. Integrate Porcupine for production.

If you want, I will:
- Add exact Porcupine init code and a small script to copy .ppn from assets and load the library.
- Integrate Vosk offline STT example (will require large model; I can provide steps).
- Prepare a ready-to-build GitHub repo with further README details.

Next step I will do automatically (unless you say otherwise):
- Add Porcupine init snippet and instructions inside WakeWordEngine.kt (code comments).
- Provide step-by-step to generate keyword and add to the project.

Reply "build porcupine" if you want me to inject the Porcupine init code and update README with exact commands.


## Added features by assistant:
- Porcupine keyword loader snippet added to WakeWordEngine.kt (still requires SDK/native libs).
- BootReceiver to auto-start AssistantService on device boot.
- OnlineAssistant (OkHttp) to call OpenAI Chat Completions; configure API key in app preferences or via gradle.
- Build.gradle updated to include OkHttp dependency.

### Next steps to finalize build:
1. Add Porcupine Android native .so files to app/src/main/jniLibs/<abi>/
2. Add Porcupine SDK .jar or maven dependency as per Picovoice docs.
3. (Optional) Store OpenAI API key securely; for quick testing, you can set it in app preferences under key `openai_key`.


## GitHub Actions APK build (added)
I have added `.github/workflows/android-build.yml` which builds `assembleDebug` and `assembleRelease` on push to `main`.

### How to get APK (no Android Studio required)
1. Create a GitHub repo and push the project files (upload all files from this ZIP).
2. In the repository, go to Settings → Secrets and variables → Actions → New repository secret:
   - If you want signed release builds, add:
     - `ANDROID_KEYSTORE` : base64 contents of your keystore.jks
     - `KEYSTORE_PASSWORD`
     - `KEY_ALIAS`
     - `KEY_PASSWORD`
   - For debug builds no secrets needed.
3. Push to `main` branch (or use "Actions → Run workflow" button).
4. Open Actions → select workflow run → Artifacts → download the APK.

Note: The workflow tries to use Gradle wrapper (`./gradlew`) if present; otherwise it will attempt `gradle` system command. If Gradle wrapper is preferred, add wrapper files (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`) to repo.

