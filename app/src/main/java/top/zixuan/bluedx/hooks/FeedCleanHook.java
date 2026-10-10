package top.zixuan.bluedx.hooks;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import top.zixuan.bluedx.util.XLog;

/**
 * 界面精简：隐藏已知的营销 / 推荐卡片。
 * 按 view tag / contentDescription / 资源名模糊匹配，找不到就跳过。
 */
public final class FeedCleanHook {

    private FeedCleanHook() {}

    private static final String[] HIDE_KEYWORDS = {
            "banner_ad", "ad_banner", "layout_ad", "view_ad", "tip_ad",
            "marketing", "promotion", "recommend_header", "live_recommend",
            "vip_guide", "vip_tip", "open_vip", "btn_open_vip"
    };

    public static void install(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Activity act = (Activity) param.thisObject;
                        View root = act.getWindow() != null ? act.getWindow().getDecorView() : null;
                        if (root instanceof ViewGroup) {
                            walk((ViewGroup) root, 0);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            });
            XLog.i("FeedCleanHook installed");
        } catch (Throwable t) {
            XLog.e("FeedCleanHook failed", t);
        }
    }

    private static void walk(ViewGroup group, int depth) {
        if (depth > 12) return;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (shouldHide(child)) {
                child.setVisibility(View.GONE);
                XLog.d("hidden view: " + describe(child));
            }
            if (child instanceof ViewGroup) {
                walk((ViewGroup) child, depth + 1);
            }
        }
    }

    private static boolean shouldHide(View v) {
        try {
            String desc = describe(v).toLowerCase();
            if (desc.isEmpty()) return false;
            for (String kw : HIDE_KEYWORDS) {
                if (desc.contains(kw)) return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static String describe(View v) {
        StringBuilder sb = new StringBuilder();
        try {
            Object tag = v.getTag();
            if (tag != null) sb.append(tag).append(' ');
        } catch (Throwable ignored) {
        }
        try {
            CharSequence cd = v.getContentDescription();
            if (cd != null) sb.append(cd).append(' ');
        } catch (Throwable ignored) {
        }
        try {
            int id = v.getId();
            if (id != View.NO_ID) {
                String name = v.getResources().getResourceEntryName(id);
                sb.append(name);
            }
        } catch (Throwable ignored) {
        }
        return sb.toString().trim();
    }
}
