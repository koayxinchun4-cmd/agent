-keepnames class com.example.agent.data.remote.Content
-if class com.example.agent.data.remote.Content
-keep class com.example.agent.data.remote.ContentJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
