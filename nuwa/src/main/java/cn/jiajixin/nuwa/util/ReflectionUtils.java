package cn.jiajixin.nuwa.util;

import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by jixin.jia on 15/10/31.
 */
public class ReflectionUtils {
    public static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        Log.i("ReflectionUtils", field);
        return localField.get(obj);
    }

    public static void setField(Object obj, Class<?> cl, String field, Object value)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
        Log.i("ReflectionUtils", field);
        Log.i("ReflectionUtils", value.toString());
    }
}
