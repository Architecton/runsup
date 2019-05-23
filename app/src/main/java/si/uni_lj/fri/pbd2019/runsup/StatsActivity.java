package si.uni_lj.fri.pbd2019.runsup;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

public class StatsActivity extends AppCompatActivity implements OnSeekBarChangeListener, OnChartValueSelectedListener {

    private LineChart chart;
    private SeekBar seekBarX, seekBarY;
    private TextView tvX, tvY;
    protected Typeface tfRegular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_linechart);

        setTitle("Statistics");

        // tvX = findViewById(R.id.tvXMax);
        //tvY = findViewById(R.id.tvYMax);

        //seekBarX = findViewById(R.id.seekBar1);
        seekBarX.setOnSeekBarChangeListener(this);

        //seekBarY = findViewById(R.id.seekBar2);
        seekBarY.setMax(180);
        seekBarY.setOnSeekBarChangeListener(this);

        tfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        {
            //chart = findViewById(R.id.chart1);
            chart.setBackgroundColor(Color.WHITE);
            chart.getDescription().setEnabled(true);
            chart.setTouchEnabled(true);
            chart.setOnChartValueSelectedListener(this);
            chart.setDrawGridBackground(false);
            MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
            mv.setChartView(chart);
            chart.setMarker(mv);
            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);
            chart.setPinchZoom(true);
        }

        XAxis xAxis;
        {
            xAxis = chart.getXAxis();
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }

        YAxis yAxis;
        {
            yAxis = chart.getAxisLeft();
            chart.getAxisRight().setEnabled(false);
            yAxis.enableGridDashedLine(10f, 10f, 0f);
            yAxis.setAxisMaximum(200f);
            yAxis.setAxisMinimum(-50f);
        }

        seekBarX.setProgress(45);
        seekBarY.setProgress(180);
        setData(45, 180);
        chart.animateX(1500);
        Legend l = chart.getLegend();
        l.setForm(LegendForm.LINE);
    }

    // setData: set data to chart
    private void setData(int count, float range) {

        // ArrayList instance that will contain the values that will be plotted.
        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * range) - 30;
            values.add(new Entry(i, val));
        }

        // Define LineDataSet instance to be plotted.
        LineDataSet setToPlot;

        // If chart contains data.
        if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
            setToPlot = (LineDataSet) chart.getData().getDataSetByIndex(0);
            setToPlot.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            setToPlot.setValues(values);
            setToPlot.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            setToPlot = new LineDataSet(values, "TODO");
            setToPlot.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            setToPlot.setDrawIcons(false);
            setToPlot.enableDashedLine(10f, 5f, 0f);
            setToPlot.setColor(Color.BLACK);
            setToPlot.setCircleColor(Color.BLACK);
            setToPlot.setLineWidth(1.0f);
            setToPlot.setCircleRadius(3.0f);
            setToPlot.setDrawCircleHole(false);
            setToPlot.setFormLineWidth(1f);
            setToPlot.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            setToPlot.setFormSize(15.f);
            setToPlot.setValueTextSize(9f);
            setToPlot.enableDashedHighlightLine(10f, 5f, 0f);
            setToPlot.setDrawFilled(true);
            setToPlot.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return chart.getAxisLeft().getAxisMinimum();
                }
            });

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
            setToPlot.setFillDrawable(drawable);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(setToPlot);
            LineData data = new LineData(dataSets);

            // set data to chart.
            chart.setData(data);
            chart.animateXY(2000, 2000);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        tvX.setText(String.valueOf(seekBarX.getProgress()));
        tvY.setText(String.valueOf(seekBarY.getProgress()));

        setData(seekBarX.getProgress(), seekBarY.getProgress());

        // redraw
        chart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOW HIGH", "low: " + chart.getLowestVisibleX() + ", high: " + chart.getHighestVisibleX());
        Log.i("MIN MAX", "xMin: " + chart.getXChartMin() + ", xMax: " + chart.getXChartMax() + ", yMin: " + chart.getYChartMin() + ", yMax: " + chart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
}
