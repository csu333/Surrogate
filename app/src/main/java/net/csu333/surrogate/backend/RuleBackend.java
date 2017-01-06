package net.csu333.surrogate.backend;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.csu333.surrogate.common.PackageRules;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by jp on 3/01/17.
 */

public class RuleBackend {

    private static final String TAG = RuleBackend.class.getCanonicalName();
    private static PackageManager mPackageManager;
    private final SharedPreferences mPreferences;
    private HashMap<String, PackageRules> mRules = new HashMap<>();
    private HashMap<String, String> mFriendlyNames = new HashMap<>();
    private HashMap<String, Drawable> mIcons = new HashMap<>();

    public static final String RULE_SET_KEY = "RULE_SET";

    public RuleBackend(SharedPreferences preferences){
        mPreferences  = preferences;

        // Load rules from preferences
        Type type = new TypeToken<HashMap<String, PackageRules>>(){}.getType();
        mRules = new Gson().fromJson(mPreferences.getString(RULE_SET_KEY, "[]"), type);
    }

    public void setPackageManager(PackageManager packageManager){
        mPackageManager = packageManager;
    }

    public int addPackages(Iterable<PackageRules> packageRulesList){
        int packageAdded = 0;
        for (PackageRules pr : packageRulesList){
            if (!mRules.containsKey(pr.packageName)) {
                mRules.put(pr.packageName, pr);
                packageAdded++;

                // Enable applications which are installed, disable the others
                pr.enabled = (getPackageFriendlyName(pr) != null);
            }
        }

        savePreferences();

        return packageAdded;
    }

    public void addPackage(PackageRules packageRules){
        mRules.put(packageRules.packageName, packageRules);

        savePreferences();
    }

    public ArrayList<PackageRules> getPackages(){
        ArrayList<PackageRules> ret = new ArrayList<>();
        ret.addAll(mRules.values());

        Collections.sort(ret, new Comparator<PackageRules>() {
            @Override
            public int compare(PackageRules pr1, PackageRules pr2) {
                return pr1.compareTo(pr2);
            }
        });

        return ret;
    }

    public String getPackageFriendlyName(PackageRules packageRule){
        if (mPackageManager != null) {
            if (!mFriendlyNames.containsKey(packageRule.packageName)) {
                ApplicationInfo ai = null;
                try {
                    ai = mPackageManager.getApplicationInfo(packageRule.packageName, PackageManager.GET_META_DATA);
                    mFriendlyNames.put(packageRule.packageName, mPackageManager.getApplicationLabel(ai).toString());
                } catch (PackageManager.NameNotFoundException e) {
                    mFriendlyNames.put(packageRule.packageName, null);
                }
            }

            return mFriendlyNames.get(packageRule.packageName);
        }

        return null;
    }

    public Drawable getPackageIcon(PackageRules packageRule){
        if (mPackageManager != null) {
            if (!mIcons.containsKey(packageRule.packageName)) {
                ApplicationInfo ai = null;
                try {
                    ai = mPackageManager.getApplicationInfo(packageRule.packageName, PackageManager.GET_META_DATA);
                    mIcons.put(packageRule.packageName, mPackageManager.getApplicationIcon(ai));
                } catch (PackageManager.NameNotFoundException e) {
                    mIcons.put(packageRule.packageName, null);
                }
            }

            return mIcons.get(packageRule.packageName);
        }

        return null;
    }

    public String getPackagesAsJson(){
        ArrayList<PackageRules> packages =  getPackages();
        return new Gson().toJson(packages);
    }

    public PackageRules getPackage(String packageName){
        if (mRules.containsKey(packageName))
            return mRules.get(packageName);

        Log.i(TAG, "No package found for: " + packageName);
        return null;
    }

    public void removePackage(String packageName){
        mRules.remove(packageName);
        savePreferences();
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(RULE_SET_KEY, new Gson().toJson(mRules));
        editor.apply();
    }

    public void updatePackageRules(PackageRules packageRules) {
        PackageRules pr = mRules.get(packageRules.packageName);
        pr.enabled = packageRules.enabled;
        pr.rules = packageRules.rules;

        savePreferences();
    }
}
