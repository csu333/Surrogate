package net.csu333.surrogate.frontend;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import net.csu333.surrogate.R;
import net.csu333.surrogate.common.PackageRules;
import net.csu333.surrogate.common.Rule;

public class PackageDefinitionActivity extends AppCompatActivity {

    private PackageRules mPackageRules;
    private MenuItemImpl mEnabled;
    private String mOriginalPackageName;
    private int mLastRuleIndex;
    private ArrayAdapter<Rule> mRuleAdapter;

    private final static int ACTIVITY_CREATE = 0;
    private final static int ACTIVITY_EDIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_definition);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent().hasExtra("packageRules")) {
            mPackageRules = getIntent().getParcelableExtra("packageRules");
        } else {
            mPackageRules = new PackageRules();
        }

        findViewById(R.id.action_save);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), RuleDefinitionActivity.class);
                startActivityForResult(intent, ACTIVITY_CREATE);
            }
        });

        EditText packageName = (EditText) findViewById(R.id.package_name);
        packageName.setText(mPackageRules.packageName);
        mOriginalPackageName = mPackageRules.packageName;

        mRuleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mPackageRules.rules);
        final ListView ruleList = (ListView) findViewById(R.id.rule_list);
        ruleList.setAdapter(mRuleAdapter);
        ruleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), RuleDefinitionActivity.class);
                intent.putExtra("rule", mPackageRules.rules.get(position));
                mLastRuleIndex = position;
                startActivityForResult(intent, ACTIVITY_EDIT);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTIVITY_CREATE) {
                if (data.hasExtra("result")) {
                    Rule r = data.getParcelableExtra("result");
                    mPackageRules.rules.add(r);
                    mRuleAdapter.notifyDataSetChanged();
                }
            } else if (requestCode == ACTIVITY_EDIT) {
                if (data.hasExtra("copy")){
                    Rule r = mPackageRules.rules.get(mLastRuleIndex);
                    mPackageRules.rules.add(r);
                    Intent intent = new Intent(getBaseContext(), RuleDefinitionActivity.class);
                    intent.putExtra("rule", r);
                    mLastRuleIndex = mPackageRules.rules.size() - 1;
                    startActivityForResult(intent, ACTIVITY_EDIT);
                } else if (data.hasExtra("delete")) {
                    mPackageRules.rules.remove(mLastRuleIndex);
                    mRuleAdapter.notifyDataSetChanged();
                } else if (data.hasExtra("result")){
                    Rule r = data.getParcelableExtra("result");
                    mPackageRules.rules.set(mLastRuleIndex, r);
                    mRuleAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_package, menu);
        mEnabled = (MenuItemImpl) menu.findItem(R.id.action_enable);

        if (mPackageRules != null && !mPackageRules.enabled){
            mEnabled.setIcon(R.drawable.sync_off);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        EditText packageName = (EditText) findViewById(R.id.package_name);
        Intent resultData = new Intent();

        switch (id){
            case R.id.action_enable:
                mPackageRules.enabled = !mPackageRules.enabled;
                if (mPackageRules.enabled){
                    mEnabled.setIcon(R.drawable.sync);
                } else {
                    mEnabled.setIcon(R.drawable.sync_off);
                }
                return true;
            case R.id.action_save:
                mPackageRules.packageName = packageName.getText().toString();
                resultData.putExtra("originalPackageName", mOriginalPackageName);
                resultData.putExtra("result", mPackageRules);
                setResult(Activity.RESULT_OK, resultData);
                finish();
                return true;
            case R.id.action_delete:
                mPackageRules.packageName = packageName.getText().toString();
                resultData.putExtra("originalPackageName", mOriginalPackageName);
                setResult(Activity.RESULT_OK, resultData);
                finish();
                return true;
            default: break;

        }

        return super.onOptionsItemSelected(item);
    }
}
