package top.zixuan.bluedx.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import top.zixuan.bluedx.BluedX;
import top.zixuan.bluedx.R;
import top.zixuan.bluedx.util.Config;

/**
 * BluedX 设置页 — 原生设置风格（浅色、分组列表、系统开关）
 */
public class MainActivity extends Activity {

    private SharedPreferences mPrefs;

    /** 隐藏已读 / 延迟已读 互斥 */
    private android.widget.Switch mHideReadSwitch;
    private android.widget.Switch mDelayReadSwitch;
    private boolean mToggling = false;

    private static final int C_TEXT = 0xFF000000;
    private static final int C_SUB = 0xFF8E8E93;
    private static final int C_PAGE = 0xFFF2F2F7;
    private static final int C_SEP = 0xFFC6C6C8;
    private static final int C_BLUE = 0xFF007AFF;
    private static final int C_GREEN = 0xFF34C759;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = Config.appPrefs(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(C_PAGE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(36));
        scroll.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ===== 大标题 =====
        TextView large = new TextView(this);
        large.setText("BluedX");
        large.setTextSize(32);
        large.setTypeface(Typeface.DEFAULT_BOLD);
        large.setTextColor(C_TEXT);
        large.setPadding(dp(4), dp(16), 0, dp(2));
        root.addView(large);

        TextView sub = new TextView(this);
        sub.setText("Blued / 小蓝 · 设置");
        sub.setTextSize(15);
        sub.setTextColor(C_SUB);
        sub.setPadding(dp(4), 0, 0, dp(20));
        root.addView(sub);

        // ===== 模块状态 =====
        root.addView(header("状态"));
        LinearLayout statusGroup = group();
        TextView statusRow = new TextView(this);
        boolean active = isModuleActive();
        statusRow.setText(active ? "模块已激活" : "模块未激活 — 请在 LSPosed 中启用");
        statusRow.setTextSize(16);
        statusRow.setTextColor(active ? C_GREEN : C_SUB);
        statusRow.setPadding(dp(16), dp(14), dp(16), dp(14));
        statusGroup.addView(statusRow);
        root.addView(statusGroup);

        // ===== 功能 =====
        root.addView(header("功能"));
        LinearLayout feat = group();
        feat.addView(row(feat, getString(R.string.pref_chat_screenshot_protect),
                getString(R.string.pref_chat_screenshot_protect_desc),
                BluedX.KEY_CHAT_SCREENSHOT_PROTECT, true));
        feat.addView(separator());
        feat.addView(row(feat, getString(R.string.pref_unlock_screenshot),
                getString(R.string.pref_unlock_screenshot_desc),
                BluedX.KEY_UNLOCK_SCREENSHOT, true));
        feat.addView(separator());
        feat.addView(row(feat, getString(R.string.pref_block_ads),
                getString(R.string.pref_block_ads_desc),
                BluedX.KEY_BLOCK_ADS, true));
        feat.addView(separator());
        feat.addView(row(feat, getString(R.string.pref_block_analytics),
                getString(R.string.pref_block_analytics_desc),
                BluedX.KEY_BLOCK_ANALYTICS, true));
        feat.addView(separator());
        feat.addView(row(feat, getString(R.string.pref_hide_read),
                getString(R.string.pref_hide_read_desc),
                BluedX.KEY_HIDE_READ, false, true));
        feat.addView(separator());
        feat.addView(row(feat, getString(R.string.pref_delay_read),
                getString(R.string.pref_delay_read_desc),
                BluedX.KEY_DELAY_READ, true, true));
        feat.addView(separator());
        feat.addView(row(feat, getString(R.string.pref_clean_feed),
                getString(R.string.pref_clean_feed_desc),
                BluedX.KEY_CLEAN_FEED, false));
        feat.addView(separator());
        feat.addView(row(feat, getString(R.string.pref_debug_log),
                getString(R.string.pref_debug_log_desc),
                BluedX.KEY_DEBUG_LOG, false));
        root.addView(feat);

        // ===== 使用 =====
        root.addView(header("使用说明"));
        LinearLayout use = group();
        use.addView(plainRow("在 LSPosed 启用 BluedX"));
        use.addView(separator());
        use.addView(plainRow("作用域勾选 com.danlan.xiaolan"));
        use.addView(separator());
        use.addView(plainRow("强行停止小蓝后重新打开"));
        use.addView(separator());
        use.addView(plainRow("日志：adb logcat -s BluedX"));
        root.addView(use);

        // ===== 关于 =====
        root.addView(header("关于"));
        LinearLayout about = group();
        about.addView(plainRow("版本  1.0.0"));
        about.addView(separator());
        about.addView(plainRow("许可  MIT"));
        about.addView(separator());
        about.addView(plainRow("作者  GuiGe101"));
        root.addView(about);

        setContentView(scroll);
    }

