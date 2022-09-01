package com.cwlarson.deviceid.testutils.shadows

import android.content.ContentResolver
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(ContentResolver::class)
class MyShadowContentResolver {
    private var gsfid: String? = ""

    fun setGSFID(value: String?) {
        gsfid = value
    }

    @Suppress("unused","UNUSED_PARAMETER")
    @Implementation
    fun query(
        uri: Uri?,
        projection: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        sortOrder: String?
    ): Cursor = MatrixCursor(arrayOf("1", "2"), 2).apply {
        newRow().apply {
            add("2", gsfid)
        }
    }
}