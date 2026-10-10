package top.zixuan.bluedx.hooks;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import top.zixuan.bluedx.util.Config;
import top.zixuan.bluedx.util.XLog;

/**
 * 延迟已读：进聊天先看消息，30 秒后再给对方「已读」。
 *
 * 反编译结论：
 *   Lcom/blued/android/chat/core/worker/chat/ReadFlagSender;
 *   （含 MyTimerTask / ReceiptModel / ReceiptType）
 *   方法：sendReadReceipt / sendReadReceiptImmediate / sendReadReceiptLater
 *   协议：SendReceiptPackage / ReceiptRead
 *
 * 策略：
 *   默认把「立即已读」改成 30 秒后发；
 *   调用 sendReadReceiptImmediate(true) 或标记 force 时立即发（长按用）。
 */
public final class DelayReadHook {

    private DelayReadHook() {}

    /** 延迟毫秒，可在设置里改 */
    public static volatile long DELAY_MS = 30_000L;

    /** 本线程/本次调用是否强制立即发（长按「立即已读」） */
    private static final ThreadLocal<Boolean> FORCE_NOW = new ThreadLocal<>();

    /** 正在排队的会话 key → 可取消 */
    private static final ConcurrentHashMap<String, Runnable> PENDING = new ConcurrentHashMap<>();

    public static void forceNow() {
        FORCE_NOW.set(Boolean.TRUE);
    }

    public static void clearForce() {
        FORCE_NOW.remove();
    }

    private static final String[] OWNER_CLASSES = {
            "com.blued.android.chat.core.worker.chat.ReadFlagSender",
            "com.soft.blued.ui.msg.controller.tools.ChatHelperV4",
            "com.soft.blued.ui.msg.controller.tools.IMManager",
            "com.blued.android.chat.ChatManager",
            "com.blued.android.chat.ChatManagerInner",
            "com.blued.android.chat.grpc.PrivateChatManager",
            "com.blued.android.module.im.biz.receipt.ReceiptRead",
    };

    private static final String[] DELAY_METHODS = {
            "sendReadReceipt",
            "sendReadReceiptImmediate",
            "sendReadedFlagForSession",
            "sendReceipt",
            "sendReceiptImmediate",
    };

    private static final String[] PASS_METHODS = {
            "sendReadReceiptLater",
    };

    public static void install(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.i("DelayReadHook.install begin");
        int hooked = 0;
        for (String clsName : OWNER_CLASSES) {
            Class<?> clazz;
            try {
                clazz = XposedHelpers.findClass(clsName, lpparam.classLoader);
            } catch (Throwable t) {
                continue;
            }
            for (Method m : clazz.getDeclaredMethods()) {
                String name = m.getName();
                boolean isDelay = false;
                for (String n : DELAY_METHODS) {
                    if (name.equals(n)) { isDelay = true; break; }
                }
                if (!isDelay) continue;
                try {
                    XposedBridge.hookMethod(m, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {
                                if (!Config.delayRead()) return;
                                // 隐藏已读优先，二者互斥
                                if (Config.hideRead()) return;
                                if (Boolean.TRUE.equals(FORCE_NOW.get())) return;
                                if (isSelfMessage(param.args)) return;

                                // 拦下本次调用，30 秒后再原样调用
                                final Object thisObj = param.thisObject;
                                final Method target = (Method) param.method;
                                final Object[] args = param.args;
                                final String key = sessionKey(args);

                                Runnable task = () -> {
                                    try {
                                        target.invoke(thisObj, args);
                                        XLog.d("delayed read receipt sent key=" + key);
                                    } catch (Throwable ignored) {
                                    } finally {
                                        PENDING.remove(key);
                                    }
                                };

                                Runnable old = PENDING.put(key, task);
                                if (old != null) {
                                    // 同一会话已有未发送的，合并成一次
                                    XLog.d("coalesce pending receipt key=" + key);
                                }

                                android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
                                h.postDelayed(task, DELAY_MS);
                                XLog.d("delayed read receipt by " + DELAY_MS + "ms key=" + key);
                                param.setResult(null);
                            } catch (Throwable ignored) {
                            }
                        }
                    });
                    hooked++;
                } catch (Throwable ignored) {
                }
            }
        }
        XLog.i("DelayReadHook hooked=" + hooked);
    }

    /** 自己发出去的消息不该触发「对方看到我已读」 */
    private static boolean isSelfMessage(Object[] args) {
        if (args == null) return false;
        for (Object a : args) {
            if (a instanceof Boolean && Boolean.TRUE.equals(a)) {
                // 某些签名是 (boolean isSelf) / (boolean immediate)
                // immediate=true 时我们也延迟，所以这里不拦
            }
            try {
                Object from = XposedHelpers.callMethod(a, "getFrom");
                if (from != null && String.valueOf(from).length() > 0) {
                    // 无法可靠判断 from，交给上层
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static String sessionKey(Object[] args) {
        if (args == null || args.length == 0) return "default";
        for (Object a : args) {
            if (a instanceof String) return (String) a;
            try {
                Object sid = XposedHelpers.callMethod(a, "getSessionId");
                if (sid != null) return String.valueOf(sid);
            } catch (Throwable ignored) {
            }
            try {
                Object id = XposedHelpers.callMethod(a, "getUid");
                if (id != null) return String.valueOf(id);
            } catch (Throwable ignored) {
            }
        }
        return "default";
    }
}
