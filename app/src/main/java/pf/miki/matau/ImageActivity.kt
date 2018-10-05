package pf.miki.matau

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import com.alexvasilkov.gestures.Settings
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        gesture_layout.controller.settings
                .setMaxZoom(3f)
                .setDoubleTapZoom(-1f) // Falls back to max zoom level
                .setPanEnabled(true)
                .setZoomEnabled(true)
                .setDoubleTapEnabled(true)
                .setRotationEnabled(false)
                .setRestrictRotation(false)
                .setOverscrollDistance(0f, 0f)
                .setOverzoomFactor(3f)
                .setFillViewport(true)
                .setFitMethod(Settings.Fit.OUTSIDE).gravity = Gravity.CENTER
        val imageURL = intent.getStringExtra("image")
        if (imageURL != null)
            Glide.with(this)
                    .load(Uri.parse(imageURL))
                    .into(frame_layout_image)

    }
}
