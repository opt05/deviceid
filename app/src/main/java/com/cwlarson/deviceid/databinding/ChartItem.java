package com.cwlarson.deviceid.databinding;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.DrawableRes;

public class ChartItem extends BaseObservable {
    private float chartAxis1,chartAxis2;
    private int chartPercentage;
    private @DrawableRes int chartDrawable;

    public ChartItem(float axis1, float axis2, @DrawableRes int drawable) {
        this.chartAxis1=axis1;
        this.chartAxis2=axis2;
        this.chartDrawable=drawable;
        this.chartPercentage=(chartAxis1==0&&chartAxis2==0)?0:100-((int)(((chartAxis1>chartAxis2)?chartAxis2/chartAxis1:chartAxis1/chartAxis2)*100));
    }

    @Bindable
    public float getChartAxis1() {
        return chartAxis1;
    }

    public void setChartAxis1(float chartAxis1) {
        this.chartAxis1 = chartAxis1;
    }
    @Bindable
    public float getChartAxis2() {
        return chartAxis2;
    }

    public void setChartAxis2(float chartAxis2) {
        this.chartAxis2 = chartAxis2;
    }
    @Bindable
    public int getChartDrawable() {
        return chartDrawable;
    }

    public void setChartDrawable(@DrawableRes int chartDrawable) {
        this.chartDrawable = chartDrawable;
    }
    @Bindable
    public int getChartPercentage() {
        return chartPercentage;
    }

    public void setChartPercentage(int chartPercentage) {
        this.chartPercentage = chartPercentage;
    }
}
