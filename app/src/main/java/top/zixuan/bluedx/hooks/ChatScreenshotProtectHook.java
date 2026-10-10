package top.zixuan.bluedx.hooks;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import top.zixuan.bluedx.util.Config;
import top.zixuan.bluedx.util.XLog;

/**
 * 聊天截屏保护 — 强制打开小蓝/Blued 自带的「聊天截屏保护」。
 *
 * 字段：is_prohibit_chat_screenshot
 * 接口：/users/prohibit/chatScreenshotConfig
 *
 * 注意：返回值类型可能是 boolean / int / Integer，必须按方法签名给值，
 * 否则会在加固壳里直接把进程打死。
 */
public final class ChatScreenshotProtectHook {

    private ChatScreenshotProtectHook() {}

    private static final String[] MODEL_CLASSES = {
            "com.blued.android.modules.UserChatScreenshotMode",
            "com.blued.android.modules.ChatScreenshotInfoMode",
    };

    private static final String[] GETTER_NAMES = {
            "getIs_prohibit_chat_screenshot",
            "is_prohibit_chat_screenshot",
            "getIsProhibitChatScreenshot",
            "isProhibitChatScreenshot",
    };

    private static final String[] SETTER_NAMES = {
            "setIs_prohibit_chat_screenshot",
            "setIsProhibitChatScreenshot",
    };

    public static void install(XC_LoadPackage.LoadPackageParam lpparam) {
        installModelHooksOnly(lpparam);
        installSecureOnly(lpparam);
        XLog.i("ChatScreenshotProtectHook installed");
    }

    /** 只 Hook 业务模型字段，不碰系统框架（降低被壳检测概率） */
    public static void installModelHooksOnly(XC_LoadPackage.LoadPackageParam lpparam) {
        hookGetters(lpparam);
        hookSetters(lpparam);
        XLog.i("ChatScreenshotProtect model hooks installed");
    }

    /** 聊天页 FLAG_SECURE（会 Hook Activity，延后调用） */
    public static void installSecureOnly(XC_LoadPackage.LoadPackageParam lpparam) {
        hookActivitySecure(lpparam);
    }

    /** 按 Method.getReturnType 给「开启」值，避免类型不匹配崩进程 */
    private static Object trueValueFor(Class<?> ret) {
        if (ret == boolean.class || ret == Boolean.class) return Boolean.TRUE;
        if (ret == int.class || ret == Integer.class) return 1;
        if (ret == long.class || ret == Long.class) return 1L;
        if (ret == String.class) return "1";
        return Boolean.TRUE;
    }

    private static Object coerceArg(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) return Boolean.TRUE;
        if (type == int.class || type == Integer.class) return 1;
        if (type == long.class || type == Long.class) return 1L;
        return Boolean.TRUE;
    }

    private static void hookGetters(XC_LoadPackage.LoadPackageParam lpparam) {
        for (String clsName : MODEL_CLASSES) {
            Class<?> clazz;
            try {
                clazz = XposedHelpers.findClass(clsName, lpparam.classLoader);
            } catch (Throwable t) {
                XLog.d("class not found: " + clsName);
                continue;
            }
            for (String mName : GETTER_NAMES) {
                for (Method m : clazz.getDeclaredMethods()) {
                    if (!m.getName().equals(mName)) continue;
                    try {
                        final Class<?> ret = m.getReturnType();
                        XposedBridge.hookMethod(m, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                param.setResult(trueValueFor(ret));
                            }
                        });
                        XLog.d("hooked getter " + clsName + "#" + mName + " ret=" + ret.getSimpleName());
                    } catch (Throwable t) {
                        XLog.d("skip getter " + clsName + "#" + mName);
                    }
                }
            }
        }
    }

    private static void hookSetters(XC_LoadPackage.LoadPackageParam lpparam) {
        for (String clsName : MODEL_CLASSES) {
            Class<?> clazz;
            try {
                clazz = XposedHelpers.findClass(clsName, lpparam.classLoader);
            } catch (Throwable t) {
                continue;
            }
            for (String mName : SETTER_NAMES) {
                for (Method m : clazz.getDeclaredMethods()) {
                    if (!m.getName().equals(mName)) continue;
                    Class<?>[] params = m.getParameterTypes();
                    if (params.length != 1) continue;
                    try {
                        final Class<?> p0 = params[0];
                        XposedBridge.hookMethod(m, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                try {
                                    param.args[0] = coerceArg(p0);
                                } catch (Throwable ignored) {
                                }
                            }
                        });
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    }

    /**
     * 聊天相关页面补 FLAG_SECURE。
     * 只 hook Activity.onResume，且失败不影响启动。
     */
    private static void hookActivitySecure(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        if (!Config.chatScreenshotProtect()) return;
                        Activity act = (Activity) param.thisObject;
                        if (act == null) return;
                        String name = act.getClass().getName();
                        if (!isChatLike(name)) return;
                        Window w = act.getWindow();
                        if (w != null) {
                            w.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable t) {
            XLog.e("chat FLAG_SECURE hook failed", t);
        }
    }

    private static boolean isChatLike(String className) {
        if (className == null) return false;
        String name = className.toLowerCase();
        return name.contains("chat") || name.contains("msg") || name.contains("message")
                || name.contains("im.") || name.contains("conversation");
    }
}
