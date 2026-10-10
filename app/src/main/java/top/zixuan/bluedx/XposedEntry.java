package top.zixuan.bluedx;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 极简入口：启动阶段零 Hook，全部反射 + 延后，扛住加固壳。
 */
public class XposedEntry implements IXposedHookLoadPackage {

    private static void log(String msg) {
        try {
            Log.i("BluedX", msg);
        } catch (Throwable ignored) {
        }
        try {
            de.robv.android.xposed.XposedBridge.log("BluedX I " + msg);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        String pkg = lpparam.packageName;
        log("handleLoadPackage pkg=" + pkg + " process=" + lpparam.processName);

        if ("top.zixuan.bluedx".equals(pkg)) {
            try {
                top.zixuan.bluedx.hooks.ModuleStatus.markActive();
                log("markActive done");
            } catch (Throwable t) {
                log("markActive fail " + t);
            }
            return;
        }

        if (!BluedX.isTarget(pkg)) {
            return;
        }
        log("target hit, scheduling hooks");

        // 壳会在启动瞬间扫 hook，这里什么都不做，等它自检完
        try {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    log("stage1 t+3s");
                    try {
                        Class<?> c = Class.forName("top.zixuan.bluedx.hooks.ChatScreenshotProtectHook");
                        c.getMethod("installModelHooksOnly", XC_LoadPackage.LoadPackageParam.class)
                                .invoke(null, lpparam);
                        log("stage1 model hooks ok");
                    } catch (Throwable t) {
                        log("stage1 model fail " + t);
                    }
                    try {
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                log("stage2 t+11s");
                                try {
                                    Class<?> c = Class.forName("top.zixuan.bluedx.hooks.ChatScreenshotProtectHook");
                                    c.getMethod("installSecureOnly", XC_LoadPackage.LoadPackageParam.class)
                                            .invoke(null, lpparam);
                                    log("stage2 secure ok");
                                } catch (Throwable t) {
                                    log("stage2 secure fail " + t);
                                }
                                try {
                                    Class<?> c = Class.forName("top.zixuan.bluedx.hooks.DelayReadHook");
                                    c.getMethod("install", XC_LoadPackage.LoadPackageParam.class)
                                            .invoke(null, lpparam);
                                    log("stage2 delayread ok");
                                } catch (Throwable t) {
                                    log("stage2 delayread fail " + t);
                                }
                                try {
                                    Class<?> c = Class.forName("top.zixuan.bluedx.hooks.SecureFlagHook");
                                    c.getMethod("install", XC_LoadPackage.LoadPackageParam.class)
                                            .invoke(null, lpparam);
                                    log("stage2 secureflag ok");
                                } catch (Throwable t) {
                                    log("stage2 secureflag fail " + t);
                                }
                            }
                        }, 8000L);
                    } catch (Throwable t) {
                        log("stage1 schedule fail " + t);
                    }
                }
            }, 3000L);
        } catch (Throwable t) {
            log("schedule fail " + t);
        }
    }
}
