package com.foo.flickerimg

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract.Helpers.update
import android.support.annotation.MainThread
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
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
import com.foo.common.utils.firstNotNull
import com.foo.flickerimg.views.QuickReturnLayout
import com.foo.flickerimg.views.StateLayout
import com.foo.flickerimg.views.setOptionalText
import com.squareup.picasso.Picasso
import io.reactivex.disposables.CompositeDisposable

class MainActivity : BaseActivity() {


    @BindView(R.id.statelayout)
    lateinit var stateLayout: StateLayout

    @BindView(R.id.quickreturnlayout)
    lateinit var quickReturnLayout: QuickReturnLayout


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
    }

    /**
     * Reuse the [RecyclerViewHolder] to create the header view
     */
    private fun createHeaderView(): View? {
        return recyclerView.getAdapterOfType<PhotoAdapter>()?.let { adapter ->
            adapter.getItem(0)?.let { firstItem ->
                PhotoViewHolder(recyclerView, layoutInflater).apply {
                    onBindViewHolder(0, adapter.itemCount, firstItem)
                }.itemView.apply {
                    this.setBackgroundColor(resources.getColor(R.color.pink))
                }
                // return the view
            }
        }
    }

    override fun onResume() {
        super.onResume()
        update()
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
                        showError(it)
                    }
                }
        )
    }

    @MainThread
    private fun update(item: RecentPhotosResponse) {
        recyclerView.adapter = PhotoAdapter(this).apply {
            addItems(item.photos?.items ?: listOf())
        }
    }

    private fun showError(error: Throwable) {
        showToast(error.message)
    }

}

class PhotoViewHolder(parent: ViewGroup, inflater: LayoutInflater) : RecyclerViewHolder<Photo>(inflater.inflate(R.layout.list_item_photo, parent, false)) {

    @BindView(R.id.textview_title)
    lateinit var titleText: TextView

    @BindView(R.id.textview_subtitle)
    lateinit var subtitleText: TextView

    @BindView(R.id.imageview_square_thumbnail)
    lateinit var squareThumbnail: ImageView


    init {
        ButterKnife.bind(this, this.itemView)
    }

    override fun onBindViewHolderImpl(position: Int, total: Int, item: Photo) {
        titleText.setOptionalText(item.title)
        subtitleText.setOptionalText(item.owner)


        item.getUrl(Size.thumbnail)?.let { url ->
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.ic_image_grey600_24dp)
                    .error(R.drawable.ic_alert_circle_outline_white_24dp)
                    .into(squareThumbnail);
        } ?: run {
            squareThumbnail.setImageResource(R.drawable.ic_alert_circle_outline_white_24dp)
        }

    }
}


class PhotoAdapter(ctx: Context) : RecyclerViewAdapter<Photo>(ctx) {
    override fun getDataItemId(dataItem: Photo): String {
        return dataItem.id
    }

    override fun findItemViewType(k: Photo?, position: Int): Int {
        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerViewHolder<Photo> {
        return PhotoViewHolder(parent, inflater)
    }


}