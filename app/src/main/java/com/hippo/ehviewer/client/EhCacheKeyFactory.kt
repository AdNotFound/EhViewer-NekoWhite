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
package com.hippo.ehviewer.client

import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.client.data.AbstractGalleryInfo

// E-Hentai Large Preview (v1 Cover): https://ehgt.org/**.jpg
// ExHentai Large Preview (v1 Cover): https://s.exhentai.org/t/**.jpg
// E-Hentai v2 Cover: https://ehgt.org/w/**.webp
// ExHentai v2 Cover: https://s.exhentai.org/w/**.webp
// Normal Preview (v1 v2): https://*.hath.network/c(m|1|2)/[timed token]/[gid]-[index].(jpg|webp)
// Large Preview (v2): https://*.hath.network/[timed token]/**.webp
const val URL_PREFIX_THUMB_E = "https://ehgt.org/"
const val URL_PREFIX_THUMB_EX = "https://s.exhentai.org/"
const val URL_SIGNATURE_THUMB_NORMAL = ".hath.network/c"
private const val URL_PREFIX_V1_THUMB_EX = URL_PREFIX_THUMB_EX + "t/"
private const val URL_PREFIX_V1_THUMB_EX_OLD = "https://exhentai.org/t/"
private val NormalPreviewKeyRegex = Regex("/(c[12m])/[^/]+/(\\d+-\\d+)")

fun getImageKey(gid: Long, index: Int) = "image:$gid:$index"

fun getThumbKey(gid: Long): String = "preview:large:$gid:0"

fun getLargePreviewKey(gid: Long, index: Int) = "preview:large:$gid:$index"

fun getNormalPreviewKey(url: String) = NormalPreviewKeyRegex.find(url)?.let { "preview:normal:${it.groupValues[1]}:${it.groupValues[2]}" } ?: url

val String.isNormalPreviewKey
    get() = startsWith("preview:normal:")

val String.thumbUrl
    get() = removePrefix(URL_PREFIX_THUMB_E)
        .removePrefix(URL_PREFIX_V1_THUMB_EX_OLD)
        .removePrefix(URL_PREFIX_V1_THUMB_EX)
        .removePrefix(URL_PREFIX_THUMB_EX).let {
            if (it.startsWith("https:")) {
                it
            } else {
                if (EhUtils.isExHentai && !Settings.forceEhThumb) {
                    if (it.endsWith("webp")) URL_PREFIX_THUMB_EX else URL_PREFIX_V1_THUMB_EX
                } else {
                    URL_PREFIX_THUMB_E
                } + it
            }
        }

val AbstractGalleryInfo.thumbUrl
    get() = thumb?.thumbUrl
