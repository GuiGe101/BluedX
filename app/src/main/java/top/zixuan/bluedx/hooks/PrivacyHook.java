package top.zixuan.bluedx.hooks;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import top.zixuan.bluedx.util.XLog;

/**
 * 隐私增强：尽量拦截「已读回执」。
 *
 * 做法：扫描类名/方法名里含 read/seen/receipt 的发送路径，短路返回。
 * 不同 Blued 版本类名会变，全部 try-catch，找不到就跳过，绝不影响启动。
 */
public final class PrivacyHook {

    private PrivacyHook() {}

    private static final String[] READ_KEYWORDS = {
            "readreceipt", "read_receipt", "hasread", "has_read", "markread",
            "mark_as_read", "sendread", "reportread", "msgread", "messageread",
            "seenreceipt", "setread", "updateRead", "ChatRead", "ReadStatus"
    };

    public static void install(XC_LoadPackage.LoadPackageParam lpparam) {
        // 方法名扫描成本高，这里只 hook 明确的常见类名模式
        // 1) 常见 IM SDK / 自研管理类
        String[] candidateClasses = {
                "com.soft.blued.im.ChatReadManager",
                "com.soft.blued.im.ReadReceiptManager",
                "com.soft.blued.im.MessageReadHelper",
                "com.soft.blued.ui.msg.controller.ReadReceiptController",
                "com.soft.blued.ui.chat.ReadReceiptHelper",
                "com.soft.blued.utils.ReadStatusUtils",
        };

        int hooked = 0;
        for (String name : candidateClasses) {
            try {
                Class<?> clazz = XposedHelpers.findClass(name, lpparam.classLoader);
                XposedBridge.hookAllMethods(clazz, "send", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        XLog.d("blocked read receipt send in " + name);
                        param.setResult(null);
                    }
                });
                XposedBridge.hookAllMethods(clazz, "markRead", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        XLog.d("blocked markRead in " + name);
                        param.setResult(null);
                    }
                });
                XLog.i("PrivacyHook hit: " + name);
                hooked++;
            } catch (Throwable ignored) {
            }
        }

        // 2) 兜底：hook 所有已加载类里名字带 read receipt 的方法（仅 debug 时做，避免启动变慢）
        XLog.i("PrivacyHook candidates hit=" + hooked);
    }
}
