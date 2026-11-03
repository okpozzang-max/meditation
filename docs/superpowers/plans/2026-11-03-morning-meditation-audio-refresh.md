# Morning Meditation Audio Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the four bundled ambience files with the user's downloaded recordings, encoded as stereo MP3 at 192 kbps, with any source longer than five minutes truncated to five minutes and looped by the existing player.

**Architecture:** Keep audio playback behavior in the existing Media3 foreground service. Replace only the bundled raw resources and update their source/license record; ExoPlayer's current repeat-one mode will keep each selected track playing until session end.

**Tech Stack:** Android/Kotlin, Media3 ExoPlayer, FFmpeg/ffprobe for one-time WAV preparation and metadata validation.

**Spec:** [`docs/superpowers/specs/2026-11-03-morning-meditation-audio-refresh-design.md`](../specs/2026-11-03-morning-meditation-audio-refresh-design.md)

## Global Constraints

- Do not alter or remove user originals in `음원/`.
- Encode all four replacement files as stereo MP3 at 192 kbps.
- Keep files at or below 300 seconds; retain the full source duration for files shorter than five minutes.
- Preserve the audio catalog's resource names and session repeat behavior.
- Record Freesound source URL, author, CC0 license, and encoding/trimming details.

## Review Focus

1. **Duration cap:** the wave asset must be no longer than 300 seconds; other clips retain their source duration.
2. **Encoding:** every packaged clip is MP3, stereo, and 192 kbps.
3. **Looping:** existing repeat-one playback remains in place until session end.
4. **Source integrity:** original downloads in `음원/` are unchanged.
5. **License record:** app source metadata identifies the exact file, author, source, license, and processing.

---

## File Structure

- `음원/*.wav`: user-provided source files; read only.
- `app/src/main/res/raw/forest_loop.mp3`: replacement forest ambience.
- `app/src/main/res/raw/waves_loop.mp3`: five-minute maximum wave ambience; replaces `waves_loop.flac`.
- `app/src/main/res/raw/rain_loop.mp3`: replacement gentle rain.
- `app/src/main/res/raw/singing_bowl.mp3`: replacement singing bowl.
- `app/src/main/assets/audio-sources.md`: current source and processing records.
- `app/src/main/java/com/example/morningmeditation/playback/MeditationSessionService.kt`: no change expected; repeat-one behavior is already implemented.

### Task 1: Prepare and replace bundled soundscape recordings

**Files:**
- Replace: `app/src/main/res/raw/forest_loop.mp3`
- Replace: `app/src/main/res/raw/rain_loop.mp3`
- Replace: `app/src/main/res/raw/singing_bowl.mp3`
- Replace: `app/src/main/res/raw/waves_loop.flac` with `app/src/main/res/raw/waves_loop.mp3`
- Modify: `app/src/main/assets/audio-sources.md`

**Interfaces:**
- Consumes the four WAVs in the approved design's input table.
- Produces the same `R.raw.*` soundscape mapping already used by `AudioCatalog` (the wave resource extension changes to MP3).

- [x] Confirm `ffmpeg` and `ffprobe` are available; use a local converter only for these user-provided files.
- [x] Encode each WAV to stereo MP3 at 192 kbps. Preserve source sample rates except resample the 22.05kHz bowl source to 44.1kHz so the MPEG-1 MP3 encoder can produce 192 kbps; this does not add source detail. Keep clips under 300 seconds and encode the wave track to 299.8 seconds to account for MP3 frame padding.
- [x] Replace the four app resource files and update source/license metadata, noting that the wave track uses the first 300 seconds.
- [x] Verify all four output files with `ffprobe`: MP3 codec, stereo channels, 192 kbps, expected duration, and wave duration at most 300 seconds. Confirm the source WAV file hashes are unchanged.
- [x] Run `:app:assembleDebug` and confirm the resulting APK includes the replacement raw resources.

**Expected output lengths:** forest approximately 177.83 seconds; waves approximately 299.8 seconds; rain approximately 215.82 seconds; singing bowl 60 seconds. MP3 durations can differ by a small encoder delay/padding amount.

**Ruling:** The bowl's MPEG-2 MP3 encode at 22.05kHz tops out at 160 kbps. Resample this source to 44.1kHz before encoding at the approved 192 kbps so the output format matches the design; this does not increase the source's acoustic bandwidth.
