/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hippo.ehviewer.ui.scene

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.hippo.easyrecyclerview.EasyRecyclerView
import com.hippo.easyrecyclerview.MarginItemDecoration
import com.hippo.ehviewer.R
import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.client.EhClient
import com.hippo.ehviewer.client.EhRequest
import com.hippo.ehviewer.client.EhUrl
import com.hippo.ehviewer.client.data.GalleryDetail
import com.hippo.ehviewer.client.data.GalleryInfo
import com.hippo.ehviewer.client.data.GalleryPreview
import com.hippo.ehviewer.client.data.PreviewSet
import com.hippo.ehviewer.client.exception.EhException
import com.hippo.ehviewer.ui.GalleryActivity
import com.hippo.util.getParcelableCompat
import com.hippo.widget.ContentLayout
import com.hippo.widget.ContentLayout.ContentHelper
import com.hippo.widget.LoadImageView
import com.hippo.widget.Slider
import com.hippo.widget.recyclerview.AutoGridLayoutManager
import com.hippo.yorozuya.LayoutUtils
import com.hippo.yorozuya.ViewUtils
import java.util.Locale

class GalleryPreviewsScene : ToolbarScene() {
    private var mGalleryInfo: GalleryInfo? = null
    private var mRecyclerView: EasyRecyclerView? = null
    private var mAdapter: GalleryPreviewAdapter? = null
    private var mHelper: GalleryPreviewHelper? = null
    private var mHasFirstRefresh = false
    private var mScrollTo = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            onInit()
        } else {
            onRestore(savedInstanceState)
        }
    }

    private fun onInit() {
        val args = arguments ?: return
        mGalleryInfo = args.getParcelableCompat(KEY_GALLERY_INFO)
        mScrollTo = args.getInt(KEY_SCROLL_TO)
    }

    private fun onRestore(savedInstanceState: Bundle) {
        mGalleryInfo = savedInstanceState.getParcelableCompat(KEY_GALLERY_INFO)
        mHasFirstRefresh = savedInstanceState.getBoolean(KEY_HAS_FIRST_REFRESH)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val hasFirstRefresh: Boolean = if (mHelper != null && 1 == mHelper!!.shownViewIndex) {
            false
        } else {
            mHasFirstRefresh
        }
        outState.putBoolean(KEY_HAS_FIRST_REFRESH, hasFirstRefresh)
        outState.putParcelable(KEY_GALLERY_INFO, mGalleryInfo)
    }

    override fun onCreateViewWithToolbar(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val mContentLayout = inflater.inflate(
            R.layout.scene_gallery_previews,
            container,
            false,
        ) as ContentLayout
        mContentLayout.hideFastScroll()
        mRecyclerView = mContentLayout.recyclerView
        mAdapter = GalleryPreviewAdapter()
        mRecyclerView!!.adapter = mAdapter
        val columnWidth = Settings.previewSize
        val layoutManager =
            AutoGridLayoutManager(context, columnWidth, LayoutUtils.dp2pix(context, 16f))
        layoutManager.setStrategy(AutoGridLayoutManager.STRATEGY_SUITABLE_SIZE)
        mRecyclerView!!.layoutManager = layoutManager
        mRecyclerView!!.clipToPadding = false
        val padding = LayoutUtils.dp2pix(context, 4f)
        val decoration = MarginItemDecoration(padding, padding, padding, padding, padding)
        mRecyclerView!!.addItemDecoration(decoration)
        mHelper = GalleryPreviewHelper()
        mContentLayout.setHelper(mHelper!!)

        // Only refresh for the first time
        if (!mHasFirstRefresh) {
            mHasFirstRefresh = true
            if (mScrollTo == -1) {
                mHelper!!.goTo(1)
                mScrollTo = 0
            } else {
                mHelper!!.firstRefresh()
            }
        }
        return mContentLayout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (null != mHelper) {
            if (1 == mHelper!!.shownViewIndex) {
                mHasFirstRefresh = false
            }
        }
        if (null != mRecyclerView) {
            mRecyclerView!!.stopScroll()
            mRecyclerView = null
        }
        mAdapter = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitle(R.string.gallery_previews)
        setNavigationIcon(R.drawable.v_arrow_left_dark_x24)
    }

    override fun getMenuResId(): Int = if ((mGalleryInfo as GalleryDetail).previewPages > 1) R.menu.scene_gallery_previews else 0

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val context = context ?: return false
        val id = item.itemId
        if (id == R.id.action_go_to) {
            if (mHelper == null) {
                return true
            }
            val pages = mHelper!!.pages
            if (pages > 1 && mHelper!!.canGoTo()) {
                val helper = GoToDialogHelper(pages, mHelper!!.pageForTop)
                val dialog = AlertDialog.Builder(context).setTitle(R.string.go_to)
                    .setView(R.layout.dialog_go_to)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                dialog.show()
                helper.setDialog(dialog)
            }
            return true
        }
        return false
    }

    override fun onNavigationClick() {
        onBackPressed()
    }

    fun onItemClick(position: Int): Boolean {
        val context = context
        if (null != context && null != mHelper && null != mGalleryInfo) {
            val p = mHelper!!.getDataAtEx(position)
            if (p != null) {
                val intent = Intent(context, GalleryActivity::class.java)
                intent.action = GalleryActivity.ACTION_EH
                intent.putExtra(GalleryActivity.KEY_GALLERY_INFO, mGalleryInfo)
                intent.putExtra(GalleryActivity.KEY_PAGE, p.position)
                startActivity(intent)
            }
        }
        return true
    }

    private fun onGetPreviewSetSuccess(result: Pair<PreviewSet, Int>, taskId: Int) {
        if (null != mHelper && mHelper!!.isCurrentTask(taskId) && null != mGalleryInfo) {
            val previewSet = result.first
            val size = previewSet.size()
            val list = ArrayList<GalleryPreview>(size)
            for (i in 0 until size) {
                list.add(previewSet.getGalleryPreview(mGalleryInfo!!.gid, i))
            }
            mHelper!!.onGetPageData(
                taskId,
                result.second,
                0,
                null,
                null,
                list as List<GalleryPreview>,
            )
            if (mScrollTo != 0 && mScrollTo < size) {
                mHelper!!.scrollTo(mScrollTo)
                mScrollTo = 0
            }
        }
    }

    private fun onGetPreviewSetFailure(e: Exception, taskId: Int) {
        if (mHelper != null && mHelper!!.isCurrentTask(taskId)) {
            mHelper!!.onGetException(taskId, e)
        }
    }

    private class GalleryPreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: LoadImageView
        var text: TextView

        init {
            image = itemView.findViewById(R.id.image)
            text = itemView.findViewById(R.id.text)
        }
    }

    private inner class GetPreviewSetListener(
        context: Context,
        private val mTaskId: Int,
    ) : EhCallback<GalleryPreviewsScene, Pair<PreviewSet, Int>>(context) {
        override fun onSuccess(result: Pair<PreviewSet, Int>) {
            val scene = this@GalleryPreviewsScene
            scene.onGetPreviewSetSuccess(result, mTaskId)
        }

        override fun onFailure(e: Exception) {
            val scene = this@GalleryPreviewsScene
            scene.onGetPreviewSetFailure(e, mTaskId)
        }

        override fun onCancel() {}
    }

    private inner class GalleryPreviewAdapter : RecyclerView.Adapter<GalleryPreviewHolder>() {
        private val mInflater: LayoutInflater = layoutInflater

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryPreviewHolder = GalleryPreviewHolder(
            mInflater.inflate(
                R.layout.item_gallery_preview,
                parent,
                false,
            ),
        )

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: GalleryPreviewHolder, position: Int) {
            if (null != mHelper) {
                val preview = mHelper!!.getDataAtEx(position)
                if (preview != null) {
                    preview.load(holder.image)
                    holder.text.text = (preview.position + 1).toString()
                }
            }
            holder.itemView.setOnClickListener { onItemClick(position) }
        }

        override fun getItemCount(): Int = if (mHelper != null) mHelper!!.size() else 0
    }

    private inner class GalleryPreviewHelper : ContentHelper<GalleryPreview>() {
        override fun getPageData(
            taskId: Int,
            type: Int,
            page: Int,
            index: String?,
            isNext: Boolean,
        ) {
            val activity = mainActivity
            if (null == activity || null == mGalleryInfo) {
                onGetException(taskId, EhException(getString(R.string.error_cannot_find_gallery)))
                return
            }
            val url =
                EhUrl.getGalleryDetailUrl(mGalleryInfo!!.gid, mGalleryInfo!!.token, page, false)
            val request = EhRequest()
            request.setMethod(EhClient.METHOD_GET_PREVIEW_SET)
            request.setCallback(
                GetPreviewSetListener(context, taskId),
            )
            request.setArgs(url)
            request.enqueue(this@GalleryPreviewsScene)
        }

        override val context
            get() = this@GalleryPreviewsScene.requireContext()

        @SuppressLint("NotifyDataSetChanged")
        override fun notifyDataSetChanged() {
            if (mAdapter != null) {
                mAdapter!!.notifyDataSetChanged()
            }
        }

        override fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {
            if (mAdapter != null) {
                mAdapter!!.notifyItemRangeInserted(positionStart, itemCount)
            }
        }

        override fun isDuplicate(d1: GalleryPreview, d2: GalleryPreview): Boolean = false
    }

    private inner class GoToDialogHelper(private val mPages: Int, private val mCurrentPage: Int) :
        View.OnClickListener,
        DialogInterface.OnDismissListener {
        private var mSlider: Slider? = null
        private var mDialog: Dialog? = null
        fun setDialog(dialog: AlertDialog) {
            mDialog = dialog
            (ViewUtils.`$$`(dialog, R.id.start) as TextView).text =
                String.format(Locale.US, "%d", 1)
            (ViewUtils.`$$`(dialog, R.id.end) as TextView).text =
                String.format(Locale.US, "%d", mPages)
            mSlider = ViewUtils.`$$`(dialog, R.id.slider) as Slider
            mSlider!!.setRange(1, mPages)
            mSlider!!.progress = mCurrentPage + 1
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this)
            dialog.setOnDismissListener(this)
        }

        override fun onClick(v: View) {
            if (null == mSlider) {
                return
            }
            val page = mSlider!!.progress - 1
            if (page in 0 until mPages && mHelper != null) {
                mHelper!!.goTo(page)
                if (mDialog != null) {
                    mDialog!!.dismiss()
                    mDialog = null
                }
            } else {
                showTip(R.string.error_out_of_range, LENGTH_LONG)
            }
        }

        override fun onDismiss(dialog: DialogInterface) {
            mDialog = null
            mSlider = null
        }
    }

    companion object {
        const val KEY_GALLERY_INFO = "gallery_info"
        const val KEY_SCROLL_TO = "scroll_to"
        private const val KEY_HAS_FIRST_REFRESH = "has_first_refresh"
    }
}
