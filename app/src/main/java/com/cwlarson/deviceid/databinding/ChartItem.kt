package com.cwlarson.deviceid.databinding

import androidx.annotation.DrawableRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Ignore

data class ChartItem(
        @get:Bindable
        var chartaxis1 : Float,
        @get:Bindable
        var chartaxis2 : Float,
        @get:Bindable
        @DrawableRes
        var chartdrawable : Int
) : BaseObservable() {
        @get:Bindable
        @Ignore
        val chartpercentage : Int
                = if(chartaxis1 == 0f && chartaxis2 == 0f) 0 else 100 -
                        ((if(chartaxis1 > chartaxis2) chartaxis2 / chartaxis1
                        else chartaxis1 / chartaxis2) * 100).toInt()
}
