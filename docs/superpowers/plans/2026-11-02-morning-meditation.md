# 아침 명상 Android 앱 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 출근 준비 전 휴대폰에서 타이머와 배경 소리를 골라 명상을 시작하고, 화면을 잠가도 세션을 이어갈 수 있는 개인용 Android 앱을 만든다.

**Architecture:** Kotlin 네이티브 단일 앱으로 구성한다. 순수 Kotlin 세션 모델이 타이머 상태를 관리하고, DataStore는 마지막 시간·소리 선택만 저장한다. Media3 세션 서비스가 소리 재생과 화면 잠금 중 세션을 담당하며, Jetpack Compose 화면은 시작 선택과 세션 조작을 제공한다.

**Tech Stack:** AGP 9.5.0, Gradle 9.6.0, JDK 17, Kotlin 내장 지원과 Compose Compiler 플러그인 2.4.10, Compose BOM 2026.09.00, Media3 1.11.1, Preferences DataStore 1.3.0, JUnit 4 및 Compose UI 테스트.

**Spec:** [`docs/superpowers/specs/2026-11-02-morning-meditation-design.md`](../specs/2026-11-02-morning-meditation-design.md)

## Global Constraints

- 지원 범위: Android 휴대폰, minSdk 26 이상을 권고하고 compileSdk/targetSdk는 37로 설정한다.
- 첫 실행 기본값은 5분·숲 소리다. 사용자가 고른 시간과 소리는 기기에 저장하고 세션 이력은 저장하지 않는다.
- 시간 선택은 1·5·10·15·20분으로 고정한다. 소리는 소리 없이·숲·파도·비·싱잉볼로 고정한다.
- 오디오 파일은 앱에 포함하고 네트워크 없이 재생한다. 출처와 재사용 라이선스를 `app/src/main/assets/audio-sources.md`에 기록한다.
- 소리를 재생할 때는 Media3 미디어 세션과 `mediaPlayback` 유형을 사용한다. 소리 없이 타이머만 계속될 때는 선언한 `specialUse` 유형을 사용한다. 세션 종료 시 오디오·서비스·알림을 정리한다.
- Android 13 이상에서도 앱 시작 때 알림 권한 대화상자를 띄우지 않는다. 세션 알림은 현재 시스템 권한 상태에 맞춰 표시한다.
- UI는 한국어, 어두운 숲빛 토큰, Android 기본 접근성 의미를 사용한다. 터치 영역은 최소 48dp, 조작부 간격은 최소 8dp, 일반 글자 대비는 4.5:1 이상이다.
- 구현 단계는 검증 후 기능별로 커밋한다. 작업 중 임시 산출물은 `.superpowers/`에 둔다.
- 현재 PC에는 Java, Gradle, Android SDK, Android Studio가 발견되지 않았다. 구현 전 JDK 17과 Android SDK 37/Build Tools 36을 준비해야 한다. AGP 9.5.0은 Gradle 9.6.0과 JDK 17을 요구한다.

## Review Focus

1. **화면 잠금 중 세션 지속:** 소리 재생/소리 없음 두 경우 모두 남은 시간이 줄고, 종료 시 서비스가 멈춘다. Task 3의 기기 검증.
2. **타이머 일시정지·재개:** 일시정지 동안 시간이 줄지 않고 재개 후 남은 시간이 이어진다. Task 2의 단위 테스트.
3. **시스템 오디오 중단:** 오디오 포커스를 잃으면 소리와 타이머가 함께 멈추고, 사용자가 직접 재개한다. Task 3의 서비스 테스트.
4. **알림 권한 거부:** Android 13 이상에서 권한을 거부해도 앱 시작/세션 시작이 막히지 않고, 화면 안 조작은 동작한다. Task 3의 API 33+ 테스트.
5. **선택 상태·큰 글자·회전:** 선택이 색상만으로 전달되지 않고, 큰 시스템 글자와 세로/가로 화면에서도 주요 조작이 보인다. Task 4의 Compose 테스트와 수동 확인.

---

## File Structure

