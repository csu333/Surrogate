package net.csu333.surrogate.backend;

import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.csu333.surrogate.common.PackageRules;
import net.csu333.surrogate.common.Rule;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jp on 3/01/17.
 */

public class RuleBackend {

    private static final String TAG = RuleBackend.class.getCanonicalName();
    SharedPreferences mPreferences;
    HashMap<String, PackageRules> mRules = new HashMap<String, PackageRules>();

    public static final String RULE_SET_KEY = "RULE_SET";

    public RuleBackend(SharedPreferences preferences){
        mPreferences  = preferences;

        Log.d(TAG, "New RuleBackend with preferences: " + mPreferences.getString(RULE_SET_KEY, "[]"));

        // Load rules from preferences
        Type type = new TypeToken<HashMap<String, PackageRules>>(){}.getType();
        mRules = new Gson().fromJson(mPreferences.getString(RULE_SET_KEY, "[]"), type);
    }

    public void addPackages(Iterable<PackageRules> packageRulesList){
        for (PackageRules pr : packageRulesList){
            mRules.put(pr.packageName, pr);
        }

        savePreferences();
    }

    public void addPackage(PackageRules packageRules){
        mRules.put(packageRules.packageName, packageRules);

        savePreferences();
    }

    public void addRule(String packageName, String clazz, String method, String[] parametersType, String returnType, String returnValue){
        if (!mRules.containsKey(packageName)) {
            PackageRules packageRules = new PackageRules();
            mRules.put(packageName, packageRules);
        }

        mRules.get(packageName).rules.add(new Rule(clazz, method, parametersType, returnType, returnValue));

        savePreferences();
    }

    public ArrayList<PackageRules> getPackages(){
        ArrayList<PackageRules> ret = new ArrayList<PackageRules>();
        ret.addAll(mRules.values());

        Collections.sort(ret, new Comparator<PackageRules>() {
            @Override
            public int compare(PackageRules pr1, PackageRules pr2) {
                return pr1.compareTo(pr2);
            }
        });

        return ret;
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

    public void enablePackage(String packageName, boolean enabled){
        mRules.get(packageName).enabled = enabled;
        savePreferences();
    }

    public List<Rule> getRules(String packageName){
        if (mRules.containsKey(packageName))
            return mRules.get(packageName).rules;

        return null;
    }

    /*public int updateRule(long ruleId, String clazz, String method, String[] parametersType, String returnType, String returnValue){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        StringBuilder sb = new StringBuilder();
        if (parametersType != null) {
            for (String s : parametersType) {
                sb.append(s).append(',');
            }
            if (sb.length() > 0){
                sb.deleteCharAt(sb.length()-1);
            }
        }

        ContentValues values = new ContentValues();
        values.put(RuleContract.RuleEntry.COLUMN_NAME_CLASS, clazz);
        values.put(RuleContract.RuleEntry.COLUMN_NAME_METHOD, method);
        if (parametersType != null) {
            values.put(RuleContract.RuleEntry.COLUMN_NAME_PARAMETERS_TYPE, sb.toString());
        }
        values.put(RuleContract.RuleEntry.COLUMN_NAME_RETURN_TYPE, returnType);
        values.put(RuleContract.RuleEntry.COLUMN_NAME_RETURN_VALUE, returnValue);

        String selection = RuleContract.RuleEntry._ID + " = ?";

        String[] selectionArgs = { "" + ruleId };

        return db.update(
                RuleContract.RuleEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
    }*/

    /*public int removeRule(long packageId, long ruleId){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        return db.delete(RuleContract.RuleEntry.TABLE_NAME,
                RuleContract.RuleEntry.COLUMN_NAME_PACKAGE_ID + "=? AND " +
                        RuleContract.RuleEntry._ID + "=?", new String[]{ "" + packageId, "" + ruleId });
    }*/

    public void removePackage(String packageName){
        mRules.remove(packageName);
        savePreferences();
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(RULE_SET_KEY, new Gson().toJson(mRules));
        editor.commit();
    }

    public void updatePackageRules(PackageRules packageRules) {
        PackageRules pr = mRules.get(packageRules.packageName);
        pr.enabled = packageRules.enabled;
        pr.rules = packageRules.rules;

        savePreferences();
    }
}
