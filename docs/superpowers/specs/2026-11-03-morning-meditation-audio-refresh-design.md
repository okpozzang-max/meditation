# 아침 명상 앱 음원 교체 설계

상태: 사용자 승인 완료

## 의도

사용자가 내려받아 `음원/` 폴더에 둔 자연 소리와 싱잉볼 녹음으로 기존 음원을 교체한다. 앱 용량은 줄이고, 파도처럼 긴 음원은 5분까지만 포함해 세션 중 반복 재생한다.

## 목표와 범위

- 숲, 파도, 비, 싱잉볼의 기존 앱 음원을 폴더의 대응 WAV 파일로 교체한다.
- 네 음원 모두 stereo MP3 192 kbps로 변환한다. 원본 WAV는 사용자가 보관한 폴더에서 변경하거나 삭제하지 않는다. 싱잉볼 원본은 22.05kHz여서 MP3 192 kbps를 위해 44.1kHz로 리샘플링한다. 이는 원본에 없는 고주파 정보를 추가하지 않는다.
- 원본이 5분을 넘으면 앞의 5분만 앱 리소스에 포함한다. 5분 이하 파일은 전체 길이를 유지한다.
- 앱의 기존 ExoPlayer 반복 재생 동작을 유지해 선택한 소리가 명상 세션이 끝날 때까지 반복되게 한다.
- 음원 출처, 작성자, Freesound URL, CC0 라이선스와 처리 사실을 `app/src/main/assets/audio-sources.md`에 기록한다.
- 기존 파일명과 리소스 매핑을 유지해 불필요한 제품 코드 변경을 피한다.

## 목표에서 제외

- UI, 타이머, 서비스 동작의 변경
- 음량 조절이나 소리 미리듣기 기능
- 음원을 직접 녹음하거나 합성하는 일

## 입력 파일

| 사운드 | 원본 파일 | 길이 | 크기 |
|---|---|---:|---:|
| 숲 | `음원/607192__timothyd4y__forest-morning-1.wav` | 2:57.83 | 49.1 MiB |
| 파도 | `음원/417797__esh9419__gentle-ocean-waves-mix-2018.wav` | 11:59.77 | 131.8 MiB |
| 비 | `음원/237935__jankooiker__light-rain.wav` | 3:35.82 | 54.5 MiB |
| 싱잉볼 | `음원/48325__monkay__singingbowl.wav` | 1:00 | 5.0 MiB |

## 음원 형식 접근 방식

1. **MP3 192 kbps 변환 — 선택.** 음원 용량을 크게 줄이며 안드로이드의 기존 Media3 재생 경로를 유지한다. 손실 압축이지만 192 kbps stereo는 자연 배경음과 싱잉볼의 개인용 재생에 적절한 균형이다.
2. **WAV 유지 및 파도만 자르기.** 무손실 원본을 그대로 보존하지만 앱에 음원만 약 166 MiB가 들어간다. 사용자가 언급한 용량 문제를 충분히 줄이지 못한다.
3. **FLAC 변환 및 파도 자르기.** 무손실이지만 녹음별 압축률에 따라 결과 용량이 크고, MP3보다 저장 공간 절감이 불확실하다.

사용자는 MP3 192 kbps 접근을 선택했다. 모든 변환본을 합친 예상 크기는 약 17 MiB다. 싱잉볼 원본의 22.05kHz sample rate에서는 MPEG-2 Layer III 인코더가 192 kbps를 출력하지 않고 160 kbps로 제한하는 것을 변환 중 확인했다. 합의한 192 kbps 출력을 유지하기 위해 싱잉볼만 44.1kHz로 리샘플링한다. 이 업샘플링은 원본에 없는 음향 정보를 복원하지 않는다.

## 변경 파일과 검증

- 교체: `app/src/main/res/raw/forest_loop.mp3`, `waves_loop.mp3`, `rain_loop.mp3`, `singing_bowl.mp3`
- 갱신: `app/src/main/assets/audio-sources.md`
- 예상하지 않는 변경: Kotlin 제품 코드와 UI
- 변환 후 각 리소스의 MP3 형식, 재생 길이(5분 이하), 192 kbps, 스테레오 여부와 총 크기를 메타데이터로 확인한다. 파도 리소스가 300초를 넘지 않는지 확인하고 디버그 APK 빌드로 Android 리소스 패키징을 확인한다.

## 출처 및 라이선스

- 숲: timothyd4y, Forest Morning #1 — https://freesound.org/people/timothyd4y/sounds/607192/ — CC0
- 파도: esh9419, Gentle Ocean Waves Mix (2018) — https://freesound.org/people/esh9419/sounds/417797/ — CC0
- 비: jankooiker, Light rain — https://freesound.org/people/jankooiker/sounds/237935/ — CC0
- 싱잉볼: Monkay, SingingBowl.wav — https://freesound.org/people/Monkay/sounds/48325/ — CC0

## 중요한 가정

- 사용자가 지정한 WAV 파일은 각각 위 표의 소리 선택지와 대응한다.
- 5분 초과 파일은 첫 5분을 사용한다. 재생은 앱의 기존 파일 단위 반복 동작이 담당한다.
- 처리된 파일만 앱 리소스로 교체하고 내려받은 원본은 그대로 둔다.
- 계획상 ruling: 22.05kHz 싱잉볼을 44.1kHz로 리샘플링한 뒤 MP3 192kbps로 인코딩한다 — 저 sample rate에서 MP3 192kbps가 160kbps로 제한되기 때문 — 출력 설정은 맞추지만 음질 개선은 기대하지 않는다.
