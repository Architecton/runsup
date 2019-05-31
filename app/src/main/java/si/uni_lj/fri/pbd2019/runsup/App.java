package si.uni_lj.fri.pbd2019.runsup;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

public class App extends Application {
    private static Resources resources;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        resources = getResources();
        context = getApplicationContext();
    }

    public static Resources getAppResources() {
        return resources;
    }
    public static Context getAppContext() {return context; }
}
