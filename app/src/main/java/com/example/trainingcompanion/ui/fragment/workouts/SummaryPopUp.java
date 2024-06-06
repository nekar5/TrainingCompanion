package com.example.trainingcompanion.ui.fragment.workouts;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.trainingcompanion.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class SummaryPopUp extends DialogFragment {

    private LineChart chart;

    public SummaryPopUp() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_summary, container, false);

        if (getArguments() != null) {
            // HR
            TextView max = root.findViewById(R.id.maxhr);
            max.setText(String.valueOf(getArguments().getInt("MAX")));
            TextView avg = root.findViewById(R.id.avghr);
            avg.setText(formatDouble(getArguments().getDouble("AVG")));
            TextView min = root.findViewById(R.id.minhr);
            min.setText(String.valueOf(getArguments().getInt("MIN")));
            TextView hrv = root.findViewById(R.id.varhr);
            hrv.setText(formatDouble(getArguments().getDouble("HRV")));
            TextView arr = root.findViewById(R.id.arrhr);
            arr.setText(String.valueOf(getArguments().getBoolean("ARR")));

            // cal
            TextView cal = root.findViewById(R.id.calories);
            cal.setText(formatDouble(getArguments().getDouble("CAL")));

            // other
            TextView intensity = root.findViewById(R.id.intensity);
            intensity.setText(getArguments().getString("INTENSITY"));
            TextView stress = root.findViewById(R.id.stress);
            stress.setText(getArguments().getString("STRESS"));
            TextView bmr = root.findViewById(R.id.bmr);
            bmr.setText(formatDouble(getArguments().getDouble("BMR")));
            TextView tdee = root.findViewById(R.id.tdee);
            tdee.setText(formatDouble(getArguments().getDouble("TDEE")));


            //graph
            ArrayList<Integer> heartRateData = getArguments().getIntegerArrayList("heartRateData");
            chart = root.findViewById(R.id.popUpChart);

            ArrayList<Entry> entries = new ArrayList<>();
            for (int i = 0; i < heartRateData.size(); i++) {
                entries.add(new Entry(i, heartRateData.get(i)));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Heart rate");
            dataSet.setColor(Color.RED);
            dataSet.setLineWidth(2f);
            dataSet.setValueTextColor(Color.RED);
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);

            configureChartAppearance();

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate();
        } else {
            onDestroy();
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = requireDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        }
    }

    private void configureChartAppearance() {
        chart.getAxisLeft().setAxisMinimum(50f);
        chart.getAxisLeft().setAxisMaximum(260f);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getLegend().setTextColor(Color.WHITE);

        chart.getDescription().setEnabled(false);

        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setVisibleXRangeMaximum(50);
    }

    @SuppressLint("DefaultLocale")
    public static String formatDouble(double value) {
        return String.format("%.2f", value);
    }
}