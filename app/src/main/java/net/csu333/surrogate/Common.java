package net.csu333.surrogate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by jp on 4/01/17.
 * Inspired from com.devadvance.rootcloak2;
 */

public class Common {

    public static final String PACKAGE_NAME = "net.csu333.surrogate";

    public static abstract class PrefSet {
        abstract String getPrefKey();

        abstract String getSetKey();

        abstract AbstractMap<String, String[][]> getDefaultSet();

        @SuppressLint("WorldReadableFiles")
        public SharedPreferences getSharedPreferences(PreferenceActivity activity) {
            activity.getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            return activity.getSharedPreferences(getPrefKey(), Context.MODE_WORLD_READABLE);
        }
    }

    public static class RuleSet extends PrefSet {
        public static final String PREFS_RULES = "CustomizeRules";
        public static final String RULES_SET_KEY = PACKAGE_NAME + "_RULE_LIST";
        public static final HashMap<String, String[][]>  DEFAULT_RULES_SET = new HashMap<String, String[][]>();

        @Override
        public String getPrefKey() {
            return PREFS_RULES;
        }

        @Override
        public String getSetKey() {
            return RULES_SET_KEY;
        }

        @Override
        public HashMap<String, String[][]> getDefaultSet() {
            return DEFAULT_RULES_SET;
        }

    }


}
