package si.uni_lj.fri.pbd2019.runsup;

import android.app.Application;
import android.content.res.Resources;

public class App extends Application {
    private static Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();

        resources = getResources();
    }

    public static Resources getAppResources() {
        return resources;
    }

}
