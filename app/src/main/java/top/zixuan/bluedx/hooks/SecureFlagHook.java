package top.zixuan.bluedx.hooks;

import android.view.Window;
import android.view.WindowManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import top.zixuan.bluedx.util.XLog;

/**
 * 允许截图：去掉窗口的 FLAG_SECURE。
 * 很多聊天 / 直播界面会打这个 flag 阻止截图和录屏。
 */
public final class SecureFlagHook {

    private SecureFlagHook() {}

    public static void install(XC_LoadPackage.LoadPackageParam lpparam) {
        // Window.setFlags / addFlags
        try {
            XposedHelpers.findAndHookMethod(Window.class, "setFlags", int.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    int flags = (int) param.args[0];
                    int mask = (int) param.args[1];
                    if ((mask & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                        flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                        mask &= ~WindowManager.LayoutParams.FLAG_SECURE;
                        param.args[0] = flags;
                        param.args[1] = mask;
                        XLog.d("cleared FLAG_SECURE via setFlags");
                    }
                }
            });
            XLog.i("Window.setFlags hooked");
        } catch (Throwable t) {
            XLog.e("Window.setFlags hook failed", t);
        }

        try {
            XposedHelpers.findAndHookMethod(Window.class, "addFlags", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    int flags = (int) param.args[0];
                    if ((flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                        param.args[0] = flags & ~WindowManager.LayoutParams.FLAG_SECURE;
                        XLog.d("cleared FLAG_SECURE via addFlags");
                    }
                }
            });
            XLog.i("Window.addFlags hooked");
        } catch (Throwable t) {
            XLog.e("Window.addFlags hook failed", t);
        }

        // WindowManager.updateViewLayout 路径
        try {
            XposedHelpers.findAndHookMethod(WindowManager.class, "updateViewLayout",
                    android.view.View.class, android.view.ViewGroup.LayoutParams.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            Object lp = param.args[1];
                            if (lp instanceof WindowManager.LayoutParams) {
                                WindowManager.LayoutParams params = (WindowManager.LayoutParams) lp;
                                if ((params.flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                                    params.flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
                                    XLog.d("cleared FLAG_SECURE via updateViewLayout");
                                }
                            }
                        }
                    });
        } catch (Throwable t) {
            // 某些 ROM 该方法签名不同，忽略
        }

        // SurfaceView.setSecure —— 视频/直播常用
        try {
            XposedHelpers.findAndHookMethod("android.view.SurfaceView", lpparam.classLoader,
                    "setSecure", boolean.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.args[0] = false;
                            XLog.d("SurfaceView.setSecure forced false");
                        }
                    });
        } catch (Throwable t) {
            XLog.d("SurfaceView.setSecure not found");
        }
    }
}
