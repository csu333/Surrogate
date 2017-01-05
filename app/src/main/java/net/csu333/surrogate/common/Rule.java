package net.csu333.surrogate.common;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jp on 4/01/17.
 */

public class Rule implements Parcelable {
    public String clazz;
    public String method;
    public String[] parametersType;
    public String returnType;
    public String returnValue;

    public Rule(){}

    public Rule(String clazz, String method, String[] parameterType, String returnType, String returnValue){
        this.clazz = clazz;
        this.method = method;
        this.parametersType = parameterType;
        this.returnType = returnType;
        this.returnValue = returnValue;
    }

    protected Rule(Parcel in) {
        clazz = in.readString();
        method = in.readString();
        returnType = in.readString();
        returnValue = in.readString();
        parametersType = in.createStringArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(clazz);
        dest.writeString(method);
        dest.writeString(returnType);
        dest.writeString(returnValue);
        dest.writeStringArray(parametersType);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Rule> CREATOR = new Parcelable.Creator<Rule>() {
        @Override
        public Rule createFromParcel(Parcel in) {
            return new Rule(in);
        }

        @Override
        public Rule[] newArray(int size) {
            return new Rule[size];
        }
    };

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("\tClass: ").append(clazz).append("\n")
                .append("\tMethod: ").append(method).append("\n" );

        sb.append("\tParameters type: ");
        if (parametersType == null){
            sb.append("null\n");
        } else {
            sb.append('[');
            for (String type : parametersType){
                sb.append(type).append(',');
            }
            if (sb.length() > 1){
                sb.deleteCharAt(sb.length()-1);
            }
            sb.append(']').append('\n');
        }
        sb.append("\tReturn type: ").append(returnType).append("\n")
            .append("\tReturn value: ").append(returnValue);

        return sb.toString();
    }

}
