package com.example.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GitHubApiService {
    @GET("user")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<GitHubUserProfile>

    @GET("user/repos")
    suspend fun getUserRepos(
        @Header("Authorization") token: String,
        @Query("type") type: String = "owner",
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 50
    ): Response<List<GitHubRepoResponse>>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepo(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") token: String? = null
    ): Response<GitHubRepoResponse>

    @GET("repos/{owner}/{repo}/issues")
    suspend fun getIssues(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("state") state: String = "open",
        @Query("per_page") perPage: Int = 10,
        @Header("Authorization") token: String? = null
    ): Response<List<GitHubIssue>>

    @POST("repos/{owner}/{repo}/issues")
    suspend fun createIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body request: GitHubCreateIssueRequest,
        @Header("Authorization") token: String
    ): Response<GitHubIssue>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFileContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Query("ref") ref: String? = null,
        @Header("Authorization") token: String? = null
    ): Response<GitHubFileContentResponse>

    @GET
    suspend fun getRawContent(
        @retrofit2.http.Url url: String,
        @Header("Authorization") token: String? = null
    ): Response<okhttp3.ResponseBody>

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun createOrUpdateFile(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Body request: GitHubCreateOrUpdateFileRequest,
        @Header("Authorization") token: String
    ): Response<GitHubCommitFileResponse>

    @POST("repos/{owner}/{repo}/pulls")
    suspend fun createPullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body request: GitHubCreatePrRequest,
        @Header("Authorization") token: String
    ): Response<GitHubPullRequest>
}

interface GitHubOAuthService {
    @FormUrlEncoded
    @POST("login/device/code")
    @Headers("Accept: application/json")
    suspend fun requestDeviceCode(
        @Field("client_id") clientId: String,
        @Field("scope") scope: String = "repo,workflow,read:user,user:email"
    ): Response<GitHubDeviceCodeResponse>

    @FormUrlEncoded
    @POST("login/oauth/access_token")
    @Headers("Accept: application/json")
    suspend fun pollDeviceToken(
        @Field("client_id") clientId: String,
        @Field("device_code") deviceCode: String,
        @Field("grant_type") grantType: String = "urn:ietf:params:oauth:grant-type:device_code"
    ): Response<GitHubOAuthTokenResponse>

    @FormUrlEncoded
    @POST("login/oauth/access_token")
    @Headers("Accept: application/json")
    suspend fun exchangeWebCode(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String? = null
    ): Response<GitHubOAuthTokenResponse>
}

object GitHubApiClient {
    private const val API_BASE_URL = "https://api.github.com/"
    private const val GITHUB_BASE_URL = "https://github.com/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Nexus-Android-Agent")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val service: GitHubApiService by lazy {
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GitHubApiService::class.java)
    }

    val oauthService: GitHubOAuthService by lazy {
        Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GitHubOAuthService::class.java)
    }
}
