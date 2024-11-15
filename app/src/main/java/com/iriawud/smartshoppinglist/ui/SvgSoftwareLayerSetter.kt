import android.graphics.drawable.PictureDrawable
import android.widget.ImageView
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException

class SvgSoftwareLayerSetter : RequestListener<PictureDrawable> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<PictureDrawable>?,
        isFirstResource: Boolean
    ): Boolean {
        // Let Glide handle the error as usual
        return false
    }

    override fun onResourceReady(
        resource: PictureDrawable?,
        model: Any?,
        target: Target<PictureDrawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        // Check if the target is for an ImageView and set layer type
        if (target is ImageViewTarget<*>) {
            (target.view as ImageView).setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)
        }
        return false
    }
}
