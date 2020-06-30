package to.bnt.clipboardtoast;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Module implements IXposedHookLoadPackage {
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("android"))
            return;

        XposedBridge.log("Xposed module initialized!");

        findAndHookMethod("com.android.server.clipboard.ClipboardService.ClipboardImpl", lpparam.classLoader, "getPrimaryClip", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Someone read your clipboard: " + param.args[0]);

                final Context context = AndroidAppHelper.currentApplication();

                PackageManager pm = context.getPackageManager();
                final String appName;
                String appName1;
                try {
                    appName1 = (String) pm.getApplicationLabel(pm.getApplicationInfo((String) param.args[0], 0));
                } catch (final PackageManager.NameNotFoundException e) {
                    appName1 = "Unknown app";
                }

                appName = appName1;

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, appName + " read your clipboard", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
