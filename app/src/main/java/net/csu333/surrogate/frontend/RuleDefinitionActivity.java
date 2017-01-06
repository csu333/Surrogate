package net.csu333.surrogate.frontend;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.TextView;

import net.csu333.surrogate.R;
import net.csu333.surrogate.common.Rule;

public class RuleDefinitionActivity extends AppCompatActivity {

    private Rule mRule;
    private TextView mClassName;
    private TextView mMethodName;
    private Spinner mReturnType;
    private TextView mReturnValue;
    private TextView mParametersType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_definition);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mClassName = (TextView) findViewById(R.id.clazz);
        mMethodName = (TextView) findViewById(R.id.method);
        mReturnType = (Spinner) findViewById(R.id.return_type);
        mReturnValue = (TextView) findViewById(R.id.return_value);
        mParametersType = (TextView) findViewById(R.id.parameters_type);

        if (getIntent().hasExtra("rule")){
            mRule = getIntent().getParcelableExtra("rule");
        } else {
            mRule = new Rule();
            mRule.returnType = "java.lang.Boolean";
        }

        mClassName.setText(mRule.clazz);
        mMethodName.setText(mRule.method);
        mReturnValue.setText(mRule.returnValue);

        if (mRule.parametersType != null) {
            StringBuilder sb = new StringBuilder();
            for (String parameterType : mRule.parametersType) {
                sb.append(parameterType).append('\n');
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            mParametersType.setText(sb.toString());
        }

        switch (mRule.returnType){
            case "java.lang.Boolean":
                mReturnType.setSelection(0);
                break;
            case "java.lang.Integer":
                mReturnType.setSelection(1);
                break;
            case "java.lang.String":
                mReturnType.setSelection(2);
                break;
            default:
                mReturnType.setSelection(3);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent resultData = new Intent();

        switch (id){
            case R.id.action_save:
                mRule.clazz = mClassName.getText().toString();
                mRule.method = mMethodName.getText().toString();
                mRule.returnType = mReturnType.getSelectedItem().toString();
                mRule.returnValue = mReturnValue.getText().toString();
                mRule.parametersType = mParametersType.getText().toString().split("\n");
                if (mRule.parametersType.length == 1 && "".equals(mRule.parametersType[0])){
                    mRule.parametersType = null;
                }

                resultData.putExtra("result", mRule);
                setResult(Activity.RESULT_OK, resultData);
                finish();
                return true;
            case R.id.action_copy:
                resultData.putExtra("copy", true);
                setResult(Activity.RESULT_OK, resultData);
                finish();
                break;
            case R.id.action_delete:
                resultData.putExtra("delete", true);
                setResult(Activity.RESULT_OK, resultData);
                finish();
            default: break;

        }

        return super.onOptionsItemSelected(item);
    }
}
