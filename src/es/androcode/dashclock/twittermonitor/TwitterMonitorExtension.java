package es.androcode.dashclock.twittermonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class TwitterMonitorExtension extends DashClockExtension {
    
    // Clave de las Shared Preferences, para hacer la búsqueda a partir del último tweet que hemos visto
    public static final String ULTIMO_TWEET_VISTO = "last_tweet_viewed_id";
    
    // Clave de las Shared Preferences, para guardar como último tweet visto al abrir la extensión el tweet más reciente descargado
    public static final String ULTIMO_TWEET_CONSULTADO = "last_tweet_fetched_id";
    
    // Clave de las Shared Preferences, almacena el texto que queremos buscar
    public static final String QUERY = "query";
    
    // Acción del Intent que abre los resultados al pulsar la extensión
    public static final String ACTION_RESULTS = "es.androcode.dashclocktwittersearch.OPEN";
    
    // Máximo de Tweets que se van a buscar
    public static final int MAX_TWEETS = 50;
    
    private SharedPreferences mPrefs;
    
    @Override
    protected void onInitialize(boolean isReconnect) {
        // Aquí llamaríamos a setUpdateWhenScreenOn(boolean) o addWatchContentUris(String[]) si quisiéramos usar dichas funciones.
        // En este caso no lo hacemos
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }
    
    @Override
    protected void onUpdateData(int reason) {
        String busqueda = mPrefs.getString(QUERY, "androcode");
        
        // Creamos el objeto ExtensionData que rellenaremos con los datos que queremos mostrar en el Widget
        ExtensionData data = new ExtensionData();
        data.icon(R.drawable.ic_extension);
        data.clickIntent(new Intent(ACTION_RESULTS));
        
        try {
            // Hacemos la búsqueda en Twitter. Esto es independiente de nuestra extensión
            JSONArray tweets = getNewTweets(busqueda);
            Integer cantidad = tweets.length();
            String cantidadMostrar = (cantidad <= MAX_TWEETS) ? cantidad.toString() : MAX_TWEETS + "+";
            
            // Ahora sí, configuramos los datos del Widget
            data.status(cantidadMostrar);
            data.expandedTitle(getResources().getQuantityString(R.plurals.extension_content_title, cantidad, cantidadMostrar));
            data.expandedBody(String.format(getString(R.string.extension_content_body), busqueda));
            if (cantidad < 1) {
                data.visible(false); // Mientras no haya información ocultamos la extensión para dejar sitio a otras
            } else {
                data.visible(true);
            }
        } catch (Exception e) {
            // Normalmente haríamos otra cosa al capturar la excepción, pero esto viene bien para debug
            data.visible(true)
                    .expandedTitle(e.getClass().getCanonicalName())
                    .expandedBody(e.getMessage())
                    .status("ERROR");
        }
        
        // Y finalmente le decimos al Widget que actualice los datos
        publishUpdate(data);
    }
    
    /**
     * Realiza la búsqueda mediante la API REST pública de Twitter, y extrae los resultados como array JSON.
     * La documentación de la API se encuentra en https://dev.twitter.com/docs/api/1/get/search
     * La versión v1 de la API está obsoleta y éste método se debería transformar para usar la nueva API con autenticación.
     */
    private JSONArray getNewTweets(String busqueda) throws JSONException, UnsupportedEncodingException {
        long lastTweetId = mPrefs.getLong(ULTIMO_TWEET_VISTO, 0);
        String url = "http://search.twitter.com/search.json?rpp=" + (MAX_TWEETS + 1) + "&q=" + URLEncoder.encode(busqueda, "utf-8") + "&since_id=" + lastTweetId;
        JSONObject result = new JSONObject(readURL(url));
        
        long maxId = result.getLong("max_id");
        mPrefs.edit().putLong(ULTIMO_TWEET_CONSULTADO, maxId).commit();
        
        JSONArray tweets = result.getJSONArray("results");
        return tweets;
    }
    
    /**
     * Método copiado de http://androcode.es/2012/05/consumir-datos-de-una-url-en-android/
     */
    public String readURL(String url) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = client.execute(httpGet);
            
            StatusLine statusLine = response.getStatusLine();
            
            int statusCode = statusLine.getStatusCode();
            
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(this.getClass().getName(), "Failed to download data");
            }
            
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return builder.toString();
        
    }
    
}
