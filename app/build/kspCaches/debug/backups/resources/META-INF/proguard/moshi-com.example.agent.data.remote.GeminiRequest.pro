-keepnames class com.example.agent.data.remote.GeminiRequest
-if class com.example.agent.data.remote.GeminiRequest
-keep class com.example.agent.data.remote.GeminiRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
