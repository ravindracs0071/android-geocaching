package su.geocaching.android.ui;

import com.google.android.maps.GeoPoint;

import su.geocaching.android.application.ApplicationMain;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.geocachemap.ConnectionStateReceiver;
import su.geocaching.android.ui.geocachemap.IInternetAware;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheCompass;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;
import su.geocaching.android.view.showgeocacheinfo.Info_cache;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageButton;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 Main menu activity stub
 */
public class MenuActivity extends Activity implements OnClickListener, IInternetAware {

    private static final String TAG = MenuActivity.class.getCanonicalName();

    private Button searchButton;
    private Button selectButton;
    private Button favoritButton;
    private ApplicationMain application;

    private ConnectionStateReceiver internetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Log.d(TAG, "onCreate");

	setContentView(R.layout.dashboard_menu);
	internetManager = new ConnectionStateReceiver(this);
	initButtons();
    }

    private void initButtons() {
	searchButton = (Button) findViewById(R.id.SearchButton);
	selectButton = (Button) findViewById(R.id.SelectButton);
	favoritButton = (Button) findViewById(R.id.FavoritesButton);

	searchButton.setOnClickListener(this);
	selectButton.setOnClickListener(this);
	favoritButton.setOnClickListener(this);

	application = (ApplicationMain) getApplication();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.home_option_menu, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// Handle item selection
	switch (item.getItemId()) {
	case R.id.enableGps:
	    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
	    return true;
	case R.id.enableInternet:
	    startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    @Override
    public void onClick(View v) {
	if (v.equals(searchButton)) {
	    startSearchGeoCache();
	} else if (v.equals(selectButton)) {
	    startSelectGeoCache();
	} else if (v.equals(favoritButton)) {
	    startFavoriteFolder();
	}
    }

    /**
     * Starting activity to search GeoCache
     */
    private void startSearchGeoCache() {
	if (application.getDesiredGeoCache() == null) {
	    Toast.makeText(this.getBaseContext(), getString(R.string.search_geocache_start_without_geocache), Toast.LENGTH_SHORT).show();
	    return;
	}
	Intent intent = new Intent(this, SearchGeoCacheMap.class);
	intent.putExtra(GeoCache.class.getCanonicalName(), application.getDesiredGeoCache());
	startActivity(intent);
    }

    /**
     * Starting activity to select GeoCache
     */
    private void startSelectGeoCache() {
	if (internetManager.isInternetConnected()) {
	    Intent intent = new Intent(this, SelectGeoCacheMap.class);
	    startActivity(intent);
	} else {
	    Toast.makeText(this.getBaseContext(), "Сеть не найдена. Попробуйте позже", Toast.LENGTH_SHORT).show();
	}
    }

    private void startFavoriteFolder() {
	Intent intent = new Intent(this, su.geocaching.android.view.favoritwork.FavoritFolder.class);
	startActivity(intent);
    }

    @Override
    public void onInternetLost() {
	// TODO update internet icon to "offline"
    }

    @Override
    public void onInternetFound() {
	// TODO update internet icon to "online"
    }
}
