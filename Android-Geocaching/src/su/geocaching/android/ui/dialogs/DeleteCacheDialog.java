package su.geocaching.android.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.R;

/**
 * @author Nikita Bumakov
 */
public class DeleteCacheDialog extends AlertDialog {
    private static final String TAG = DeleteCacheDialog.class.getCanonicalName();

    private ConfirmDeleteCacheListener resultListener;

    public DeleteCacheDialog(final Context context, final ConfirmDeleteCacheListener resultListener) {
        super(context);
        this.resultListener = resultListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final GeoCache geoCache = (GeoCache)savedInstanceState.getParcelable(GeoCache.class.getCanonicalName());
        // set icon
        //setIcon(R.drawable.ic_launcher);
        // set title
        setTitle(R.string.info_activity_confirm_delete_title);
        // set content
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View aboutContentView = inflater.inflate(R.layout.remove_favorite_cache_dialog, null);
        setView(aboutContentView);
        // add yes button
        setButton(BUTTON_POSITIVE, getContext().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //final CheckBox forceRemove = (CheckBox) findViewById(R.id.removeWithoutConfirm);
                //Controller.getInstance().getPreferencesManager().setRemoveFavoriteWithoutConfirm(forceRemove.isChecked());
                resultListener.onConfirm(geoCache);
                dialog.dismiss();
            }
        });
        // add no button
        setButton(BUTTON_NEGATIVE, getContext().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        setCancelable(true);

        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "OnCreate");
    }
}