- `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`, `gradle/wrapper/*`, `gradlew.bat`: Gradle 빌드와 고정된 플러그인·라이브러리 버전.
- `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`: 앱 모듈, SDK 범위, Compose, 미디어 서비스 권한·유형 선언.
- `app/src/main/java/com/example/morningmeditation/domain/`: 시간·소리 옵션, 세션 상태, 주입 가능한 시계 기반 타이머 규칙.
- `app/src/main/java/com/example/morningmeditation/data/SessionPreferences.kt`: DataStore로 마지막 시간·소리만 저장.
- `app/src/main/java/com/example/morningmeditation/playback/`: `MeditationSessionService`, Media3 플레이어/세션, 알림 동작, 소리 리소스 연결.
- `app/src/main/java/com/example/morningmeditation/playback/SessionStateStore.kt`: 앱 내부에 현재 세션 상태를 발행하는 `StateFlow` 저장소.
- `app/src/main/java/com/example/morningmeditation/playback/MeditationSessionController.kt`: ViewModel과 서비스 사이의 시작/일시정지/재개/소리/종료 인터페이스.
- `app/src/main/java/com/example/morningmeditation/ui/`: 시작 화면, 세션 화면, ViewModel, 화면 테마와 색상·간격 토큰.
- `app/src/main/res/raw/forest_loop.ogg`, `waves_loop.ogg`, `rain_loop.ogg`, `singing_bowl.ogg`: 오프라인 재생 음원.
- `app/src/main/assets/audio-sources.md`: 네 음원의 원본 URL, 라이선스, 필요한 출처 표시.
- `app/src/test/java/com/example/morningmeditation/domain/`: 타이머 상태 전이 단위 테스트.
- `app/src/androidTest/java/com/example/morningmeditation/`: Compose 접근성/화면 테스트, 서비스·백그라운드 통합 테스트.

## Task 1: Android 프로젝트와 빌드 검증 기반

**Files:**

- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `gradle.properties`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `gradle/wrapper/gradle-wrapper.jar`
- Create: `gradlew`
- Create: `gradlew.bat`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/morningmeditation/MainActivity.kt`
- Create: `app/src/main/java/com/example/morningmeditation/ui/theme/Theme.kt`
- Create: `app/src/androidTest/java/com/example/morningmeditation/AppLaunchTest.kt`

**Interfaces:**

- Produces: 패키지 `com.example.morningmeditation`, `MainActivity`, `MorningMeditationTheme`, Android 앱 모듈 `:app`.

- [ ] **Step 1: 도구 체계와 빈 Compose 프로젝트 골격을 준비한다**

  JDK 17, Android SDK Platform 37, Build Tools 36.0.0, 에뮬레이터/ADB를 확인하고 부족한 구성요소는 Android 공식 SDK 설치 경로에서 준비한다. Gradle Wrapper는 9.6.0, AGP는 9.5.0으로 고정한다. AGP 9의 내장 Kotlin 지원을 사용하고 `org.jetbrains.kotlin.android` 플러그인은 추가하지 않는다. Compose Compiler 플러그인은 2.4.10, Compose BOM은 2026.09.00으로 고정한다. `MainActivity`는 비어 있는 Compose 화면만 표시하도록 만든 뒤 `:app:assembleDebug`가 통과하는지 확인한다.

- [ ] **Step 2: 앱 시작 UI 테스트를 작성한다**

  `AppLaunchTest.launchesKoreanHomeScreen`을 작성해 Activity 실행 후 `아침 명상` 제목이 표시되는지 검사한다.

- [ ] **Step 3: 테스트가 실패하는지 확인한다**

  Run: `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.morningmeditation.AppLaunchTest`

  Expected: Activity는 실행되지만 `아침 명상` 제목 노드가 없어 assertion이 실패한다.

- [ ] **Step 4: 제목이 있는 초기 시작 화면을 구현한다**

  `MainActivity.onCreate`가 `MorningMeditationTheme` 아래 초기 시작 화면을 표시하게 한다. Compose는 `buildFeatures.compose = true`로 켠다. `compileSdk = 37`, `targetSdk = 37`, `minSdk = 26`, `namespace/applicationId = com.example.morningmeditation`으로 설정한다.

- [ ] **Step 5: 앱 실행 테스트와 디버그 빌드를 확인한다**

  Run: `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.morningmeditation.AppLaunchTest`

  Expected: 제목 노드가 표시되어 통과한다.

  Run: `./gradlew.bat :app:assembleDebug`

  Expected: `app/build/outputs/apk/debug/app-debug.apk` 생성.

## Task 2: 타이머 모델과 선택값 저장

**Files:**

- Create: `app/src/main/java/com/example/morningmeditation/domain/MeditationDuration.kt`
- Create: `app/src/main/java/com/example/morningmeditation/domain/Soundscape.kt`
- Create: `app/src/main/java/com/example/morningmeditation/domain/SessionState.kt`
- Create: `app/src/main/java/com/example/morningmeditation/domain/MeditationTimer.kt`
- Create: `app/src/main/java/com/example/morningmeditation/data/SessionPreferences.kt`
- Create: `app/src/test/java/com/example/morningmeditation/domain/MeditationTimerTest.kt`
- Create: `app/src/androidTest/java/com/example/morningmeditation/data/SessionPreferencesTest.kt`

**Interfaces:**

- Produces: `MeditationDuration`의 1/5/10/15/20분 값, `Soundscape`의 `NONE/FOREST/WAVES/RAIN/SINGING_BOWL`, `SessionState`의 `Idle/Running/Paused/Completed` 상태.
- Produces: `MonotonicClock.nowMillis(): Long`; `MeditationTimer.start(duration, soundscape)`, `pause()`, `resume()`, `setSoundEnabled(enabled)`, `finish()`, `stateAt(nowMillis)`.
- Produces: `SessionPreferences.observe(): Flow<SessionSelection>`, `save(selection: SessionSelection)`; 초기값은 5분과 숲 소리.

- [ ] **Step 1: 타이머 모델 테스트를 작성한다**

  `MeditationTimerTest`에 아래 테스트를 추가한다.

  - `startsWithSelectedDurationAndSound`: 5분 선택 후 시작하면 남은 시간은 300,000ms이고 소리는 숲이다.
  - `pauseFreezesRemainingTime`: 60초 경과 후 일시정지하고 30초 더 지나도 남은 시간이 240,000ms로 유지된다.
  - `resumeContinuesFromRemainingTime`: 일시정지 후 재개하면 남은 240,000ms부터 줄어든다.
  - `finishesAtZeroAndDoesNotGoNegative`: 1분 완료 시 상태는 `Completed`, 남은 시간은 0이다.
  - `soundCanBeMutedAndRestored`: 선택한 소리를 끄고 다시 켜도 선택한 사운드스케이프가 보존된다.

- [ ] **Step 2: 타이머 테스트가 실패하는지 확인한다**

  Run: `./gradlew.bat :app:testDebugUnitTest --tests com.example.morningmeditation.domain.MeditationTimerTest`

  Expected: 도메인 타입과 `MeditationTimer`가 없어 실패한다.

- [ ] **Step 3: 주입 가능한 단조 시계로 타이머 규칙을 구현한다**

  시간을 `SystemClock.elapsedRealtime()` 기반으로 계산하고 테스트에서는 가짜 시계를 쓴다. 일시정지·재개와 완료 상태를 구현한다. 알림 이력이나 세션 기록은 만들지 않는다.

- [ ] **Step 4: 타이머 단위 테스트를 통과시킨다**

  Run: `./gradlew.bat :app:testDebugUnitTest --tests com.example.morningmeditation.domain.MeditationTimerTest`

  Expected: 다섯 타이머 테스트 모두 통과한다.

- [ ] **Step 5: 선택값 저장 테스트를 먼저 작성하고 실패를 확인한다**

  `SessionPreferencesTest.remembersLastDurationAndSoundButStoresNoSessions`를 작성한다. 앱 데이터를 새로 만든 뒤 10분과 빗소리를 저장하고 저장소를 다시 열어 같은 두 선택값만 나오는지 검사한다.

  Run: `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.morningmeditation.data.SessionPreferencesTest`

  Expected: 테스트 컴파일 단계에서 `SessionPreferences`가 없어 실패한다.

- [ ] **Step 6: DataStore 저장소를 구현하고 검증한다**

  Preferences DataStore 1.3.0으로 마지막 선택값만 저장한다. 테스트를 재실행한다.

  Run: `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.morningmeditation.data.SessionPreferencesTest`

  Expected: 선택값 유지 테스트 통과, 세션 이력 키/저장소 없음.

## Task 3: 오디오 리소스와 화면 잠금 세션 서비스

**Files:**

- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/morningmeditation/playback/AudioCatalog.kt`
- Create: `app/src/main/java/com/example/morningmeditation/playback/SessionStateStore.kt`
- Create: `app/src/main/java/com/example/morningmeditation/playback/MeditationSessionController.kt`
- Create: `app/src/main/java/com/example/morningmeditation/playback/MeditationSessionService.kt`
- Create: `app/src/main/java/com/example/morningmeditation/playback/SessionNotification.kt`
- Create: `app/src/main/res/raw/forest_loop.ogg`
- Create: `app/src/main/res/raw/waves_loop.ogg`
- Create: `app/src/main/res/raw/rain_loop.ogg`
- Create: `app/src/main/res/raw/singing_bowl.ogg`
- Create: `app/src/main/assets/audio-sources.md`
- Create: `app/src/androidTest/java/com/example/morningmeditation/playback/MeditationSessionServiceTest.kt`

