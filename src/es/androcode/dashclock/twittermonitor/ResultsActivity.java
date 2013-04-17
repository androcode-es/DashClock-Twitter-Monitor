package es.androcode.dashclock.twittermonitor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class ResultsActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Marcamos el último tweet consultado como último visto
        long ultimoConsultado = prefs.getLong(TwitterMonitorExtension.ULTIMO_TWEET_CONSULTADO, 0);
        prefs.edit().putLong(TwitterMonitorExtension.ULTIMO_TWEET_VISTO, ultimoConsultado).commit();
        
        // Abrimos la página de twitter, por probar
        String query = prefs.getString(TwitterMonitorExtension.QUERY, "androcode");
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/search?q=" + query)));
    }
}