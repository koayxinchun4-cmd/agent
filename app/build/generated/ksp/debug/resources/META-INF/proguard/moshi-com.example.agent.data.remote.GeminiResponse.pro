-keepnames class com.example.agent.data.remote.GeminiResponse
-if class com.example.agent.data.remote.GeminiResponse
-keep class com.example.agent.data.remote.GeminiResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