**Interfaces:**

- Consumes: `MeditationDuration`, `Soundscape`, `MeditationTimer`, `SessionState`.
- Produces: `AudioCatalog.resourceFor(soundscape): Int?`; `MeditationSessionController.state: StateFlow<SessionState>`와 `start(duration, soundscape)`, `pause()`, `resume()`, `setSoundEnabled(enabled)`, `finish()`; `MeditationSessionService`는 명령을 처리하고 `SessionStateStore`에 상태를 게시한다.

- [ ] **Step 1: 리소스 매핑과 서비스 동작 테스트를 작성한다**

  `MeditationSessionServiceTest`에 다음 테스트를 만든다.

  - `mapsEachSoundscapeToBundledAudio`: 네 소리는 서로 다른 로컬 리소스로 매핑되고 `NONE`은 리소스가 없다.
  - `loopsSelectedSoundUntilSessionEnds`: 선택한 소리가 반복 재생되고 종료 시 플레이어가 정지한다.
  - `timerCompletesAndStopsService`: 타이머가 끝나면 플레이어·서비스·알림이 정리된다.
  - `muteKeepsCountdownActive`: 소리 끄기 후에도 타이머는 계속된다.
  - `audioFocusLossPausesTimerAndRequiresManualResume`: 오디오 포커스를 잃으면 타이머와 소리가 멈추고, 사용자가 재개해야 다시 진행한다.
  - `notificationActionsPauseResumeAndFinish`: 알림 동작이 타이머 상태와 재생을 함께 바꾼다.
  - `deniedNotificationsDoNotBlockSessionStart`: Android 13 이상에서 알림 권한을 거부해도 세션 시작과 인앱 조작이 동작한다.

- [ ] **Step 2: 서비스 테스트가 실패하는지 확인한다**

  Run: `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.morningmeditation.playback.MeditationSessionServiceTest`

  Expected: 테스트 컴파일 단계에서 서비스와 소리 리소스가 없어 실패한다.

- [ ] **Step 3: 합법적으로 재사용 가능한 소리 파일을 선정한다**

  각 파일은 오프라인 재생이 가능하고 반복 연결부가 거슬리지 않도록 준비한다. 원본 URL, 라이선스 이름, 라이선스 URL, 필요한 표시 문구를 `audio-sources.md`에 기록한다. 출처/재사용 조건을 확인할 수 없는 파일은 포함하지 않는다.

- [ ] **Step 4: Media3 세션 서비스와 알림을 구현한다**

  Media3 1.11.1 `MediaSessionService`와 `ExoPlayer`를 사용한다. 재생 중 소리는 반복 재생하고, 완료/종료 시 짧게 페이드 아웃한 뒤 플레이어·서비스·알림을 정리한다. 소리 없이 타이머만 진행할 때는 Android 14+ `specialUse` 유형과 선언된 하위 용도를 사용하고, 소리 재생 때는 `mediaPlayback` 유형을 사용한다. Android 26–33에서는 호환되는 사용자 표시 알림을 제공한다. 서비스와 UI 사이에는 `StateFlow<SessionState>` 및 start/pause/resume/mute/finish 명령을 둔다.

