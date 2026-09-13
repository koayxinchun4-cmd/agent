# Android CI Fast Path

The Android CI gate runs unit tests, lint, Debug APK assembly, and the APK existence check in one Gradle job. The Gradle invocation no longer starts with `clean`, and the separate APK download/check job has been removed to avoid an additional runner queue and artifact transfer.

Gradle wrapper validation remains a parallel job.