    private TextView header(String text) {
        TextView tv = new TextView(this);
        tv.setText(text.toUpperCase());
        tv.setTextSize(12);
        tv.setTextColor(C_SUB);
        tv.setPadding(dp(8), dp(18), 0, dp(6));
        return tv;
    }

    private LinearLayout group() {
        LinearLayout g = new LinearLayout(this);
        g.setOrientation(LinearLayout.VERTICAL);
        g.setBackgroundResource(R.drawable.bg_group);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        g.setLayoutParams(lp);
        return g;
    }

    private View separator() {
        View v = new View(this);
        v.setBackgroundColor(C_SEP);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lp.leftMargin = dp(16);
        v.setLayoutParams(lp);
        return v;
    }

    private TextView plainRow(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(16);
        tv.setTextColor(C_TEXT);
        tv.setPadding(dp(16), dp(13), dp(16), dp(13));
        return tv;
    }

    /** 标题 + 副标题 + 系统 Switch 的标准设置行 */
    private View row(LinearLayout group, String title, String desc,
                     final String key, boolean def) {
        return row(group, title, desc, key, def, false);
    }

    private View row(LinearLayout group, String title, String desc,
                     final String key, boolean def, boolean exclusiveRead) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(10), dp(12), dp(10));
        row.setBackgroundColor(0xFFFFFFFF);
        row.setMinimumHeight(dp(56));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(col, colLp);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(16);
        t.setTextColor(C_TEXT);
        col.addView(t);

        TextView d = new TextView(this);
        d.setText(desc);
        d.setTextSize(12);
        d.setTextColor(C_SUB);
        d.setPadding(0, dp(2), dp(8), 0);
        d.setMaxLines(2);
        col.addView(d);

        final android.widget.Switch sw = new android.widget.Switch(this);
        sw.setChecked(mPrefs.getBoolean(key, def));
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mToggling) return;
            mPrefs.edit().putBoolean(key, isChecked).apply();
            Config.makeReadable(this);
            if (exclusiveRead && isChecked) {
                turnOffOtherRead(key, sw);
            }
        });
        row.addView(sw);

        if (exclusiveRead) {
            if (BluedX.KEY_HIDE_READ.equals(key)) mHideReadSwitch = sw;
            if (BluedX.KEY_DELAY_READ.equals(key)) mDelayReadSwitch = sw;
        }

        final Runnable toggle = () -> {
            sw.setChecked(!sw.isChecked());
        };
        row.setOnClickListener(v -> toggle.run());

        return row;
    }

    /** 互斥：打开其中一个时，关掉另一个 */
    private void turnOffOtherRead(String key, android.widget.Switch self) {
        mToggling = true;
        try {
            if (BluedX.KEY_HIDE_READ.equals(key)) {
                if (mDelayReadSwitch != null) mDelayReadSwitch.setChecked(false);
                mPrefs.edit().putBoolean(BluedX.KEY_DELAY_READ, false).apply();
            } else if (BluedX.KEY_DELAY_READ.equals(key)) {
                if (mHideReadSwitch != null) mHideReadSwitch.setChecked(false);
                mPrefs.edit().putBoolean(BluedX.KEY_HIDE_READ, false).apply();
            }
            Config.makeReadable(this);
        } finally {
            mToggling = false;
        }
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return (int) (v * d + 0.5f);
    }

    private boolean isModuleActive() {
        try {
            return top.zixuan.bluedx.hooks.ModuleStatus.isActive();
        } catch (Throwable t) {
            return false;
        }
    }
}
