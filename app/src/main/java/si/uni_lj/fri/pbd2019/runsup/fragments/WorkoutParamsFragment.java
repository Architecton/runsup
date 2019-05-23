package si.uni_lj.fri.pbd2019.runsup.fragments;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
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
import java.util.Collections;

import si.uni_lj.fri.pbd2019.runsup.Constant;
import si.uni_lj.fri.pbd2019.runsup.R;
import si.uni_lj.fri.pbd2019.runsup.WorkoutStatsActivity;

public class WorkoutParamsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, OnChartValueSelectedListener {

    private LineChart chart;
    protected Typeface tfRegular;

    private ArrayList<Double> receivedData;

    // Required empty public constructor
    public WorkoutParamsFragment() {}

    // Static method used to pass data to fragment.
    public static WorkoutParamsFragment newInstance(ArrayList<Double> data, int color, int type) {
        WorkoutParamsFragment fragment = new WorkoutParamsFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", data);
        args.putInt("color", color);
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    // setData: set data to chart
    private void setData(ArrayList<Double> dataToPlot) {

        // ArrayList instance that will contain the values that will be plotted.
        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < dataToPlot.size(); i++) {
            float val = (float)(double)dataToPlot.get(i);
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
            switch (getArguments().getInt("type")) {
                case Constant.CHART_TYPE_ELEVATION:
                    setToPlot = new LineDataSet(values, "Elevation");
                    break;
                case Constant.CHART_TYPE_PACE:
                    setToPlot = new LineDataSet(values, "Pace");
                    break;
                case Constant.CHART_TYPE_CALORIES:
                    setToPlot = new LineDataSet(values, "Calories");
                    break;
                default:
                    setToPlot = new LineDataSet(values, "Value");
            }
            setToPlot.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            setToPlot.setDrawIcons(false);
            setToPlot.enableDashedLine(10f, 5f, 0f);
            setToPlot.setColor(Color.BLACK);
            setToPlot.setCircleColor(Color.BLACK);
            setToPlot.setLineWidth(1.0f);
            setToPlot.setCircleRadius(1.0f);
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

            Drawable drawable;
            switch(getArguments().getInt("color")) {
                case Constant.GRAPH_COLOR_GREEN:
                    drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_green);
                    break;
                case Constant.GRAPH_COLOR_BLUE:
                    drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_blue);
                    break;
                case Constant.GRAPH_COLOR_RED:
                    drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_red);
                    break;
                default:
                    drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_red);
            }
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
        setData(this.receivedData);
        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(progress);
        xAxis.setAxisMaximum(progress + Constant.PLOT_INTERVAL);
        chart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.receivedData = (ArrayList<Double>) getArguments().getSerializable("data");
        switch(getArguments().getInt("type")) {
            case Constant.CHART_TYPE_ELEVATION:
                return inflater.inflate(R.layout.activity_linechart, container, false);
            case Constant.CHART_TYPE_PACE:
                return inflater.inflate(R.layout.activity_linechart2, container, false);
            case Constant.CHART_TYPE_CALORIES:
                return inflater.inflate(R.layout.activity_linechart3, container, false);
            default:
                return inflater.inflate(R.layout.activity_linechart, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch(getArguments().getInt("type")) {
            case Constant.CHART_TYPE_ELEVATION:
                chart = getActivity().findViewById(R.id.chart_elevation);
                break;
            case Constant.CHART_TYPE_PACE:
                chart = getActivity().findViewById(R.id.chart_pace);
                break;
            case Constant.CHART_TYPE_CALORIES:
                chart = getActivity().findViewById(R.id.chart_calories);
                break;

        }

        WorkoutStatsActivity.CompositeListener listener = ((WorkoutStatsActivity)getActivity()).seekBarIntervalListener;
        listener.registerListener(this);

        tfRegular = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");

        {
            chart.setBackgroundColor(Color.WHITE);
            chart.getDescription().setEnabled(false);
            chart.setTouchEnabled(true);
            chart.setOnChartValueSelectedListener(this);
            chart.setDrawGridBackground(false);
            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);
            chart.setPinchZoom(true);
            chart.setAutoScaleMinMaxEnabled(true);
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
            if (getArguments().getInt("type") == Constant.CHART_TYPE_ELEVATION) {
                yAxis.setAxisMaximum(((float)(double)Collections.max(this.receivedData) * 2));
            } else {
                yAxis.setAxisMaximum((float)(double)Collections.max(this.receivedData));
            }
            yAxis.setAxisMinimum(0f);
        }

        setData(this.receivedData);
        chart.animateX(1500);
        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
    }

    @Override
    public void onNothingSelected() {
    }
}
