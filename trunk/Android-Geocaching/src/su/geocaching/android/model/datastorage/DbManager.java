package su.geocaching.android.model.datastorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;

import java.util.ArrayList;

/**
 * This class contains method for working with database.
 * 
 * @author Alekseenko Vladimir
 */
public class DbManager extends SQLiteOpenHelper {
    private static final String TAG = DbManager.class.getCanonicalName();

    // Name, version and name table
    private static final String DATABASE_NAME_BASE = "CacheBase.db";
    private static final String DATABASE_NAME_TABLE = "cache";
    private static final String DATABASE_CHECKPOINT_NAME_TABLE = "chekpoints";
    private static final int DATABASE_VERSION = 3;
    // Name column database
    private static final String COLUMN_ID = "cid";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_WEB_TEXT = "text";
    private static final String COLUMN_NOTEBOOK_TEXT = "notetext";
    private static final String COLUMN_LON = "longtitude";
    private static final String COLUMN_LAT = "lantitude";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_STATUS = "status";
    private static final String CACHE_ID = "cache_id";
    private static final String CHECKPOINT_ID = "checkpoint_id";
    private SQLiteDatabase db = null;

    private static final String SQL_CREATE_DATABASE_TABLE = String.format("create table %s (%s integer, %s string, %s integer,%s integer, %s integer, %s integer, %s string, %s string);",
            DATABASE_NAME_TABLE, COLUMN_ID, COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, COLUMN_WEB_TEXT, COLUMN_NOTEBOOK_TEXT);
    private static final String SQL_CREATE_DATABASE_CHECKPOINT_TABLE = String.format(
            "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER, %s STRING, %s INTEGER, %s INTEGER, %s INTEGER);", DATABASE_CHECKPOINT_NAME_TABLE, COLUMN_ID, CACHE_ID,
            CHECKPOINT_ID, COLUMN_NAME, COLUMN_LAT, COLUMN_LON, COLUMN_STATUS);

