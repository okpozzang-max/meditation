# Audio sources and licenses

These bundled tracks are available offline. Each downloaded source is marked CC0 on its Freesound page. The original WAV downloads are kept in the local `음원/` folder; only the processed MP3 files are bundled in the app.

| App sound | Bundled file | Source recording | Author | Source | License | Processing |
| --- | --- | --- | --- | --- | --- | --- |
| Forest | `res/raw/forest_loop.mp3` | Forest Morning #1 | timothyd4y | [Freesound 607192](https://freesound.org/people/timothyd4y/sounds/607192/) | CC0 | Stereo MP3, 192 kbps, 48 kHz; full 2:57.83 recording |
| Waves | `res/raw/waves_loop.mp3` | Gentle Ocean Waves Mix (2018) | esh9419 | [Freesound 417797](https://freesound.org/people/esh9419/sounds/417797/) | CC0 | Stereo MP3, 192 kbps, 48 kHz; first 299.8 seconds of 11:59.77 source |
| Rain | `res/raw/rain_loop.mp3` | Light rain | jankooiker | [Freesound 237935](https://freesound.org/people/jankooiker/sounds/237935/) | CC0 | Stereo MP3, 192 kbps, 44.1 kHz; full 3:35.82 recording |
| Singing bowl | `res/raw/singing_bowl.mp3` | SingingBowl.wav | Monkay | [Freesound 48325](https://freesound.org/people/Monkay/sounds/48325/) | CC0 | Stereo MP3, 192 kbps, 44.1 kHz; full 1-minute source resampled from 22.05 kHz |

The MP3 transcodes were prepared with FFmpeg 9.0.2. The singing bowl source is 22.05 kHz; it was resampled to 44.1 kHz because the MPEG-2 MP3 encoder path capped its output at 160 kbps. Resampling does not add information beyond the original recording's bandwidth. All four soundscapes continue to use the app's existing repeat-one playback until the meditation session ends.
