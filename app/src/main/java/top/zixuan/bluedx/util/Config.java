package top.zixuan.bluedx.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import de.robv.android.xposed.XSharedPreferences;

import top.zixuan.bluedx.BluedX;

/**
 * 模块配置。
 * UI 写 SharedPreferences；Hook 侧用 XSharedPreferences 读。
 * 每次写完把 prefs 文件 chmod 成可读，保证 XSharedPreferences 能读到。
 */
public final class Config {
    private Config() {}

    private static final String MODULE_PKG = "top.zixuan.bluedx";

    private static XSharedPreferences sPrefs;

    public static synchronized XSharedPreferences prefs() {
        if (sPrefs == null) {
            sPrefs = new XSharedPreferences(MODULE_PKG, BluedX.PREFS_NAME);
            sPrefs.makeWorldReadable();
        } else {
            sPrefs.reload();
        }
        return sPrefs;
    }

    /** 模块自身 UI 使用 */
    public static SharedPreferences appPrefs(Context ctx) {
        return ctx.getSharedPreferences(BluedX.PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** 写完后把 prefs 文件放行，供 XSharedPreferences 读取 */
    public static void makeReadable(Context ctx) {
        try {
            File prefsFile = new File(ctx.getApplicationInfo().dataDir, "shared_prefs/" + BluedX.PREFS_NAME + ".xml");
            if (prefsFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                prefsFile.setReadable(true, false);
                //noinspection ResultOfMethodCallIgnored
                prefsFile.setExecutable(false, false);
            }
            if (sPrefs != null) {
                sPrefs.makeWorldReadable();
                sPrefs.reload();
            }
        } catch (Throwable ignored) {
        }
    }

    public static boolean get(String key, boolean def) {
        try {
            return prefs().getBoolean(key, def);
        } catch (Throwable t) {
            return def;
        }
    }

    public static boolean unlockScreenshot() {
        return get(BluedX.KEY_UNLOCK_SCREENSHOT, BluedX.DEFAULT_ON);
    }

    public static boolean chatScreenshotProtect() {
        return get(BluedX.KEY_CHAT_SCREENSHOT_PROTECT, BluedX.DEFAULT_ON);
    }

    public static boolean blockAds() {
        return get(BluedX.KEY_BLOCK_ADS, BluedX.DEFAULT_ON);
    }

    public static boolean blockAnalytics() {
        return get(BluedX.KEY_BLOCK_ANALYTICS, BluedX.DEFAULT_ON);
    }

    public static boolean hideRead() {
        return get(BluedX.KEY_HIDE_READ, BluedX.DEFAULT_ON);
    }

    public static boolean delayRead() {
        return get(BluedX.KEY_DELAY_READ, true);
    }

    public static boolean cleanFeed() {
        return get(BluedX.KEY_CLEAN_FEED, BluedX.DEFAULT_ON);
    }

    public static boolean debugLog() {
        return get(BluedX.KEY_DEBUG_LOG, BluedX.DEFAULT_DEBUG);
    }
}
