package top.zixuan.bluedx;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * BluedX 全局常量
 */
public final class BluedX {
    private BluedX() {}

    public static final String TAG = "BluedX";

    /** 原版 Blued / 小蓝（danlan.xiaolan）共用同一套代码 */
    public static final String TARGET_PKG = "com.soft.blued";
    public static final String TARGET_PKG_XIAOLAN = "com.danlan.xiaolan";

    public static final Set<String> TARGET_PACKAGES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(TARGET_PKG, TARGET_PKG_XIAOLAN)));

    public static final String PREFS_NAME = "bluedx_config";

    public static final String KEY_UNLOCK_SCREENSHOT = "unlock_screenshot";
    public static final String KEY_CHAT_SCREENSHOT_PROTECT = "chat_screenshot_protect";
    public static final String KEY_BLOCK_ADS = "block_ads";
    public static final String KEY_BLOCK_ANALYTICS = "block_analytics";
    public static final String KEY_HIDE_READ = "hide_read";
    public static final String KEY_DELAY_READ = "delay_read";
    public static final String KEY_CLEAN_FEED = "clean_feed";
    public static final String KEY_DEBUG_LOG = "debug_log";

    // 默认全部打开（调试日志默认关）
    public static final boolean DEFAULT_ON = true;
    public static final boolean DEFAULT_DEBUG = false;

    public static boolean isTarget(String packageName) {
        return TARGET_PACKAGES.contains(packageName);
    }
}
