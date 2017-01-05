package net.csu333.surrogate.logic;

import android.content.Context;
import android.os.StrictMode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.csu333.surrogate.BuildConfig;
import net.csu333.surrogate.R;
import net.csu333.surrogate.backend.RuleBackend;
import net.csu333.surrogate.common.PackageRules;
import net.csu333.surrogate.common.Rule;

import de.robv.android.xposed.XSharedPreferences;

/**
 * Created by jp on 4/01/17.
 */

public class Helper {
    public static int importRules(Context context, RuleBackend backend){
        InputStream is = context.getResources().openRawResource(R.raw.rules);
        int importedPackages = 0;
        try {
            Reader isr = new InputStreamReader(is, "UTF-8");
            Type type = new TypeToken<List<PackageRules>>(){}.getType();
            List<PackageRules> array = new Gson().fromJson(isr, type);

            backend.addPackages(array);

            importedPackages = array.size();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return importedPackages;
    }

    private static HashMap<String, PackageRules> loadRulesFromPrefs() {
        StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                .permitDiskReads()
                .permitDiskWrites()
                .build());

        final HashMap<String, PackageRules> newRules = new HashMap<String, PackageRules>();
        try {
            final XSharedPreferences loadedPrefs = new XSharedPreferences(BuildConfig.APPLICATION_ID, RuleBackend.RULE_SET_KEY);
            loadedPrefs.makeWorldReadable();

            // Loaded set is IMMUTABLE. We need to copy the values out of it.
            final String loadedRules = loadedPrefs.getString(RuleBackend.RULE_SET_KEY, "[]");
            if (loadedRules != null) {
                Type type = new TypeToken<HashMap<String, ArrayList<Rule>>>(){}.getType();
                newRules.putAll((HashMap<String, PackageRules>)new Gson().fromJson(loadedRules, type));
            }
        } finally {
            StrictMode.setThreadPolicy(old);
        }

        return newRules;
    }
}
