package com.foo.flickerimg

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.squareup.picasso.Picasso


internal val EXTRA_PHOTO_JSON = "EXTRA_PHOTO_JSON"

class DetailActivity : BaseActivity() {


    @BindView(R.id.imageview_photo)
    lateinit var photoImage: ImageView


    val photo: Photo?
        get() {
            return intent?.getStringExtra(EXTRA_PHOTO_JSON)?.castToObject<Photo>()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        ButterKnife.bind(this, this)

        val thisPhoto = photo
        thisPhoto ?: run {
            showToast("Must pass in a photo object")
            return
        }


        Picasso.get()
                .load(thisPhoto.getUrl(Size.medium640))
                .fit()
                .centerInside()
                .placeholder(R.drawable.ic_image_grey600_24dp)
                .error(R.drawable.ic_alert_circle_outline_white_24dp)
                .into(photoImage)
    }


    override fun finish() {
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }
}