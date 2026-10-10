package top.zixuan.bluedx.hooks;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import top.zixuan.bluedx.util.Config;
import top.zixuan.bluedx.util.XLog;

/**
 * WebView 广告 / 埋点拦截。
 * 不依赖具体业务类名，跟着 WebView 走，版本兼容性好。
 */
public final class WebViewAdHook {

    private WebViewAdHook() {}

    /** 广告域名关键词（命中即拦） */
    private static final String[] AD_HOST_KEYWORDS = {
            "ads.", "ad.", "adservice", "adnxs", "doubleclick", "googlesyndication",
            "googleadservices", "umeng", "admaster", "miaozhen", "cnzz", "pos.baidu",
            "tanx.com", "mmstat", "scorecardresearch", "inmobi", "applovin",
            "adsmogo", "domob", "youmi.net", "mobaders", "pangolin", "sigmob",
            "bytedance.com/service/2/app_log", "zijieapi.com/monitor",
            "adx.", "adsafe", "adview", "optimize", "tracking"
    };

    private static final String[] ANALYTICS_HOST_KEYWORDS = {
            "umeng", "umengcloud", "sensorsdata", "talkingdata", "bugly", "crashlytics",
            "sentry.io", "app_log", "analytics", "stat.", "stats.", "log.",
            "report.", "hm.baidu.com", "cnzz.com", "google-analytics", "adjust.com",
            "appsflyer", "growingio", "gio.", "tdsdk", "shence", "xiaoyuan"
    };

    public static void install(XC_LoadPackage.LoadPackageParam lpparam) {
        // WebViewClient.shouldOverrideUrlLoading + shouldInterceptRequest
        hookWebViewClient(lpparam);

        // 新 API: shouldInterceptRequest(WebView, WebResourceRequest)
        hookShouldInterceptRequest(lpparam);

        XLog.i("WebViewAdHook installed");
    }

    private static void hookWebViewClient(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedBridge.hookAllMethods(WebViewClient.class, "shouldInterceptRequest", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    String url = extractUrl(param.args);
                    if (url == null) return;

                    if (shouldBlock(url)) {
                        XLog.d("blocked: " + url);
                        param.setResult(emptyResponse());
                    }
                }
            });
            XLog.i("WebViewClient.shouldInterceptRequest hooked");
        } catch (Throwable t) {
            XLog.e("shouldInterceptRequest hook failed", t);
        }
    }

    private static void hookShouldInterceptRequest(XC_LoadPackage.LoadPackageParam lpparam) {
        // 部分 App 自定义 WebViewClient 子类，用 hookAllMethods 兜住
        try {
            Class<?> wvClass = XposedHelpers.findClass("android.webkit.WebView", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(wvClass, "loadUrl", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    String url = (String) param.args[0];
                    if (url != null && shouldBlock(url)) {
                        XLog.d("loadUrl blocked: " + url);
                        param.args[0] = "about:blank";
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private static String extractUrl(Object[] args) {
        if (args == null || args.length == 0) return null;
        // (WebView, String) 或 (WebView, WebResourceRequest)
        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            }
            if (arg instanceof WebResourceRequest) {
                try {
                    return ((WebResourceRequest) arg).getUrl().toString();
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }

    public static boolean shouldBlock(String url) {
        if (url == null || url.isEmpty()) return false;
        String lower = url.toLowerCase(Locale.ROOT);

        if (Config.blockAds()) {
            for (String kw : AD_HOST_KEYWORDS) {
                if (lower.contains(kw)) return true;
            }
        }
        if (Config.blockAnalytics()) {
            for (String kw : ANALYTICS_HOST_KEYWORDS) {
                if (lower.contains(kw)) return true;
            }
        }
        return false;
    }

    private static WebResourceResponse emptyResponse() {
        return new WebResourceResponse(
                "text/plain",
                "utf-8",
                new ByteArrayInputStream(new byte[0])
        );
    }
}
