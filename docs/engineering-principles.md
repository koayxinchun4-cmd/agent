# Engineering Principles

## JVM vs Android Runtime Testing

Nexus separates pure application logic from Android-specific behavior in tests.

- **Pure logic must be tested on the JVM.** Keep domain models, parsers, classifiers, planners, validation rules, and other Android-independent logic free of Android runtime dependencies so they can run as fast JVM unit tests.
- **Android-specific behavior must be validated in Android Runtime.** Code that constructs, resolves, or executes Android framework behavior should be covered by Android/runtime-aware tests rather than relying on JVM-only tests.
- **JVM tests must not directly depend on Android APIs that require Android Runtime.** When an Android operation has a meaningful contract, extract a small pure Kotlin representation/specification and test that contract on the JVM; validate the actual Android API interaction separately at runtime.
- **Keep the boundary explicit.** A useful pattern is:

```text
Pure Kotlin contract/spec
        ↓
JVM unit test
        ↓
Android-specific implementation
        ↓
Android Runtime validation
```

This principle applies to future Agent Tools and execution boundaries, including App Agent, File Agent, permissions, intents, and other Android integrations.
