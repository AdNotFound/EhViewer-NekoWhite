package com.hippo.ehviewer.preference

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.hippo.ehviewer.R
import com.hippo.ehviewer.Settings
import com.hippo.preference.DialogPreference
import com.hippo.yorozuya.ViewUtils

class UserAgentPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : DialogPreference(context, attrs), View.OnClickListener {
    private var mType: Spinner? = null
    private var mCustomUserAgentInputLayout: TextInputLayout? = null
    private var mCustomUserAgent: EditText? = null
    private val mArray: Array<String>

    init {
        mArray = context.resources.getStringArray(R.array.user_agent_types)
        dialogLayoutResource = R.layout.preference_dialog_useragent
        updateSummary(Settings.userAgent ?: Settings.DEFAULT_USER_AGENT)
    }

    private fun updateSummary(userAgent: String) {
        summary = userAgent
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
        builder.setPositiveButton(android.R.string.ok, null)
    }

    override fun onDialogCreated(dialog: AlertDialog) {
        super.onDialogCreated(dialog)
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this)
        mType = ViewUtils.`$$`(dialog, R.id.type) as Spinner
        mCustomUserAgentInputLayout = ViewUtils.`$$`(dialog, R.id.custom_useragent_input_layout) as TextInputLayout
        mCustomUserAgent = ViewUtils.`$$`(dialog, R.id.custom_useragent) as EditText

        val currentUA = Settings.userAgent
        mCustomUserAgent!!.setText(currentUA)
        if (currentUA in Settings.builtInUserAgents) {
            mType!!.setSelection(Settings.builtInUserAgents.indexOf(currentUA))
        } else {
            mType!!.setSelection(Settings.builtInUserAgents.size) // Select "Custom"
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)
        mType = null
        mCustomUserAgentInputLayout = null
        mCustomUserAgent = null
    }

    override fun onClick(v: View) {
        val dialog = dialog
        val context: Context = context
        if (null == dialog || null == mType || null == mCustomUserAgentInputLayout || null == mCustomUserAgent) {
            return
        }
        val type = mType!!.selectedItemPosition
        val userAgent = if (type < Settings.builtInUserAgents.size) {
            Settings.builtInUserAgents[type]
        } else {
            mCustomUserAgent!!.text.toString().trim()
        }
        if (userAgent.isEmpty()) {
            mCustomUserAgentInputLayout!!.error = context.getString(R.string.text_is_empty)
            return
        }
        mCustomUserAgentInputLayout!!.error = null
        Settings.putUserAgent(userAgent)
        updateSummary(userAgent)
        dialog.dismiss()
    }
}