- [ ] **Step 5: 서비스 테스트를 통과시킨다**

  Run: `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.morningmeditation.playback.MeditationSessionServiceTest`

  Expected: 로컬 소리 반복, 무음 상태 타이머 지속, 알림 권한 거부, 알림 조작, 완료 시 정리 테스트 통과.

## Task 4: 시작 화면, 명상 화면, ViewModel

**Files:**

- Create: `app/src/main/java/com/example/morningmeditation/ui/HomeScreen.kt`
- Create: `app/src/main/java/com/example/morningmeditation/ui/SessionScreen.kt`
- Create: `app/src/main/java/com/example/morningmeditation/ui/MeditationViewModel.kt`
- Create: `app/src/main/java/com/example/morningmeditation/ui/MeditationUiState.kt`
- Modify: `app/src/main/java/com/example/morningmeditation/MainActivity.kt`
- Create: `app/src/androidTest/java/com/example/morningmeditation/ui/HomeScreenTest.kt`
- Create: `app/src/androidTest/java/com/example/morningmeditation/ui/SessionScreenTest.kt`

**Interfaces:**

- Consumes: `SessionPreferences.observe/save`, session service의 상태 Flow 및 조작 명령.
- Produces: `HomeUiState(selectedDuration, selectedSoundscape)`; 선택 시간/소리와 명상 시작 이벤트를 제공한다.
- Produces: `SessionUiState(remainingSeconds, isPaused, soundscape, isSoundEnabled)`; 일시정지·재개, 소리 켜기/끄기, 종료 이벤트를 제공한다.

- [ ] **Step 1: 시작 화면 Compose 테스트를 작성한다**

  `HomeScreenTest`에 `showsFiveDurationsAndFiveSoundChoices`, `selectedChoicesExposeSelectedSemantics`, `touchTargetsMeetAndroidMinimum`, `startPassesSelectedDurationAndSound`, `defaultSelectionStartsWithinOneTap`을 작성한다. 기본 상태 5분·숲 소리, 대체 선택 15분·싱잉볼의 콜백 인자, 선택 컨트롤 48dp 이상을 확인한다.

- [ ] **Step 2: 테스트가 실패하는지 확인한다**

  Run: `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.morningmeditation.ui.HomeScreenTest`

  Expected: 테스트 컴파일 단계에서 `HomeScreen` 구현이 없어 실패한다.

- [ ] **Step 3: 시작 화면과 상태 연결을 구현한다**

  `HomeScreen`은 시간과 소리 선택 및 한 개의 주요 시작 버튼을 표시한다. 각 선택은 최소 48dp 높이와 8dp 간격을 갖고, 선택됨 상태를 텍스트/Compose semantics 모두에 반영한다. `MeditationViewModel`은 저장된 기본 선택값을 읽고 시작 탭에서 서비스를 시작한다.

- [ ] **Step 4: 시작 화면 테스트를 통과시킨다**

  Run: `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.morningmeditation.ui.HomeScreenTest`

  Expected: 다섯 시간·다섯 소리, 선택 semantics, 시작 인자 테스트 통과.

- [ ] **Step 5: 명상 중 화면 테스트를 먼저 작성한다**

  `SessionScreenTest`에 `showsRemainingTimeAndSelectedSound`, `pauseResumeLabelsTrackSessionState`, `muteCanBeReversed`, `finishReturnsToHomeWithoutHistory`를 작성한다.

- [ ] **Step 6: 명상 화면 테스트가 실패하는지 확인한다**

  Run: `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.morningmeditation.ui.SessionScreenTest`

  Expected: 테스트 컴파일 단계에서 `SessionScreen` 구현이 없어 실패한다.

- [ ] **Step 7: 명상 화면과 ViewModel 연결을 구현한다**

  화면 중앙에는 tabular 숫자로 남은 시간, 선택한 소리 상태를 두고 아래쪽에는 일시정지/재개, 소리 켜기/끄기, 명상 마치기를 둔다. 명상 완료 후 시작 화면으로 돌아가며 세션 기록은 표시하거나 저장하지 않는다. 어두운 숲빛 팔레트와 큰 시스템 글자에서도 재배치되는 Compose 레이아웃을 적용한다.

