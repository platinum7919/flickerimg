package com.foo.common.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class RecyclerViewAdapter<K>(context: Context) : RecyclerView.Adapter<RecyclerViewHolder<K>>() {
    val TAG = this::class.simpleName

    var inflater: LayoutInflater
        protected set

    init {
        inflater = LayoutInflater.from(context)
    }

    protected var itemsArray = ArrayList<K>()

    /**
     * Return a copy of the current data in the adapter
     */
    @Synchronized
    fun copyItemsArray(): ArrayList<K> {
        return ArrayList(itemsArray)
    }

    @Synchronized
    fun addItems(items: List<K>): Int {
        return copyItemsArray().apply {
            addAll(items)
            itemsArray = this
        }.size
    }


    override fun getItemCount(): Int {
        return count
    }


    val count: Int
        @Synchronized get() {
            return itemsArray.size
        }

    fun findItemByPosition(position: Int): K? {
        return if (0 <= position && position < this.count) itemsArray[position] else null
    }

    /**
     * Implementation for getting a String id from a dataItem
     */
    protected abstract fun getDataItemId(dataItem: K): String

    /**
     * Implementation for getting the ViewType of a dataItem
     */
    protected abstract fun findItemViewType(k: K?, position: Int): Int


    override fun getItemViewType(position: Int): Int {
        return findItemViewType(findItemByPosition(position), position)
    }


    override fun onBindViewHolder(viewHolder: RecyclerViewHolder<K>, position: Int) {
        viewHolder.onBindViewHolder(position, itemCount, findItemByPosition(position))
    }

}

abstract class RecyclerViewHolder<K> @JvmOverloads constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val ctx: Context
        get() {
            return itemView.context
        }

    var bindedItem: K? = null

    init {
    }


    internal fun onBindViewHolder(position: Int, total: Int, item: K?) {
        bindedItem = item
        item?.let {
            onBindViewHolderImpl(position, total, it)
        }
    }

    abstract fun onBindViewHolderImpl(position: Int, total: Int, item: K)


    open fun onViewAttachedToWindow() {

    }
}