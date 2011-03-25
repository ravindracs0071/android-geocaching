package su.geocaching.android.ui;

import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.datatype.GeoCache;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

/**
 * Class for create ListActivity with favorites caches
 */
public class FavoritesFolder extends AbstractCacheFolder {

    private static final String TAG = FavoritesFolder.class.getCanonicalName();

    @Override
    protected void onResume() {
        favoritesList = dbm.getArrayGeoCache();
        if (favoritesList.isEmpty()) {
            lvListShowCache.setAdapter(null);
            // tvNoCache.setText(tvNoCache.getText() + "\n" + getString(R.string.favorit_folder_In_DB_not_cache));
            tvNoCache.setText(getString(R.string.favorit_folder_In_DB_not_cache));
            LogManager.d(TAG, "DB empty");
        } else {
            SimpleAdapter simpleAdapter = new SimpleAdapter(this, createGeoCacheList(favoritesList), R.layout.row_in_favorit_rolder, keys, new int[] { R.id.favorite_list_image_button_type,
                    R.id.favorite_list_text_view_name, R.id.favorites_row_type_text, R.id.favorites_row_status_text });

            lvListShowCache.setAdapter(simpleAdapter);
        }
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Intent intent = new Intent(this, ShowGeoCacheInfo.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), favoritesList.get(arg2));
        startActivity(intent);
    }
}
