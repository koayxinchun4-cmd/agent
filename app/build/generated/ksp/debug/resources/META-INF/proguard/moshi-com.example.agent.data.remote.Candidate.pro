-keepnames class com.example.agent.data.remote.Candidate
-if class com.example.agent.data.remote.Candidate
-keep class com.example.agent.data.remote.CandidateJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
