package com.cwlarson.deviceid.util;

import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.Method;

public class SystemProperty {
    private final Context mContext;

    public SystemProperty(Context mContext) {
        this.mContext = mContext;
    }

    @SuppressWarnings("unchecked")
    private String getOrThrow(String key) throws NoSuchPropertyException {
        try {
            ClassLoader classLoader = mContext.getClassLoader();
            @SuppressLint("PrivateApi")
            Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
            Method methodGet = SystemProperties.getMethod("get", String.class);
            return (String) methodGet.invoke(SystemProperties, key);
        } catch (Exception e) {
            throw new NoSuchPropertyException(e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    public String get(String key) {
        try {
            return getOrThrow(key);
        } catch (NoSuchPropertyException e) {
            return null;
        }
    }

    private class NoSuchPropertyException extends Exception {
        NoSuchPropertyException(Exception e) {
            super(e);
        }
    }

}
