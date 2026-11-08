# M-Print VoxCPM2 Android prototype

Development branch: `feature/voxcpm2-export-history-speed`.

Adds Android generated WAV history, export to `Downloads/MPrint`, Android sharesheet sharing, and playback-only speed control (0.60x–1.20x). **Saved WAV remains at original speed. MP3 export is not implemented.** No edits to M-Print backend or production.

## Safety and limitations
- Separate app identity: `com.kglaynyi.mprint.voxcpm` so it installs alongside upstream; will need its own GGUF downloads.
- Cannot access original app's private WAV recordings or existing downloaded model files.
- Audio history stored in this app's private `filesDir/tts_output`; removing app data deletes it.
- The first APK build may need changes for the repository's native toolchain; install on a test device only after the GitHub Actions build succeeds.
- Do not merge until compilation and device tests pass. No ElevenLabs credits used.

## Download
Actions > M-Print VoxCPM2 Android Debug APK > latest successful run > artifact `mprint-voxcpm2-android-debug`. Extract the ZIP and install its APK.

## Smoke tests
1. Download models in the *new* app, synthesize a short Burmese sample.
2. Listen at 0.80x; confirm slower playback but unchanged WAV content.
3. Save WAV; confirm file appears in Android Downloads/MPrint and plays in an external player.
4. Share WAV to another app; confirm MIME/audio contents.
5. Restart app and check history. Delete entry and verify other entries remain.
6. Confirm installed upstream app remains unaffected.
