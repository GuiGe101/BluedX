package top.zixuan.bluedx.hooks;

import de.robv.android.xposed.XC_MethodReplacement;

/**
 * 模块激活探测。
 * XposedEntry 在模块自身包名加载时把 sActive 置 true，
 * 设置页据此显示「已激活 / 未激活」。
 */
public final class ModuleStatus {
    private ModuleStatus() {}

    private static volatile boolean sActive = false;

    public static void markActive() {
        sActive = true;
    }

    public static boolean isActive() {
        return sActive;
    }

    /** 供 Xposed hook 替换用 */
    public static XC_MethodReplacement activeTrue() {
        return XC_MethodReplacement.returnConstant(true);
    }
}