    /**
     * @param context
     *            this activivty
     */
    public DbManager(Context context) {
        super(context, DATABASE_NAME_BASE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            // Create tables
            db.execSQL(SQL_CREATE_DATABASE_TABLE);
            db.execSQL(SQL_CREATE_DATABASE_CHECKPOINT_TABLE);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            LogManager.e(TAG, e.toString(), e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.beginTransaction();
            try {
                db.execSQL(String.format("ALTER TABLE %s ADD %s string;", DATABASE_NAME_TABLE, COLUMN_NOTEBOOK_TEXT));
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                LogManager.e(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
            }
        }
        if (oldVersion < 3) {
            db.beginTransaction();
            try {
                db.execSQL(SQL_CREATE_DATABASE_CHECKPOINT_TABLE);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                LogManager.e(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
            }
        }
    }

    /**
     * @return ArrayList GeoCaches in database. Null if in database haven't GeoCache
     */
    public ArrayList<GeoCache> getArrayGeoCache() {
        ArrayList<GeoCache> exitCollection = new ArrayList<GeoCache>();
        openDB();
        Cursor cur = db.rawQuery(String.format("select %s,%s,%s,%s,%s,%s from %s", COLUMN_ID, COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, DATABASE_NAME_TABLE), null);

        cur.moveToFirst();

        while (!cur.isAfterLast()) {

            GeoCache geocache = new GeoCache();
            geocache.setId(cur.getInt(cur.getColumnIndex(COLUMN_ID)));
            geocache.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
            geocache.setStatus(GeoCacheStatus.values()[cur.getInt(cur.getColumnIndex(COLUMN_STATUS))]);
            geocache.setLocationGeoPoint(new GeoPoint(cur.getInt(cur.getColumnIndex(COLUMN_LAT)), cur.getInt(cur.getColumnIndex(COLUMN_LON))));
            geocache.setType(GeoCacheType.values()[cur.getInt(cur.getColumnIndex(COLUMN_TYPE))]);
            exitCollection.add(geocache);

            cur.moveToNext();
        }

        cur.close();
        closeDB();
        return exitCollection;
    }

    /**
     * @param id
     *            id of GeoCache
     * @return list of checkpoints corresponding to a given cache
     */
    public ArrayList<GeoCache> getCheckpointsArrayById(int id) {
        LogManager.d(TAG, "getCheckpointsArrayById " + id);
        ArrayList<GeoCache> exitCollection = new ArrayList<GeoCache>();
        db = getReadableDatabase();
        Cursor cur = db.rawQuery(
                String.format("SELECT %s,%s,%s,%s,%s FROM %s WHERE %s=%d", CHECKPOINT_ID, COLUMN_NAME, COLUMN_LAT, COLUMN_LON, COLUMN_STATUS, DATABASE_CHECKPOINT_NAME_TABLE, CACHE_ID, id), null);

        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            GeoCache geocache = new GeoCache();
            geocache.setId(cur.getInt(cur.getColumnIndex(CHECKPOINT_ID)));
            geocache.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
            geocache.setLocationGeoPoint(new GeoPoint(cur.getInt(cur.getColumnIndex(COLUMN_LAT)), cur.getInt(cur.getColumnIndex(COLUMN_LON))));
            geocache.setType(GeoCacheType.CHECKPOINT);
            geocache.setStatus(GeoCacheStatus.values()[cur.getInt(cur.getColumnIndex(COLUMN_STATUS))]);
            exitCollection.add(geocache);

            cur.moveToNext();
        }

        cur.close();
        closeDB();
        return exitCollection;
    }

    /**
     * @param geoCacheForAdd
     *            GeoCache for add in database
     * @param webText
     *            html text for description GeoCache
     * @param webNotebookText
     *            text for web notebook
     */
    public void addGeoCache(GeoCache geoCacheForAdd, String webText, String webNotebookText) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, geoCacheForAdd.getId());
        values.put(COLUMN_NAME, geoCacheForAdd.getName());
        values.put(COLUMN_STATUS, geoCacheForAdd.getStatus().ordinal());
        values.put(COLUMN_TYPE, geoCacheForAdd.getType().ordinal());
        values.put(COLUMN_LAT, geoCacheForAdd.getLocationGeoPoint().getLatitudeE6());
        values.put(COLUMN_LON, geoCacheForAdd.getLocationGeoPoint().getLongitudeE6());
        values.put(COLUMN_WEB_TEXT, webText);
        if (webNotebookText != null && webNotebookText.equals("")) {
            values.put(COLUMN_NOTEBOOK_TEXT, webNotebookText);
        }
        openDB();
        db.insert(DATABASE_NAME_TABLE, null, values);
        closeDB();
    }

    /**
     * @param checkpoint
     *            GeoCache for add in database
     */
    public void addCheckpointGeoCache(GeoCache checkpoint, int cacheId) {
        LogManager.d(TAG, "addCheckpointGeoCache " + checkpoint.getId());
        ContentValues values = new ContentValues();
        values.put(CACHE_ID, cacheId);
        values.put(CHECKPOINT_ID, checkpoint.getId());
        values.put(COLUMN_NAME, checkpoint.getName());
        values.put(COLUMN_LAT, checkpoint.getLocationGeoPoint().getLatitudeE6());
        values.put(COLUMN_LON, checkpoint.getLocationGeoPoint().getLongitudeE6());
        values.put(COLUMN_STATUS, checkpoint.getStatus().ordinal());
        openDB();
        db.insert(DATABASE_CHECKPOINT_NAME_TABLE, null, values);
        closeDB();
    }

    /**
     * Remove geocache from DB, also remove all checkpointc corresponding to a geocache
     * 
     * @param id
     *            ID geocache for delete from database
     */
    public void deleteCacheById(int id) {
        openDB();
        db.execSQL(String.format("DELETE FROM %s WHERE %s=%d;", DATABASE_NAME_TABLE, COLUMN_ID, id));
        db.execSQL(String.format("DELETE FROM %s WHERE %s=%d;", DATABASE_CHECKPOINT_NAME_TABLE, CACHE_ID, id));
        closeDB();
    }

