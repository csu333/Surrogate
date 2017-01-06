package net.csu333.surrogate.frontend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.widget.ShareActionProvider;

import net.csu333.surrogate.R;
import net.csu333.surrogate.backend.PackageAdapter;
import net.csu333.surrogate.backend.RuleBackend;
import net.csu333.surrogate.common.PackageRules;
import net.csu333.surrogate.logic.Helper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity implements ShareActionProvider.OnShareTargetSelectedListener {
    private RuleBackend mBackend;
    private PackageAdapter mPackageAdapter;
    private ListView mPackageList;

    private static final String TAG = RuleBackend.class.getCanonicalName();
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    private final Intent shareIntent=new Intent(Intent.ACTION_SEND);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //noinspection deprecation
        SharedPreferences sharedPreferences = getSharedPreferences(RuleBackend.RULE_SET_KEY, MODE_WORLD_READABLE);
        mBackend = new RuleBackend(sharedPreferences);

        mPackageList = (ListView) findViewById(R.id.package_list);
        mPackageAdapter = new PackageAdapter(this, R.layout.item_package, mBackend.getPackages());
        mPackageList.setAdapter(mPackageAdapter);

        mPackageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), PackageDefinitionActivity.class);
                intent.putExtra("packageRules", (Parcelable) mPackageList.getItemAtPosition(position));
                startActivityForResult(intent, ACTIVITY_EDIT);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), PackageDefinitionActivity.class);
                startActivityForResult(intent, ACTIVITY_CREATE);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTIVITY_CREATE) {
                if (data.hasExtra("result")){
                    PackageRules packageRules = data.getParcelableExtra("result");
                    mBackend.addPackage(packageRules);
                    reloadPackageList();
                }
            } else if (requestCode == ACTIVITY_EDIT) {
                String originalPackageName = data.getStringExtra("originalPackageName");
                if (data.hasExtra("result")){
                    PackageRules packageRules = data.getParcelableExtra("result");
                    if (originalPackageName.equals(packageRules.packageName)) {
                        mBackend.updatePackageRules(packageRules);
                        mPackageAdapter.notifyDataSetChanged();
                    } else {
                        // Package name has changed
                        mBackend.removePackage(originalPackageName);
                        mBackend.addPackage(packageRules);
                        reloadPackageList();
                    }
                } else {
                    // Package has been deleted
                    mBackend.removePackage(originalPackageName);
                    reloadPackageList();
                }
            }
        }
    }

    public void reloadPackageList(){
        mPackageAdapter = new PackageAdapter(this, R.layout.item_package, mBackend.getPackages());
        mPackageList.setAdapter(mPackageAdapter);
        mPackageAdapter.notifyDataSetInvalidated();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share_rule_set);

        // Fetch and store ShareActionProvider
        ShareActionProvider shareActionProvider = new ShareActionProvider(this);
        MenuItemCompat.setActionProvider(item, shareActionProvider);
        shareActionProvider.setOnShareTargetSelectedListener(this);
        prepareShareIntent();
        shareActionProvider.setShareIntent(shareIntent);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        int importedPackages;

        switch (id){
            case R.id.action_import_default_rules_set:
                importedPackages = Helper.importRules(this, mBackend);
                Snackbar.make(this.findViewById(R.id.package_list), "Imported packages: " + importedPackages, Snackbar.LENGTH_LONG).show();

                // Make sure to reflect the new packages
                reloadPackageList();
                return true;
            case R.id.action_import_from_internet:
                new Helper().importRulesFromInternet(this, mBackend);
                return true;
            default: break;

        }

        return super.onOptionsItemSelected(item);
    }

    private boolean prepareShareIntent(){
        File tmp = new File(new File(getApplicationInfo().dataDir), "export.json");
        try {
            PrintWriter writer = new PrintWriter(tmp, "UTF-8");
            writer.print(mBackend.getPackagesAsJson());
            writer.close();

            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+tmp.getAbsolutePath()));
            shareIntent.setType("application/json");
        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
            Snackbar.make(this.findViewById(R.id.package_list), "Failed exporting rules", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
        // Force preparation to get last modification (if any)
        return prepareShareIntent();
    }
}
