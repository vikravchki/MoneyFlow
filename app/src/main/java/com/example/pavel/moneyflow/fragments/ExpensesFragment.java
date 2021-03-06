package com.example.pavel.moneyflow.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.pavel.moneyflow.R;
import com.example.pavel.moneyflow.util.DateConverter;
import com.example.pavel.moneyflow.util.Prefs;
import com.example.pavel.moneyflow.views.RoundChart;

import org.jetbrains.annotations.Nullable;

public class ExpensesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    RoundChart roundChart;
    EditText etExpensesPlan;
    private boolean isOpened;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);
        roundChart = (RoundChart) view.findViewById(R.id.rcExpenses);

        etExpensesPlan = (EditText) view.findViewById(R.id.etIncomesPlan);
        etExpensesPlan.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    int input = Integer.parseInt(etExpensesPlan.getText().toString());
                    ContentValues cv = new ContentValues();
                    cv.put(Prefs.MONTH_CASH_FIELD_E_PLAN, input);

                    int rowUpdated = getActivity().getContentResolver().update(Prefs.URI_MONTHLY_CASH, cv,
                            Prefs.MONTHLY_CASH_FIELD_MONTH + " = " + DateConverter.getCurrentMonth() + " AND " +
                                    Prefs.MONTHLY_CASH_FIELD_YEAR + " = " + DateConverter.getCurrentYear(), null);
                    if (rowUpdated == 0) {
                        cv.put(Prefs.MONTHLY_CASH_FIELD_MONTH, DateConverter.getCurrentMonth());
                        cv.put(Prefs.MONTHLY_CASH_FIELD_YEAR, DateConverter.getCurrentYear());
                        getActivity().getContentResolver().insert(Prefs.URI_MONTHLY_CASH, cv);
                    }
                    etExpensesPlan.clearFocus();

                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    Log.d(Prefs.LOG_TAG, "Monthly cash updated! New values - " + cv.toString());
                    return true;
                }
                return false;
            }
        });
        setListenerToRootView();

        return view;
    }

    @Override
    public void onCreate(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Prefs.LOG_TAG, "onCreate: chart = null ?" + String.valueOf(roundChart == null));
//        roundChart.setBackgroundColor(((ColorDrawable) (getView().getBackground())).getColor());
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(Prefs.LOG_TAG, "onStart: chart = null ? - " + String.valueOf(roundChart == null));
        getActivity().getSupportLoaderManager().initLoader(1, null, this);
    }


    public void setListenerToRootView() {
        final View activityRootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // 99% of the time the height diff will be due to a keyboard.
                    if (isOpened == false) {
                        //Do two things, make the view top visible and the editText smaller
                    }
                    isOpened = true;
                } else if (isOpened == true) {
                    isOpened = false;
                    etExpensesPlan.clearFocus();
                }
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Prefs.URI_MONTHLY_CASH, null, Prefs.MONTHLY_CASH_FIELD_MONTH +
                "=?", new String[]{DateConverter.getCurrentMonth()}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()){
            int current = data.getInt(data.getColumnIndex(Prefs.MONTHLY_CASH_FIELD_EXPENSE));
            int plan = data.getInt(data.getColumnIndex(Prefs.MONTH_CASH_FIELD_E_PLAN));

            if (plan != 0) {
                etExpensesPlan.setText(String.valueOf(plan));
                int percent = (current * 100)/plan;
                roundChart.setValues(percent);
                roundChart.beginChartAnimation();
            }

        } else {
            roundChart.setValues(0);
            roundChart.beginChartAnimation();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
