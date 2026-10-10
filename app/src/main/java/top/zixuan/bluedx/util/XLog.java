package top.zixuan.bluedx.util;

import de.robv.android.xposed.XposedBridge;

import top.zixuan.bluedx.BluedX;

/**
 * 统一日志。release 时只打关键信息，debug 开关打开后打全部。
 */
public final class XLog {
    private XLog() {}

    private static volatile boolean sDebug = false;

    public static void setDebug(boolean debug) {
        sDebug = debug;
    }

    public static boolean isDebug() {
        return sDebug;
    }

    public static void d(String msg) {
        if (sDebug) {
            XposedBridge.log(BluedX.TAG + " D " + msg);
        }
    }

    public static void i(String msg) {
        XposedBridge.log(BluedX.TAG + " I " + msg);
    }

    public static void e(String msg, Throwable t) {
        XposedBridge.log(BluedX.TAG + " E " + msg);
        if (t != null) {
            XposedBridge.log(t);
        }
    }
}
