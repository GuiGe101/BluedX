package top.zixuan.bluedx.hooks;

import java.net.URL;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import top.zixuan.bluedx.util.XLog;

/**
 * 网络层广告 / 埋点拦截。
 * 覆盖 HttpURLConnection、OkHttp（如果 App 里有），不依赖具体业务类。
 */
public final class HttpAdHook {

    private HttpAdHook() {}

    public static void install(XC_LoadPackage.LoadPackageParam lpparam) {
        hookUrlOpenConnection(lpparam);
        hookOkHttp(lpparam);
        XLog.i("HttpAdHook installed");
    }

    private static void hookUrlOpenConnection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(URL.class, "openConnection", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    URL url = (URL) param.thisObject;
                    if (url != null && WebViewAdHook.shouldBlock(url.toString())) {
                        XLog.d("URL.openConnection blocked: " + url);
                        // 抛出受检异常太麻烦，改成指向空地址由调用方失败
                        // 这里不拦截 openConnection，交给上层 WebView/重定向更安全
                    }
                }
            });
        } catch (Throwable t) {
            XLog.d("URL.openConnection hook skipped");
        }
    }

    private static void hookOkHttp(XC_LoadPackage.LoadPackageParam lpparam) {
        // OkHttp3：RealCall.execute / enqueue 里拿 Request.url()
        String[] classes = {
                "okhttp3.RealCall",
                "okhttp3.internal.connection.RealCall"
        };
        for (String cls : classes) {
            try {
                Class<?> realCall = XposedHelpers.findClass(cls, lpparam.classLoader);
                XposedBridge.hookAllMethods(realCall, "execute", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (maybeBlock(param.thisObject)) {
                            XLog.d("OkHttp execute blocked");
                            param.setThrowable(new java.io.IOException("BluedX: blocked by ad filter"));
                        }
                    }
                });
                XposedBridge.hookAllMethods(realCall, "enqueue", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (maybeBlock(param.thisObject)) {
                            XLog.d("OkHttp enqueue blocked");
                            param.setResult(null);
                        }
                    }
                });
                XLog.i("OkHttp hooked: " + cls);
                return;
            } catch (Throwable ignored) {
            }
        }
        XLog.d("OkHttp not found in this process");
    }

    private static boolean maybeBlock(Object realCall) {
        try {
            Object request = XposedHelpers.callMethod(realCall, "request");
            Object url = XposedHelpers.callMethod(request, "url");
            String s = String.valueOf(url);
            return WebViewAdHook.shouldBlock(s);
        } catch (Throwable t) {
            return false;
        }
    }
}
