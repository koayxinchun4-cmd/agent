-keepnames class com.example.agent.data.remote.Part
-if class com.example.agent.data.remote.Part
-keep class com.example.agent.data.remote.PartJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
