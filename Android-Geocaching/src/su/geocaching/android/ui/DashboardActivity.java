package su.geocaching.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

/**
 * Main activity in application
 * 
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class DashboardActivity extends Activity {
    
    private static final String TAG = DashboardActivity.class.getCanonicalName();
    private GoogleAnalyticsTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.dashboard_menu);

        Controller.getInstance().initManagers(this.getApplicationContext());

        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start(getString(R.string.id_Google_Analytics), this);
        tracker.trackPageView(getString(R.string.dashboard_activity_folder));
        tracker.dispatch();
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
            case R.id.preference:
                startActivity(new Intent(this, DashboardPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Starting activity to select GeoCache
     */
    public void onSelectClick(View v) {
        tracker.trackEvent("Click", "Button", "from DashBoardActivity to SelesctActivity ", 0);

        Intent intent = new Intent(this, SelectGeoCacheMap.class);
        startActivity(intent);

    }

    /**
     * Starting activity to search GeoCache
     */
    public void onSearchClick(View v) {
        tracker.trackEvent("Click", "Button", "from DashBoardActivity to SearchActivity ", 0);
        if (Controller.getInstance().getPreferencesManager(getApplicationContext()).getLastSearchedGeoCache() == null) {
            Toast.makeText(this.getBaseContext(), getString(R.string.search_geocache_start_without_geocache), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, SearchGeoCacheMap.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), Controller.getInstance().getPreferencesManager(getApplicationContext()).getLastSearchedGeoCache());
        startActivity(intent);

    }

    /**
     * Starting about activity
     */
    public void onAboutClick(View v) {
        tracker.trackEvent("Click", "Button", "from DashBoardActivity to AboutActivity ", 0);
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);

    }

    /**
     * Starting activity with favorites geocaches
     */
    public void onFavoriteClick(View v) {
        tracker.trackEvent("Click", "Button", "from DashBoardActivity to FavoriteActivity ", 0);
        Intent intent = new Intent(this, FavoritesFolder.class);
        startActivity(intent);

    }
}
