package net.csu333.surrogate.backend;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import net.csu333.surrogate.R;
import net.csu333.surrogate.common.PackageRules;

import java.util.ArrayList;

/**
 * Created by jp on 4/01/17.
 */

public class PackageAdapter extends ArrayAdapter<PackageRules> {

    private RuleBackend mRuleBackend;

    public PackageAdapter(Context context, int resource, ArrayList<PackageRules> packageRulesList, RuleBackend ruleBackend) {
        super(context, resource, packageRulesList);
        mRuleBackend = ruleBackend;
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        PackageRules packageRules = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_package, parent, false);
        }

        // Find fields to populate in inflated template
        TextView packageName = (TextView) convertView.findViewById(R.id.package_name);
        TextView packageId = (TextView) convertView.findViewById(R.id.package_id);
        ImageView packageIcon = (ImageView) convertView.findViewById(R.id.package_icon);

        if (packageRules != null) {
            String friendlyName = null;
            Drawable icon = null;
            if (mRuleBackend != null){
                friendlyName = mRuleBackend.getPackageFriendlyName(packageRules);
                icon = mRuleBackend.getPackageIcon(packageRules);
            }

            packageName.setEnabled(packageRules.enabled);
            packageId.setEnabled(packageRules.enabled);
            packageIcon.setEnabled(packageRules.enabled);

            if (friendlyName != null){
                packageName.setText(friendlyName);
                packageId.setText(packageRules.packageName);
            } else {
                packageName.setText(packageRules.packageName);
                packageId.setVisibility(View.INVISIBLE);
            }

            if (icon != null){
                packageIcon.setImageDrawable(icon);
            }
        }

        return convertView;
    }
}
