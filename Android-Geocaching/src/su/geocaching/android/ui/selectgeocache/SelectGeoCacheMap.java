package su.geocaching.android.ui.selectgeocache;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.*;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.ConnectionManager;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import su.geocaching.android.ui.geocachemap.IInternetAware;
import su.geocaching.android.ui.geocachemap.SelectCacheOverlay;
import su.geocaching.android.ui.selectgeocache.geocachegroup.GroupCacheTask;
import su.geocaching.android.ui.selectgeocache.timer.MapUpdateTimer;
import su.geocaching.android.utils.GpsHelper;
import su.geocaching.android.utils.UiHelper;

import java.util.List;

/**
 * @author Yuri Denison
 * @since 04.11.2010
 */
public class SelectGeoCacheMap extends MapActivity implements IInternetAware {
    private static final String TAG = SelectGeoCacheMap.class.getCanonicalName();
    private static final int MAX_CACHE_NUMBER = 100;

    private MyLocationOverlay userOverlay;
    private MapView map;
    private MapController mapController;
    private SelectCacheOverlay selectCacheOverlay;
    private MapUpdateTimer mapTimer;
    private ConnectionManager connectionManager;
    private Activity context;
    private Location currentLocation;
    private ImageView progressBarView;
    private AnimationDrawable progressBarAnimation;
    private int countDownloadTask;
    private Handler handler;
    private GoogleAnalyticsTracker tracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_geocache_map);
        map = (MapView) findViewById(R.id.selectGeocacheMap);
        map.getOverlays().clear();
        mapController = map.getController();

        progressBarView = (ImageView) findViewById(R.id.progressCircle);
        progressBarView.setBackgroundResource(R.anim.earth_anim);
        progressBarAnimation = (AnimationDrawable) progressBarView.getBackground();
        progressBarView.setVisibility(View.GONE);
        countDownloadTask = 0;
        handler = new Handler();

        selectCacheOverlay = new SelectCacheOverlay(Controller.getInstance().getResourceManager().getMarker(new GeoCache()), this, map);
        map.getOverlays().add(selectCacheOverlay);

        connectionManager = Controller.getInstance().getConnectionManager();
        connectionManager.addSubscriber(this);

        context = this;
        askTurnOnInternet();
        userOverlay = new MyLocationOverlay(this, map) {
            @Override
            public void onLocationChanged(Location location) {
                super.onLocationChanged(location);
                currentLocation = location;
            }
        };

        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start(getString(R.string.id_Google_Analytics), this);
        tracker.trackPageView(getString(R.string.select_activity_folder));
        tracker.dispatch();

        map.setBuiltInZoomControls(true);
        map.getOverlays().add(userOverlay);
        map.invalidate();
    }

    private synchronized void updateProgressStart() {
        if (countDownloadTask == 0) {
            LogManager.d(TAG, "set visible Visible for progress");
            handler.post(new Runnable() {
                public void run() {
                    progressBarView.setVisibility(View.VISIBLE);
                }
            });
        }
        LogManager.d(TAG, "count plus. count = " + countDownloadTask);
        countDownloadTask++;
    }

    private synchronized void updateProgressStop() {
        countDownloadTask--;
        LogManager.d(TAG, "count minus. count = " + countDownloadTask);
        if (countDownloadTask == 0) {
            LogManager.d(TAG, "set visible gone for progress");
            handler.post(new Runnable() {
                public void run() {
                    progressBarView.setVisibility(View.GONE);
                }
            });
        }
    }

    private void updateMapInfoFromSettings() {
        int[] lastMapInfo = Controller.getInstance().getLastMapInfo(this);
        GeoPoint lastCenter = new GeoPoint(lastMapInfo[0], lastMapInfo[1]);
        LogManager.d("mapInfo", "X = " + lastMapInfo[0]);
        LogManager.d("mapInfo", "Y = " + lastMapInfo[1]);
        LogManager.d("mapInfo", "zoom = " + lastMapInfo[2]);

        mapController.setCenter(lastCenter);
        mapController.animateTo(lastCenter);
        mapController.setZoom(lastMapInfo[2]);
        map.invalidate();
    }

    private void saveMapInfoToSettings() {
        Controller.getInstance().setLastMapInfo(map.getMapCenter(), map.getZoomLevel(), this);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mapController.setZoom(savedInstanceState.getInt("zoom"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.setKeepScreenOn(Controller.getInstance().getKeepScreenOnPreference(map.getContext()));
        map.setSatellite(!Controller.getInstance().getMapTypeString(map.getContext()).equals("MAP"));

        userOverlay.enableMyLocation();
        selectCacheOverlay.clear();
        map.invalidate();
        updateMapInfoFromSettings();
        mapTimer = new MapUpdateTimer(this);
        updateCacheOverlay();
        map.invalidate();
    }

    @Override
    protected void onPause() {
        userOverlay.disableMyLocation();
        mapTimer.cancel();
        connectionManager.removeSubscriber(this);
        saveMapInfoToSettings();
        tracker.stop();
        LogManager.d("mapInfo", "save on pause");
        super.onPause();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    /**
     * Creating menu object
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.select_geocache_map, menu);
        return true;
    }

    /**
     * Called when menu element selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.revertCenterToLocation:
                if (currentLocation != null) {
                    GeoPoint center = GpsHelper.locationToGeoPoint(currentLocation);
                    mapController.animateTo(center);
                } else {
                    Toast.makeText(getBaseContext(), R.string.status_null_last_location, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.mapSettings:
                startActivity(new Intent(this, MapPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Handles item selections */

    public void updateCacheOverlay() {
        LogManager.d(TAG, "updateCacheOverlay; count = " + countDownloadTask);
        GeoPoint upperLeftCorner = map.getProjection().fromPixels(0, 0);
        GeoPoint lowerRightCorner = map.getProjection().fromPixels(map.getWidth(), map.getHeight());
        Controller.getInstance().updateSelectedGeoCaches(this, upperLeftCorner, lowerRightCorner);
        updateProgressStart();
    }

    public void addGeoCacheList(List<GeoCache> geoCacheList) {
        if (geoCacheList == null || geoCacheList.size() == 0) {
            updateProgressStop();
            return;
        }
        if (geoCacheList.size() > MAX_CACHE_NUMBER) {
            geoCacheList = geoCacheList.subList(0, MAX_CACHE_NUMBER);
        }
        for (GeoCache geoCache : geoCacheList) {
            selectCacheOverlay.addOverlayItem(new GeoCacheOverlayItem(geoCache, "", "", this));
        }
        updateProgressStop();
        map.invalidate();
    }

    public void testAddGeoCacheList(List<GeoCache> geoCacheList) {
        if (geoCacheList == null || geoCacheList.size() == 0) {
            updateProgressStop();
            return;
        }
        new GroupCacheTask(this, geoCacheList).execute();
        updateProgressStop();
    }

    public void addOverlayItemList(List<GeoCacheOverlayItem> overlayItemList) {
        selectCacheOverlay.clear();
        for (GeoCacheOverlayItem item : overlayItemList) {
            selectCacheOverlay.addOverlayItem(item);
        }
        map.invalidate();
    }

    public int getZoom() {
        return map.getZoomLevel();
    }

    public GeoPoint getCenter() {
        return map.getMapCenter();
    }

    @Override
    public void onInternetLost() {
        Toast.makeText(this, getString(R.string.search_geocache_internet_lost), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInternetFound() {
    }

    /**
     * Ask user turn on Internet, if this disabled
     */
    private void askTurnOnInternet() {
        if (connectionManager.isInternetConnected()) {
            LogManager.d(TAG, "Internet connected");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.ask_enable_internet_text)).setCancelable(false)
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent startGPS = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        context.startActivity(startGPS);
                        dialog.cancel();
                    }
                }).setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        AlertDialog turnOnInternetAlert = builder.create();
        turnOnInternetAlert.show();
    }

    /*
      * (non-Javadoc)
      *
      * @see android.app.Activity#onWindowFocusChanged(boolean)
      */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateCacheOverlay();
        }
        progressBarAnimation.start();
    }

    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }

    public MapView getMapView() {
        return map;
    }
}