package net.csu333.surrogate.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jp on 4/01/17.
 */

public class PackageRules implements Comparable <PackageRules>, Parcelable {

    public String packageName;
    public boolean enabled = true;
    public List<Rule> rules = new ArrayList<>();

    public PackageRules(){}

    protected PackageRules(Parcel in) {
        packageName = in.readString();
        enabled = in.readByte() != 0x00;
        if (in.readByte() == 0x01) {
            rules = new ArrayList<>();
            in.readList(rules, Rule.class.getClassLoader());
        } else {
            rules = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeByte((byte) (enabled ? 0x01 : 0x00));
        if (rules == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(rules);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PackageRules> CREATOR = new Parcelable.Creator<PackageRules>() {
        @Override
        public PackageRules createFromParcel(Parcel in) {
            return new PackageRules(in);
        }

        @Override
        public PackageRules[] newArray(int size) {
            return new PackageRules[size];
        }
    };

    @Override
    public int compareTo(PackageRules pr){
        return packageName.compareTo(pr.packageName);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Package Name: ").append(packageName)
                .append(", Enabled: ").append(enabled)
                .append(rules);
        return sb.toString();
    }
}
