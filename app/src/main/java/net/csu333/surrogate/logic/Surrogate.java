package net.csu333.surrogate.logic;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.os.StrictMode;
import android.util.Log;

import net.csu333.surrogate.Common;
import net.csu333.surrogate.backend.RuleBackend;
import net.csu333.surrogate.common.PackageRules;
import net.csu333.surrogate.common.Rule;

import bsh.Interpreter;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XC_MethodReplacement.returnConstant;

/**
 * Created by jp on 4/01/17.
 */

public class Surrogate implements IXposedHookLoadPackage {

    private static final String TAG = Surrogate.class.getCanonicalName();
    private static RuleBackend mBackend;
    private static final String APPLICATION_ID = Common.class.getPackage().getName();


    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        Log.d(TAG, "Loaded app: " + lpparam.packageName);

        if (mBackend == null) {
            StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                    .permitDiskReads()
                    .permitDiskWrites()
                    .build());

            XSharedPreferences pref = new XSharedPreferences(APPLICATION_ID, RuleBackend.RULE_SET_KEY);
            pref.makeWorldReadable();
            pref.reload();
            mBackend = new RuleBackend(pref);
            Log.d(TAG, "Backend loaded");
            StrictMode.setThreadPolicy(old);
        }

        PackageRules packageRules = mBackend.getPackage(lpparam.packageName);
        Log.d(TAG, "PackageRules found: " + packageRules);

        if (packageRules != null && packageRules.enabled){
            Log.d(TAG,"Found enabled package id: " + packageRules.packageName);

            for(Rule rule : packageRules.rules){
                final String returnValue = rule.returnValue;
                Log.d(TAG, "Processing new rule:");
                Object[] parameterTypesAndCallback = new Object[1];
                XC_MethodHook returnMethod;

                Log.d(TAG, "\tClass: " + rule.clazz + "\n" +
                        "\tMethod: " + rule.method + "\n" +
                        //"\tParameters type: " + rule.parametersTypeString + "\n" +
                        "\tReturn type: " + rule.returnType + "\n" +
                        "\tReturn value: " + rule.returnValue + "\n");

                        // Convert parameters type from Strings stored in DB to their actual type
                if (rule.parametersType != null){
                    // Keep last place for return method
                    parameterTypesAndCallback = new Object[rule.parametersType.length + 1];
                    int i = 0;
                    for (String type : rule.parametersType){
                        parameterTypesAndCallback[i++] = Class.forName(type);
                    }
                }

                // Determine what to return
                switch (rule.returnType){
                    case "java.lang.String":
                        returnMethod = returnConstant(rule.returnValue);
                        break;
                    case "java.lang.Integer":
                        returnMethod = returnConstant(Integer.parseInt(rule.returnValue));
                        break;
                    case "java.lang.Boolean":
                        returnMethod = returnConstant(Boolean.parseBoolean(rule.returnValue));
                        break;
                    case "beforeHookedMethod(param)":
                        returnMethod = new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                Interpreter interpreter = new Interpreter();
                                try {
                                    interpreter.set("context", this);//set any variable, you can refer to it directly from string
                                    interpreter.set("param", param);
                                    interpreter.eval(returnValue);//execute code
                                }
                                catch (Exception e){//handle exception
                                    Log.e(TAG, Log.getStackTraceString(e));
                                }
                            }
                        };
                        break;
                    case "afterHookedMethod(param)":
                        returnMethod = new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                Interpreter interpreter = new Interpreter();
                                try {
                                    interpreter.set("context", this);//set any variable, you can refer to it directly from string
                                    interpreter.set("param", param);
                                    interpreter.eval(returnValue);//execute code
                                }
                                catch (Exception e){//handle exception
                                    Log.e(TAG, Log.getStackTraceString(e));
                                }
                            }
                        };
                        break;
                    default:
                        Interpreter interpreter = new Interpreter();
                        try {
                            interpreter.set("context", this);//set any variable, you can refer to it directly from string
                            Object ret = interpreter.eval(rule.returnValue);//execute code
                            if (ret == null) {
                                Log.e(TAG, "Custom return value returned null");
                                returnMethod = returnConstant(rule.returnValue);
                            } else {
                                returnMethod = returnConstant(ret);
                            }
                        }
                        catch (Exception e){//handle exception
                            Log.e(TAG, Log.getStackTraceString(e));
                            returnMethod = returnConstant(rule.returnValue);
                        }
                }

                parameterTypesAndCallback[parameterTypesAndCallback.length - 1] = returnMethod;

                try {
                    findAndHookMethod(rule.clazz, lpparam.classLoader, rule.method, parameterTypesAndCallback);
                    Log.d(TAG, "Method hooked");
                } catch (NoSuchMethodError ex){
                    Log.e(TAG, "Method not found: " + rule.method);
                } catch (XposedHelpers.ClassNotFoundError ex){
                    Log.e(TAG, "Class not found: " + rule.clazz);
                } catch (XposedHelpers.InvocationTargetError ex){
                    Log.e(TAG, "Invocation error: " + Log.getStackTraceString(ex));
                } catch (Exception ex){
                    Log.e(TAG, Log.getStackTraceString(ex));
                }
            }
        }
    }
}
