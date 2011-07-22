package su.geocaching.android.controller.selectmap.mapupdatetimer;

import su.geocaching.android.ui.selectmap.SelectMapActivity;

/**
 * @author Yuri Denison
 * @since 25.11.10
 */
public class State {
    private boolean mapUpdated;
    private boolean requestSent;
    private final SelectMapActivity gcMap;


    public State(SelectMapActivity gcMap) {
        this.mapUpdated = false;
        this.requestSent = false;
        this.gcMap = gcMap;
    }

    public synchronized void setMapUpdatedTrue() {
        mapUpdated = true;
        sendRequest();
    }

    public synchronized void setRequestSentTrue() {
        requestSent = true;
        sendRequest();
    }

    private synchronized void sendRequest() {
        if (requestSent && mapUpdated) {
            gcMap.updateCacheOverlay();

            mapUpdated = false;
            requestSent = false;
        }
    }
}
