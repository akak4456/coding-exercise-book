package com.makeus.kkongi.tedimagepicker.tedimagepicker.adapter

import android.app.Activity
import android.net.Uri
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.bumptech.glide.Glide
import com.makeus.kkongi.tedimagepicker.R
import com.makeus.kkongi.tedimagepicker.databinding.ItemGalleryCameraBinding
import com.makeus.kkongi.tedimagepicker.databinding.ItemGalleryMediaBinding
import com.makeus.kkongi.tedimagepicker.tedimagepicker.base.BaseSimpleHeaderAdapter
import com.makeus.kkongi.tedimagepicker.tedimagepicker.base.BaseViewHolder
import com.makeus.kkongi.tedimagepicker.tedimagepicker.builder.TedImagePickerBaseBuilder
import com.makeus.kkongi.tedimagepicker.tedimagepicker.builder.type.MediaType
import com.makeus.kkongi.tedimagepicker.tedimagepicker.model.Media
import com.makeus.kkongi.tedimagepicker.tedimagepicker.util.ToastUtil
import com.makeus.kkongi.tedimagepicker.tedimagepicker.zoom.TedImageZoomActivity

internal class MediaAdapter(
    private val activity: Activity,
    private val builder: TedImagePickerBaseBuilder<*>
) :
    BaseSimpleHeaderAdapter<Media>(if (builder.showCameraTile) 1 else 0) {

    internal val selectedUriList: MutableList<Uri> = mutableListOf()
    var onMediaAddListener: (() -> Unit)? = null

    override fun getHeaderViewHolder(parent: ViewGroup) = CameraViewHolder(parent)
    override fun getItemViewHolder(parent: ViewGroup) = ImageViewHolder(parent)

    fun toggleMediaSelect(uri: Uri) {
        if (selectedUriList.contains(uri)) {
            removeMedia(uri)
        } else {
            addMedia(uri)
        }
    }


    private fun addMedia(uri: Uri) {
        if (selectedUriList.size == builder.maxCount) {
            val message =
                builder.maxCountMessage ?: activity.getString(builder.maxCountMessageResId)
            ToastUtil.showToast(message)
        } else {
            selectedUriList.add(uri)
            onMediaAddListener?.invoke()
            refreshSelectedView()
        }
    }

    private fun getViewPosition(it: Uri): Int =
        items.indexOfFirst { media -> media.uri == it } + headerCount


    private fun removeMedia(uri: Uri) {
        val position = getViewPosition(uri)
        selectedUriList.remove(uri)
        notifyItemChanged(position)
        refreshSelectedView()
    }

    private fun refreshSelectedView() {
        selectedUriList.forEach {
            val position: Int = getViewPosition(it)
            notifyItemChanged(position)
        }
    }

    inner class ImageViewHolder(parent: ViewGroup) :
        BaseViewHolder<ItemGalleryMediaBinding, Media>(parent, R.layout.item_gallery_media) {

        init {
            binding.run {
                selectType = builder.selectType
                viewZoomOut.setOnClickListener {
                    val item = getItem(adapterPosition.takeIf { it != NO_POSITION }
                        ?: return@setOnClickListener)
                    startZoomActivity(item)
                }
                showZoom = false
            }

        }

        override fun bind(data: Media) {
            binding.run {
                media = data
                isSelected = selectedUriList.contains(data.uri)
                if (isSelected) {
                    selectedNumber = selectedUriList.indexOf(data.uri) + 1
                }

                showZoom =
                    !isSelected && (builder.mediaType == MediaType.IMAGE) && builder.showZoomIndicator
            }
        }

        override fun recycled() {
            if (activity.isDestroyed) {
                return
            }
            Glide.with(activity).clear(binding.ivImage)
        }

        private fun startZoomActivity(media: Media) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                binding.ivImage,
                media.uri.toString()
            ).toBundle()

            activity.startActivity(TedImageZoomActivity.getIntent(activity, media.uri), options)

        }
    }

    inner class CameraViewHolder(parent: ViewGroup) : HeaderViewHolder<ItemGalleryCameraBinding>(
        parent, R.layout.item_gallery_camera
    ) {

        init {
            binding.ivImage.setImageResource(builder.cameraTileImageResId)
            itemView.setBackgroundResource(builder.cameraTileBackgroundResId)
        }

    }

}
