package com.cwlarson.deviceid.databinding

import androidx.annotation.DrawableRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Ignore

data class ChartItem(
        @get:Bindable
        var chartAxis1 : Float,
        @get:Bindable
        var chartAxis2 : Float,
        @get:Bindable
        @DrawableRes
        var chartDrawable : Int
): BaseObservable() {
        @Ignore
        @get:Bindable
        val chartPercentage : Int
                = if(chartAxis1 == 0f && chartAxis2 == 0f) 0 else 100 -
                        ((if(chartAxis1 > chartAxis2) chartAxis2 / chartAxis1
                        else chartAxis1 / chartAxis2) * 100).toInt()
}
