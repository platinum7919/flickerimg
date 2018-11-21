package com.foo.flickerimg

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.foo.common.ui.RecyclerViewAdapter
import com.foo.common.ui.RecyclerViewHolder
import com.foo.common.ui.getAdapterOfType
import com.foo.common.ui.setup
import com.foo.flickerimg.views.QuickReturnLayout
import com.foo.flickerimg.views.StateLayout
import com.foo.flickerimg.views.setOptionalText
import com.squareup.picasso.Picasso

class MainActivity : BaseActivity() {
    val REQUEST_PHOTO_DETAIL = 1

    @BindView(R.id.statelayout)
    lateinit var stateLayout: StateLayout

    @BindView(R.id.quickreturnlayout)
    lateinit var quickReturnLayout: QuickReturnLayout


    @BindView(R.id.textview_footer)
    lateinit var footerText: TextView

    @BindView(R.id.recyclerview)
    lateinit var recyclerView: RecyclerView
    lateinit var llm: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this, this)
        llm = recyclerView.setup()
        quickReturnLayout.setup(recyclerView) {
            createHeaderView()
        }
        update()
    }

    /**
     * Reuse the [RecyclerViewHolder] to create the header view, since that this what was shown
     * in the video...
     */
    private fun createHeaderView(): View? {

        return recyclerView.getAdapterOfType<PhotoAdapter>()?.let { adapter ->
            adapter.getItem(0)?.let { firstItem ->
                adapter.PhotoViewHolder(quickReturnLayout).apply {
                    onBindViewHolder(0, adapter.itemCount, firstItem)

                    this.contentLayout.setOnClickListener {
                        recyclerView.smoothScrollToPosition(0)
                    }

                }.itemView.apply {
                    this.setBackgroundColor(resources.getColor(R.color.pink))
                }
                // return the view
            }
        }
    }


    @MainThread
    fun update() {
        stateLayout.showLoading()
        manage("getRecentPhotos",
                Flickr.getRecentPhotos().subscribe { result, error ->
                    stateLayout.showNormal()
                    result?.let {
                        update(it)
                    }

                    error?.let {
                        stateLayout.showError(throwable = it, onRetryClicked = View.OnClickListener {
                            update()
                        })
                    }
                }
        )
    }

    @MainThread
    private fun update(item: RecentPhotosResponse) {
        recyclerView.getAdapterOfType<PhotoAdapter>()?.let {
            quickReturnLayout.hideHeader(false)
            it.replaceItems(item.photos?.items ?: listOf())
            it.notifyDataSetChanged()
        } ?: run {
            recyclerView.adapter = object : PhotoAdapter(this) {
                override fun showDetail(bindedItem: Photo?) {
                    bindedItem?.let {
                        this@MainActivity.showDetail(it)
                    } ?: run {
                        Log.w(TAG, "item not binded? No detail")
                    }
                }
            }.apply {
                addItems(item.photos?.items ?: listOf())
            }
        }

    }

    private fun showError(error: Throwable) {
        showToast(error.message)
    }

    private fun showDetail(photo: Photo) {
        startActivityForResult(Intent(this, DetailActivity::class.java).apply {
            putExtra(EXTRA_PHOTO_JSON, photo.castToString())
        }, REQUEST_PHOTO_DETAIL)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            REQUEST_PHOTO_DETAIL -> {
                // maybe check for resultCode == RESULT_OK?? not in the spec though...
                intent?.getStringExtra(EXTRA_PHOTO_JSON)?.castToObject<Photo>()?.let { photo ->
                    showFooter(photo)
                }
            }
            else -> {
                // other request goes here
            }
        }
    }

    private fun showFooter(photo: Photo) {
        footerText.setOptionalText(photo.title)
    }
}


abstract class PhotoAdapter(ctx: Context) : RecyclerViewAdapter<Photo>(ctx) {
    override fun getDataItemId(dataItem: Photo): String {
        return dataItem.id
    }

    override fun findItemViewType(k: Photo?, position: Int): Int {
        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerViewHolder<Photo> {
        return PhotoViewHolder(parent)
    }


    inner class PhotoViewHolder(parent: ViewGroup?) : RecyclerViewHolder<Photo>(inflater.inflate(R.layout.list_item_photo, parent, false)) {

        @BindView(R.id.textview_title)
        lateinit var titleText: TextView

        @BindView(R.id.textview_subtitle)
        lateinit var subtitleText: TextView

        @BindView(R.id.imageview_square_thumbnail)
        lateinit var squareThumbnail: ImageView

        @BindView(R.id.layout_content)
        lateinit var contentLayout: ViewGroup



        init {
            ButterKnife.bind(this, this.itemView)
        }

        @OnClick(R.id.layout_content)
        fun onContentClicked() {
            this@PhotoAdapter.showDetail(bindedItem)
        }

        override fun onBindViewHolderImpl(position: Int, total: Int, item: Photo) {
            titleText.setOptionalText(item.title)
            subtitleText.setOptionalText(item.owner)

            item.getUrl(Size.thumbnail)?.let { url ->
                Picasso.get()
                        .load(url)
                        .placeholder(R.drawable.ic_image_grey600_24dp)
                        .error(R.drawable.ic_alert_circle_outline_white_24dp)
                        .into(squareThumbnail)
            } ?: run {
                squareThumbnail.setImageResource(R.drawable.ic_alert_circle_outline_white_24dp)
            }

        }
    }

    abstract fun showDetail(bindedItem: Photo?)

}