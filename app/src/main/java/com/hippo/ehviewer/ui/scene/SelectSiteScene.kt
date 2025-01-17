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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.hippo.ehviewer.R
import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.client.EhUrl
import com.hippo.ehviewer.client.EhUtils
import com.hippo.yorozuya.ViewUtils

class SelectSiteScene :
    SolidScene(),
    View.OnClickListener {
    private var mButtonGroup: MaterialButtonToggleGroup? = null
    private var mOk: View? = null

    override fun needShowLeftDrawer(): Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.scene_select_site, container, false)
        mButtonGroup = ViewUtils.`$$`(view, R.id.button_group) as MaterialButtonToggleGroup
        (ViewUtils.`$$`(view, if (EhUtils.isExHentai) R.id.site_ex else R.id.site_e) as MaterialButton).isChecked = true
        mOk = ViewUtils.`$$`(view, R.id.ok)
        mOk!!.setOnClickListener(this)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mButtonGroup = null
        mOk = null
    }

    override fun onClick(v: View) {
        val id = mButtonGroup?.checkedButtonId ?: return
        if (v == mOk) {
            Settings.putSelectSite(false)
            Settings.putGallerySite(if (id == R.id.site_ex) EhUrl.SITE_EX else EhUrl.SITE_E)
            startSceneForCheckStep(CHECK_STEP_SELECT_SITE, arguments)
            finish()
        }
    }
}