    /**
     * @param name
     *            name of checkpoint geocache
     * @param checkpointId
     *            geocache id for delete from database
     */
    public void deleteCheckpointCache(int cacheId, int checkpointId) {
        openDB();
        db.execSQL(String.format("DELETE FROM %s WHERE %s=%d AND %s=%d;", DATABASE_CHECKPOINT_NAME_TABLE, CACHE_ID, cacheId, CHECKPOINT_ID, checkpointId));
        closeDB();
    }

    /**
     * @param cacheId
     *            id of Searching GeoCache
     * @param checkpointId
     *            id of checkpoint
     * @param status
     *            checkpoint status
     */
    public void ubdateCheckpointCacheStatus(int cacheId, int checkpointId, GeoCacheStatus status) {
        openDB();
        db.execSQL(String.format("UPDATE %s SET %s=%d WHERE %s=%d AND %s=%d", DATABASE_CHECKPOINT_NAME_TABLE, COLUMN_STATUS, status.ordinal(), CACHE_ID, cacheId, CHECKPOINT_ID, checkpointId));
        closeDB();
    }

    /**
     * @param id
     *            ID GeoCache for taking his html description
     * @return String if GeoCache in database. Empty string if in database haven't GeoCache
     */
    public String getWebTextById(int id) {
        String exitString;
        openDB();
        Cursor c = db.rawQuery(String.format("select %s from %s where %s=%d", COLUMN_WEB_TEXT, DATABASE_NAME_TABLE, COLUMN_ID, id), null);
        if (c.getCount() == 0) {
            c.close();
            closeDB();
            return null;
        }
        c.moveToFirst();
        exitString = c.getString(0);
        c.close();
        closeDB();
        return exitString;
    }

    public String getWebNotebookTextById(int id) {
        String exitString;
        openDB();
        Cursor c = db.rawQuery(String.format("select %s from %s where %s=%d", COLUMN_NOTEBOOK_TEXT, DATABASE_NAME_TABLE, COLUMN_ID, id), null);
        if (c != null) {
            if (c.getCount() == 0) {
                c.close();
                closeDB();
                return null;
            }
            c.moveToFirst();

            exitString = c.getString(0);
            c.close();
            closeDB();
            return exitString;
        }
        return null;
    }

    public void ubdateNotebookText(int cacheId, String htmlNotebookText) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTEBOOK_TEXT, htmlNotebookText);
        openDB();
        db.update(DATABASE_NAME_TABLE, values, COLUMN_ID + "=" + cacheId, null);
        closeDB();
    }

    /**
     * @param id
     *            ID GeoCache for taking from database
     * @return GeoCache if database have GeoCache. Null if database haven't GeoCache
     */
    public GeoCache getCacheByID(int id) {
        openDB();
        Cursor c = db.rawQuery(String.format("select %s,%s,%s,%s,%s from %s where %s=%d", COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, DATABASE_NAME_TABLE, COLUMN_ID, id), null);
        if (c.getCount() == 0) {
            c.close();
            closeDB();
            return null;
        }
        c.moveToFirst();
        GeoCache exitCache = new GeoCache();
        exitCache.setId(id);

        exitCache.setName(c.getString(c.getColumnIndex(COLUMN_NAME)));
        exitCache.setLocationGeoPoint(new GeoPoint(c.getInt(c.getColumnIndex(COLUMN_LAT)), c.getInt(c.getColumnIndex(COLUMN_LON))));

        exitCache.setStatus(GeoCacheStatus.values()[c.getInt(c.getColumnIndex(COLUMN_STATUS))]);

        exitCache.setType(GeoCacheType.values()[c.getInt(c.getColumnIndex(COLUMN_TYPE))]);

        c.close();
        closeDB();
        return exitCache;
    }

    /**
     * Method for open database
     */
    private void openDB() {
        LogManager.d(TAG, "openDB");
        db = this.getWritableDatabase();
    }

    /**
     * Method for close database
     */
    private void closeDB() {
        LogManager.d(TAG, "closeDB");
        db.close();
    }
}