- [ ] **Step 8: 명상 화면 테스트를 통과시킨다**

  Run: `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.morningmeditation.ui.SessionScreenTest`

  Expected: 남은 시간·소리 상태·재생 조작·종료 화면 상태 테스트 통과.

## Task 5: 기기별 접근성·백그라운드 확인과 APK 전달

**Files:**

- Verify: `app/src/androidTest/java/com/example/morningmeditation/`의 기존 UI·서비스 테스트
- Verify: `app/build/outputs/apk/debug/app-debug.apk`

**Interfaces:**

- Consumes: 완성된 Compose 화면, `MeditationSessionService`, 앱 리소스.
- Produces: 재현 가능한 unit/UI/service 테스트, `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 1: 실제 화면에서 접근성과 레이아웃을 확인한다**

  Task 4에서 작성한 Compose 테스트로 선택 버튼의 이름/선택 상태와 48dp 최소 크기를 확인한다. 에뮬레이터에서 글자 크기를 200%로 높이고 세로/가로 회전 뒤 시간과 시작/종료 조작이 계속 보이는지 수동 확인한다.

- [ ] **Step 2: 잠금 화면/절전 수동 검증을 수행한다**

  Android 14 이상 에뮬레이터 또는 연결된 Android 휴대폰에서 아래 순서로 확인한다.

  1. 1분 + 숲 소리로 시작해 화면을 잠근다. 30초 후 돌아와 남은 시간이 계속 줄었고 소리가 재생 중인지 확인한다.
  2. 1분 + 소리 없이 시작해 화면을 잠근다. 30초 후 돌아와 남은 시간이 계속 줄었는지 확인한다.
  3. 잠금 화면 미디어 조작/알림에서 일시정지 후 재개, 종료를 실행한다.
  4. 전화 또는 오디오 포커스 인터럽트를 모의하고 타이머·소리가 함께 일시정지되는지 확인한다.
  5. Android 13+ 알림 권한을 거부한 상태에서도 시작 화면과 인앱 조작이 동작하는지 확인한다.

- [ ] **Step 3: 전체 검증을 실행한다**

  Run: `./gradlew.bat :app:testDebugUnitTest`

  Expected: 타이머 도메인 단위 테스트 전체 통과.

  Run: `./gradlew.bat :app:connectedDebugAndroidTest`

  Expected: 앱 시작, Compose 접근성, 서비스 및 화면 테스트 전체 통과. 에뮬레이터/기기가 없으면 테스트를 실행하지 못한 구체 사유와 수동 검증이 필요한 항목을 기록한다.

  Run: `./gradlew.bat :app:assembleDebug`

  Expected: 디버그 APK 생성.

- [ ] **Step 4: 개인 휴대폰 설치용 APK를 준비한다**

  Android 기기가 ADB에 연결되어 있으면 `adb install -r app/build/outputs/apk/debug/app-debug.apk`로 설치한다. ADB를 사용할 수 없으면 APK 경로와 설치 전제(기기에서 파일을 열어 설치)를 사용자에게 전달한다. 미등록 앱의 개발자 검증을 우회하는 목적으로 검증 설정을 바꾸지 않는다.

## 공식 Android 참고 자료

- [AGP 9.5.0 호환성 및 Gradle/JDK 요구사항](https://developer.android.com/build/releases/agp-9-5-0-release-notes)
- [Compose Compiler와 Compose BOM 설정](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler)
- [Media3 안정 버전](https://developer.android.com/jetpack/androidx/releases/media3)
- [Preferences DataStore 안정 버전](https://developer.android.com/jetpack/androidx/releases/datastore)
- [Media3 백그라운드 재생 서비스](https://developer.android.com/media/media3/session/background-playback)
- [Foreground service 유형 및 specialUse](https://developer.android.com/develop/background-work/services/fgs/service-types)
- [Android 개발자 검증과 ADB 설치](https://developer.android.com/developer-verification/guides/faq)

