package com.foo.flickerimg

import com.google.gson.annotations.SerializedName


/**
 * { "stat":"fail", "code":112, "message":"Method \"unknown\" not found" }
 */
class FlickrError {
    @SerializedName("stat")
    var stat: String? = null

    @SerializedName("code")
    var code: Int = 0

    @SerializedName("message")
    var message: String? = null

}

class RecentPhotosResponse() {
    @SerializedName("photos")
    var photos: Photos? = null
}

open class Page {

    @SerializedName("page")
    var page: Int = 0

    @SerializedName("pages")
    var pages: Int = 0

    @SerializedName("perpage")
    var perpage: Int = 0

    @SerializedName("total")
    var total: Int = 0

    fun copy(): Page {
        return Page().also {
            it.page = this.page
            it.pages = this.pages
            it.perpage = this.perpage
            it.total = this.total
        }
    }
}

class Photos : Page() {
    @SerializedName("photo")
    var items: List<Photo>? = null
}


/**
 * {"id":"31011833857","owner":"150988304@N05",
 * "secret":"7167a1f5ba","server":"4860",
 * "farm":5,"title":"130","ispublic":1,
 * "isfriend":0,"isfamily":0}
 */
class Photo(@SerializedName("id")
            var id: String) {


    @SerializedName("owner")
    var owner: String? = null

    @SerializedName("secret")
    var secret: String? = null

    @SerializedName("originalsecret")
    var originalSecret: String? = null


    @SerializedName("server")
    var server: String? = null

    @SerializedName("farm")
    var farm: Int? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("ispublic")
    var isPublic: Int? = 0

    @SerializedName("isfriend")
    var isFriend: Int? = 0

    @SerializedName("isfamily")
    var isFamily: Int? = 0

    /**
     * https://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}.jpg
     * or
     * https://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}_[mstzb].jpg
     * or
     * https://farm{farm-id}.staticflickr.com/{server-id}/{id}_{o-secret}_o.(jpg|gif|png)
     */
    fun getUrl(size: Size? = null, fileExtension: String = "jpg", originalPreferred: Boolean = false): String? {
        farm ?: return null
        server ?: return null
        var url = StringBuilder("https://farm${farm}.staticflickr.com/${server}/${id}")

        if (originalPreferred) {
            originalSecret?.let {
                //o-secret part
                return url.append("_${it}_o.${fileExtension}").toString()
            }
        }


        secret?.let {
            //secret part
            url.append("_${it}")

            val sizeString = size?.value
            if (sizeString != null) {
                url.append("_${sizeString}")
            }
            return url.append(".${fileExtension}").toString()
        }

        return null
    }
}


/**
 *    s    small square 75x75
 *    q    large square 150x150
 *    t    thumbnail, 100 on longest side
 *    m    small, 240 on longest side
 *    n    small, 320 on longest side
 *    -    medium, 500 on longest side
 *    z    medium 640, 640 on longest side
 *    c    medium 800, 800 on longest side†
 *    b    large, 1024 on longest side*
 *    h    large 1600, 1600 on longest side†
 *    k    large 2048, 2048 on longest side†
 */
public enum class Size(val value: String) {
    smallSquare("s"),
    largeSquare("q"),
    thumbnail("t"),
    small240("m"),
    small320("n"),
    medium500("-"),
    medium640("z"),
    medium800("c"),
    large1024("b"),
    large1600("h"),
    large2048("k"),
}