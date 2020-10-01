package com.cwlarson.deviceid.tabsdetail

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import com.cwlarson.deviceid.R
import com.cwlarson.deviceid.tabs.Item
import com.cwlarson.deviceid.util.clipboardManager
import com.cwlarson.deviceid.util.toast

internal fun Activity?.shareItem(item: Item): Boolean =
        this?.let {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, getString(R.string.send_to))
                putExtra(Intent.EXTRA_TEXT, item.subtitle?.getSubTitleText())
            }
            shareIntent.resolveActivity(packageManager)?.let {
                startActivity(Intent.createChooser(shareIntent, null))
                true
            } ?: toast(R.string.send_to_no_apps, Toast.LENGTH_LONG); false
        } ?: false

internal fun Activity?.copyItemToClipboard(item: Item): Boolean =
        this?.let {
            val text = item.getFormattedString(it)
            val clip = ClipData.newPlainText(text, item.subtitle?.getSubTitleText())
            clipboardManager.setPrimaryClip(clip)
            toast(getString(R.string.copy_to_clipboard, text))
            true
        } ?: false