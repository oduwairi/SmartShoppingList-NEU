package com.iriawud.smartshoppinglist.ui

import SvgSoftwareLayerSetter
import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.caverock.androidsvg.SVG

object SvgLoader {
    fun load(context: Context, rawResId: Int, imageView: ImageView) {
        val svgUri = "android.resource://${context.packageName}/raw/$rawResId"
        val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(context)
            .`as`(PictureDrawable::class.java)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(SvgSoftwareLayerSetter())
        requestBuilder.load(svgUri).into(imageView)
    }
}
