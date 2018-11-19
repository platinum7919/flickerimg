package com.foo.flickerimg

import android.content.Context
import com.foo.common.api.BaseApi
import com.foo.common.api.ServerException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Response
import org.json.JSONObject
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.nio.charset.Charset

/**
 * Singleton to describe of Flickr API
 */
object Flickr : BaseApi<FlickrException>() {

    val expectedResponseFormat = "json"

    val ctx: Context
        get() {
            return getContext()
        }

    @Volatile
    private var endpointsImpl: FlickrEndpoints? = null

    val endpoints: FlickrEndpoints
        @Synchronized
        get() {
            return endpointsImpl ?: (createEndpoints().apply {
                endpointsImpl = this
            })
        }

    init {

    }

    /**
     * Get the recent photos
     */
    fun getRecentPhotos(): Single<RecentPhotosResponse> {
        return async(endpoints.getRecentPhotos())
    }

    //region parent class implementation
    override val gson: Gson by lazy {
        GsonBuilder().apply {
            setPrettyPrinting()
        }.create()
    }

    override val userAgent: String
        get() = "some user agent here"


    override fun checkResponseType(response: Response) {
        if (response.header("Content-Type")?.contains("json", ignoreCase = true) == true) {
            // good it is json
        } else {
            throw InvalidFormatException("Invalid Format: Status ${response.code()}")
        }
    }

    override fun createServerException(response: Response): FlickrException {
        return FlickrException(response)
    }

    //endregion

    //region base methods
    @Synchronized
    fun invalidate() {
        endpointsImpl = null
    }


    //https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=fee10de350d1f31d5fec0eaf330d2dba&format=json&nojsoncallback=true
    private fun createEndpoints(): FlickrEndpoints {
        return createEndpoints(ctx,
                "https://api.flickr.com",
                FlickrEndpoints::class,
                logHttpCalls = { true })
    }


    internal fun <R : Any> async(
            single: Single<R>): Single<R> {
        return apiWrapper(single)

    }

    /**
     * Tell the [Single] to run on worker thread and observe on UI Thread
     */
    internal fun <R : Any> apiWrapper(single: Single<R>): Single<R> {
        return single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }


    /**
     * called by [BaseApi.ErrorConverter] to handle more restful like error
     *
     * Has to override default behavior because ...
     */
    override fun checkServerError(response: Response) {
        checkResponseType(response)
        if (!response.isSuccessful) {
            // not 2xx
            throw createServerException(response)
        } else {
            // 2xx - suppose to be successful but...
            //
            // this is a bit of a hack since the Flickr is not really a restful API
            // e.g.
            // For the wrong method param (.../services/rest/?method=flickr.photos.getRecent2)
            //  code : 200
            //  body : {"stat":"fail","code":112,"message":"Method \"flickr.photos.getRecent2\" not found"}
            // For the wrong url (.../services/rest2/?method=flickr.photos.getRecent)
            //  code : 404
            //  body : <!DOCTYPE HTML PUBLIC "-//W3C/ ....
            //
            // So, we can't actually determine if the request was successful by just the status code alone
            // (i.e. is the success body missing mandatory param or is it actually an error body??)
            response.body()?.source()?.let { source ->
                // read the whole thing, but this should be ok since the ResponseBody thing is cached internally
                // by the response, so the converter should not read the response stream twice
                source.request(Long.MAX_VALUE)
                val responseString = source.buffer().clone().readString(Charset.forName("UTF-8"))
                responseString ?: return
                val responseObject = try {
                    JSONObject(responseString)
                } catch (t: Throwable) {
                    throw InvalidFormatException(t.message ?: "???")
                }
                val isErrorBody = responseObject.run {
                    has("stat") && has("code")
                }
                if (isErrorBody) {
                    throw FlickrException(response.code(), responseString)
                }
                // all good, looks like the 2xx response code is not a lie... lol
            }
        }
    }

    //endregion

}

/**
 * Definition of the Flickr endpoints
 */
interface FlickrEndpoints {

    @GET("/services/rest/")
    fun getRecentPhotos(
            @Query("method") method: String = "flickr.photos.getRecent",
            @Query("api_key") apiKey: String = SecureStore.getFlickrApiKey(),
            @Query("format") format: String = Flickr.expectedResponseFormat,
            @Query("nojsoncallback") nojsoncallback: Boolean = true): Single<RecentPhotosResponse>

}

/**
 * Exception class to describe the error of Flickr API
 */
class FlickrException : ServerException {
    val TAG = this::class.simpleName

    var error: FlickrError? = null

    constructor(response: Response) : super(response) {

    }

    /**
     * if error message needs to be more complicated to uniquely identify,
     * we can use the request too
     */
    constructor(statuCode: Int, responseBody: String) : super(statuCode, responseBody) {
        responseBody?.castToObject<FlickrError>()?.let {
            error = it
            customMessage = it.message
        }
    }
}

/**
 * Exception when server return something like an HTML
 */
class InvalidFormatException(message: String = "Invalid format") : Exception(message) {

}