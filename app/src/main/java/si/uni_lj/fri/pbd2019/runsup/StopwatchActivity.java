package si.uni_lj.fri.pbd2019.runsup;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class StopwatchActivity extends AppCompatActivity {

    public static final String TAG = StopwatchActivity.class.getName();

    private boolean bound;  // indicator that indicates whether the service is bound
    private ServiceConnection sConn;  // connection to service
    private TrackerService service;  // Service proxy instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        // Create a new service connection.
        sConn = new ServiceConnection() {

            // callback that is called when the service is connected.
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(TAG, "StopwatchActivity onServiceConnected");
                service = ((TrackerService.LocalBinder)binder).getService();  // Call getService of passed binder.
                bound = true;  // Set bound indicator to true.
            }

            // callback that is called when the service is disconnected.
            public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG, "StopwatchActivity onServiceDisconnected");
                bound = false; // Set bound indicator to false.
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        this.sendBroadcast(new Intent(Constant.COMMAND_PAUSE));
        // Unregister service TrackerService.
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.sendBroadcast(new Intent(Constant.COMMAND_CONTINUE));
        // Start service TrackerService.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {  // If service still bounded, unbind.
            unbindService(sConn);
            bound = false;
        }

    }

    // TODO Bind to button.
    public void startStopwatch(View view) {

        // change the button text to string #stopwatch_stop

        // start TrackerService with action si.uni_lj.fri.pbd2019.runsup.COMMAND_START
        bindService(new Intent(this, TrackerService.class), sConn, BIND_AUTO_CREATE);
        this.sendBroadcast(new Intent(Constant.COMMAND_START));
    }

    public void endWorkout() {
        this.sendBroadcast(new Intent(Constant.COMMAND_STOP));  // Send command to stop workout.
        if (bound) {  // If service still bounded, unbind.
            unbindService(sConn);
            bound = false;
        }
    }
}
