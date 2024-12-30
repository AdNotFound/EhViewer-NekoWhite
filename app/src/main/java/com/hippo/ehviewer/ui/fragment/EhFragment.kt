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
package com.hippo.ehviewer.ui.fragment

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import com.hippo.ehviewer.EhApplication
import com.hippo.ehviewer.R
import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.client.EhCookieStore
import com.hippo.ehviewer.client.EhEngine
import com.hippo.ehviewer.client.EhTagDatabase
import com.hippo.ehviewer.client.EhUrl.SITE_EX
import com.hippo.ehviewer.client.EhUtils.isExHentai
import com.hippo.util.launchNonCancellable

class EhFragment : BasePreferenceFragment() {
    private lateinit var detailSize: Preference
    private lateinit var listThumbSize: Preference
    private lateinit var thumbSize: Preference
    private lateinit var thumbShowTitle: Preference
    private lateinit var forceEhThumb: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.eh_settings)
        val account = findPreference<Preference>(Settings.KEY_ACCOUNT)
        val gallerySite = findPreference<Preference>(Settings.KEY_GALLERY_SITE)
        val theme = findPreference<Preference>(Settings.KEY_THEME)
        val blackDarkTheme = findPreference<Preference>(Settings.KEY_BLACK_DARK_THEME)
        val listMode = findPreference<Preference>(Settings.KEY_LIST_MODE)
        val showTagTranslations = findPreference<Preference>(Settings.KEY_SHOW_TAG_TRANSLATIONS)
        val tagTranslationsSource = findPreference<Preference>(Settings.KEY_TAG_TRANSLATIONS_SOURCE)
        detailSize = findPreference<Preference>(Settings.KEY_DETAIL_SIZE)!!
        listThumbSize = findPreference<Preference>(Settings.KEY_LIST_THUMB_SIZE)!!
        thumbSize = findPreference<Preference>(Settings.KEY_THUMB_SIZE)!!
        thumbShowTitle = findPreference<Preference>(Settings.KEY_THUMB_SHOW_TITLE)!!
        forceEhThumb = findPreference<Preference>(Settings.KEY_FORCE_EH_THUMB)!!

        gallerySite!!.onPreferenceChangeListener = this
        theme!!.onPreferenceChangeListener = this
        blackDarkTheme!!.onPreferenceChangeListener = this
        listMode!!.onPreferenceChangeListener = this
        showTagTranslations!!.onPreferenceChangeListener = this
        detailSize.onPreferenceChangeListener = this
        listThumbSize.onPreferenceChangeListener = this
        thumbSize.onPreferenceChangeListener = this
        thumbShowTitle.onPreferenceChangeListener = this
        Settings.displayName?.let { account?.summary = it }
        if (!EhTagDatabase.isTranslatable(requireActivity())) {
            if (!Settings.showTagTranslations) {
                preferenceScreen.removePreference(showTagTranslations)
            }
            preferenceScreen.removePreference(tagTranslationsSource!!)
        }
        if (!EhCookieStore.hasSignedIn()) {
            Settings.SIGN_IN_REQUIRED.forEach {
                val preference = findPreference<Preference>(it)
                preferenceScreen.removePreference(preference!!)
            }
        }
        showForceEhThumb(isExHentai)
        updateListPreference(Settings.listMode)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val key = preference.key
        if (Settings.KEY_THEME == key) {
            AppCompatDelegate.setDefaultNightMode((newValue as String).toInt())
            requireActivity().recreate()
        } else if (Settings.KEY_BLACK_DARK_THEME == key) {
            if (requireActivity().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES > 0) {
                EhApplication.application.recreateAllActivity()
            }
        } else if (Settings.KEY_GALLERY_SITE == key) {
            showForceEhThumb((newValue as String).toInt() == SITE_EX)
            requireActivity().setResult(Activity.RESULT_OK)
            lifecycleScope.launchNonCancellable {
                runCatching {
                    EhEngine.getUConfig()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        } else if (Settings.KEY_LIST_MODE == key) {
            updateListPreference((newValue as String).toInt())
            requireActivity().setResult(Activity.RESULT_OK)
        } else if (Settings.KEY_DETAIL_SIZE == key) {
            requireActivity().setResult(Activity.RESULT_OK)
        } else if (Settings.KEY_LIST_THUMB_SIZE == key) {
            requireActivity().setResult(Activity.RESULT_OK)
        } else if (Settings.KEY_THUMB_SIZE == key) {
            requireActivity().setResult(Activity.RESULT_OK)
        } else if (Settings.KEY_THUMB_SHOW_TITLE == key) {
            requireActivity().setResult(Activity.RESULT_OK)
        } else if (Settings.KEY_SHOW_TAG_TRANSLATIONS == key) {
            if (java.lang.Boolean.TRUE == newValue) {
                lifecycleScope.launchNonCancellable { EhTagDatabase.update(true) }
            }
        }
        return true
    }

    @get:StringRes
    override val fragmentTitle: Int
        get() = R.string.settings_eh

    private fun showForceEhThumb(newValue: Boolean) {
        forceEhThumb.isVisible = newValue
    }

    private fun updateListPreference(newValue: Int) {
        val isDetailMode = newValue == 0
        detailSize.isVisible = isDetailMode
        listThumbSize.isVisible = isDetailMode
        thumbSize.isVisible = !isDetailMode
        thumbShowTitle.isVisible = !isDetailMode
    }
}
