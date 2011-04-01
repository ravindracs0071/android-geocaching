package su.geocaching.android.ui.searchgeocache;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.selectgeocache.MapIconTypeActivity;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since Apr 1, 2011
 *
 */
public class SearchGeoCacheMapPreferenceActivity extends PreferenceActivity {
    /* (non-Javadoc)
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.search_gc_map_preference);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Preference mapMarkerFilterPreference = findPreference("mapMarkerType");
        mapMarkerFilterPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getBaseContext(), MapIconTypeActivity.class));
                return true;
            }
        });

    }
}