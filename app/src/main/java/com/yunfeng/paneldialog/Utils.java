package com.yunfeng.paneldialog;

import android.content.Context;

/**
 * Author: xueyunfeng
 */
public class Utils {

    // 将dip或dp值转换为px值，保证尺寸大小不变
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int displayWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int displayHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
}
