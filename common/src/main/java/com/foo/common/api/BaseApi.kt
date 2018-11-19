package com.foo.common.api

import android.content.Context
import android.util.Log
import com.foo.common.utils.firstNotNull
import com.google.gson.Gson
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass


/**
 * A base class for Using Retrofit with okhttp with Reactive-X adapter and gson converter
 */
abstract class BaseApi<SE : ServerException> {
    val TAG = this::class.simpleName

    companion object {
        val HEADER_NO_CACHE = "Cache-Control: no-cache"
    }


    abstract val gson: Gson
    /**
     *
     */
    abstract val userAgent: String


    /**
     * The current http client that is being use for the endpoint
     */
    var httpClient: OkHttpClient? = null

    /**
     * The current retrofit object that is being use for the endpoint
     */
    var retrofit: Retrofit? = null

    var httpClientBuilder: OkHttpClient.Builder? = null

    /**
     * Retrofit builder
     */
    fun <K : Any> createEndpoints(ctx: Context,
                                  baseUrl: String,
                                  endpointsClass: KClass<K>,
                                  connectTimeoutSec: Long = 10,
                                  readTimeoutSec: Long = 10,
                                  writeTimeoutSec: Long = 10,
                                  logHttpCalls: () -> Boolean = { false },
            // add TokenAuthenticator (to the Builder) here
                                  prebuild: (OkHttpClient.Builder) -> OkHttpClient.Builder = { it }): K {
        val SIZE_OF_CACHE = (10 * 1024 * 1024).toLong() // 10 MiB
        val cache = Cache(File(ctx.getCacheDir(), "http"), SIZE_OF_CACHE)

        val builder = OkHttpClient().newBuilder()
                .connectTimeout(connectTimeoutSec, TimeUnit.SECONDS)
                .writeTimeout(readTimeoutSec, TimeUnit.SECONDS)
                .readTimeout(writeTimeoutSec, TimeUnit.SECONDS)
                .cache(cache)
                .addInterceptor(ErrorConverter())
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (logHttpCalls.invoke()) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                }).apply {
                    httpClientBuilder = this
                }

        return Retrofit.Builder()
                .client(prebuild.invoke(builder).build().also { httpClient = it })

                // reactiveX
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())


                // gson plugin
                .addConverterFactory(GsonConverterFactory.create(gson))

                // base url
                .baseUrl(baseUrl)
                .build()
                .also { retrofit = it }
                .create(endpointsClass.java)
    }

    /**
     * Override this to define exactly what is an error (anything except internet connectivity error)
     */
    @Throws(ServerException::class)
    open fun checkServerError(response: Response) {
        if (!response.isSuccessful) {
            checkResponseType(response)
            throw this@BaseApi.createServerException(response)
        }
    }

    inner class ErrorConverter : Interceptor {
        override fun intercept(chain: Interceptor.Chain?): Response {

            chain ?: kotlin.run {
                throw IllegalArgumentException("Null chain")
            }
            val response = chain.proceed(chain.request())
            checkServerError(response)
            return response
        }
    }


    /**
     * Throw exception here if isn't the correct content-type
     */
    @Throws(Exception::class)
    abstract fun checkResponseType(response: Response)


    /**
     * Provide either parameter
     * @param response
     * The raw response from okHttp
     *
     * @param message
     * the string message for the exception
     */
    abstract fun createServerException(response: Response): SE

}

open class ServerException : Exception {

    var customMessage: String? = null
    var responseBody: String? = null
    var statusCode: Int? = null

    constructor(statusCode: Int, responseBody: String?) {
        this.statusCode = statusCode
        this.responseBody = responseBody
    }

    /**
     *
     */
    constructor(rawResponse: Response) : this(rawResponse.code(), rawResponse.body()?.string()) {

    }


    override val message: String?
        get() {
            return listOf(customMessage, super.message).firstNotNull()
        }
}