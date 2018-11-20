package com.foo.flickerimg.views

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.foo.flickerimg.R

/**
 * Animation helper class
 */
object Animations {

    fun animate(ctx: Context, view: View, animResId: Int, startVisibility: Int, endVisibility: Int, additionalListener: Animation.AnimationListener? = null): Animation {
        view.visibility = startVisibility
        view.clearAnimation()
        return AnimationUtils.loadAnimation(ctx, animResId).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    view.setTag(R.id.animation_hashcode, animation.hashCode())
                    additionalListener?.onAnimationStart(animation)
                }

                override fun onAnimationRepeat(animation: Animation) {
                    additionalListener?.onAnimationRepeat(animation)
                }

                override fun onAnimationEnd(animation: Animation) {
                    view.visibility = endVisibility
                    additionalListener?.onAnimationEnd(animation)
                }
            })
            view.startAnimation(this)
        }
    }


}

open class DefaultAnimationListener : Animation.AnimationListener {

    override fun onAnimationRepeat(animation: Animation?) {

    }

    override fun onAnimationEnd(animation: Animation?) {

    }


    override fun onAnimationStart(animation: Animation?) {
    }

}