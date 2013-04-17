package es.androcode.dashclock.twittermonitor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ConfigActivity extends Activity {
    
    private SharedPreferences mPrefs;
    private TextView mQuery;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mQuery = (TextView) findViewById(R.id.query);
        
        // Coloca el texto que actualmente está guardado
        mQuery.setText(mPrefs.getString(TwitterMonitorExtension.QUERY, "androcode"));
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mPrefs.edit()
                .putString(TwitterMonitorExtension.QUERY, mQuery.getText().toString().trim()) // Guarda el texto para las búsquedas
                // Y elimina las referencias al último tweet consultado y último visto, puesto que la búsqueda es distinta.
                .putLong(TwitterMonitorExtension.ULTIMO_TWEET_CONSULTADO, 0)
                .putLong(TwitterMonitorExtension.ULTIMO_TWEET_VISTO, 0)
                .commit();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_about:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://androcode.es/2013/04/como-hacer-una-extension-para-dashclock-widget")));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
