package net.csu333.surrogate.backend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import net.csu333.surrogate.R;
import net.csu333.surrogate.common.PackageRules;

import java.util.ArrayList;

/**
 * Created by jp on 4/01/17.
 */

public class PackageAdapter extends ArrayAdapter<PackageRules> {
    public PackageAdapter(Context context, int resource, ArrayList<PackageRules> packageRulesList) {
        super(context, resource, packageRulesList);
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

        if (packageRules != null) {
            packageName.setEnabled(packageRules.enabled);
            packageName.setText(packageRules.packageName);
        }

        return convertView;
    }
}
