package tool.xfy9326.naucourse.activities;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.LinkedHashMap;
import java.util.List;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.asyncTasks.CourseSearchAsync;
import tool.xfy9326.naucourse.asyncTasks.CourseSearchClassNameAsync;
import tool.xfy9326.naucourse.asyncTasks.CourseSearchInfoAsync;
import tool.xfy9326.naucourse.handlers.MainHandler;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.DialogMethod;
import tool.xfy9326.naucourse.methods.TimeMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.CourseSearchMethod;
import tool.xfy9326.naucourse.utils.CourseSearchDetail;
import tool.xfy9326.naucourse.utils.CourseSearchInfo;
import tool.xfy9326.naucourse.views.AdvancedRecyclerView;
import tool.xfy9326.naucourse.views.recyclerAdapters.CourseSearchAdapter;

public class CourseSearchActivity extends AppCompatActivity {
    private CourseSearchMethod courseSearchMethod;
    private CourseSearchAdapter courseSearchAdapter;
    private Dialog loadingDialog;
    private boolean isSpinnerMode = false;
    private boolean isWeekTypeSearch = false;
    private int lastSelectTermPosition = 0;
    private int lastSelectTypePosition = 0;
    private boolean isDefaultTypeSet = false;
    private boolean hasTableUpdate = false;
    private ArrayAdapter<String> valueAdapter = null;
    private List<String> termList;
    private AdvancedRecyclerView recyclerView;
    private String lastSelectClassName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);
        BaseMethod.getApp(this).setCourseSearchActivity(this);
        toolBarSet();
        this.courseSearchMethod = new CourseSearchMethod();
        viewSet();
    }

    private void toolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void viewSet() {
        if (valueAdapter == null) {
            valueAdapter = new ArrayAdapter<>(CourseSearchActivity.this, android.R.layout.simple_list_item_1);
            valueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        recyclerView = findViewById(R.id.recyclerView_course_search);
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setEmptyView(findViewById(R.id.textView_empty_data));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseSearchAdapter = new CourseSearchAdapter(this);
        recyclerView.setAdapter(courseSearchAdapter);

        getBaseSearchData();
    }

    private synchronized void closeLoadingDialog() {
        if (loadingDialog != null) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            loadingDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        closeLoadingDialog();
        if (hasTableUpdate) {
            MainHandler mainHandler = new MainHandler(this);
            mainHandler.sendEmptyMessage(Config.HANDLER_RELOAD_TABLE_DATA);
        }
        BaseMethod.getApp(this).setCourseSearchActivity(null);
        super.onDestroy();
    }

    private void getBaseSearchData() {
        if (loadingDialog == null) {
            loadingDialog = DialogMethod.showLoadingDialog(this, true, dialog -> finish());
        }
        new CourseSearchInfoAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, courseSearchMethod, getApplicationContext());
    }

    private void searchDetail(CourseSearchInfo courseSearchInfo) {
        if (loadingDialog == null) {
            loadingDialog = DialogMethod.showLoadingDialog(this, true, null);
        }
        new CourseSearchAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, courseSearchInfo, courseSearchMethod, getApplicationContext());
    }

    private void getClassListData(String term) {
        if (loadingDialog == null) {
            loadingDialog = DialogMethod.showLoadingDialog(this, false, null);
        }
        new CourseSearchClassNameAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, courseSearchMethod, term, getApplicationContext());
    }

    synchronized public void setBaseSearchView(final LinkedHashMap<String, String> searchTypeList, final List<String> termList, final List<String> roomList, final List<String> deptList) {
        if (searchTypeList != null && termList != null && roomList != null && deptList != null) {
            this.termList = termList;

            final String[] typeValueList = searchTypeList.keySet().toArray(new String[]{});
            final String[] typeTextList = searchTypeList.values().toArray(new String[]{});

            final Spinner spinnerTerm = findViewById(R.id.spinner_course_search_term);
            final Spinner spinnerSearchType = findViewById(R.id.spinner_course_search_type);
            final Spinner spinnerSearchValue = findViewById(R.id.spinner_course_search_value);
            final EditText editTextValue = findViewById(R.id.editText_course_search_value);

            spinnerSearchValue.setAdapter(valueAdapter);
            spinnerSearchValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    lastSelectClassName = (String) spinnerSearchValue.getSelectedItem();
                    if (courseSearchAdapter != null) {
                        courseSearchAdapter.clearSearchResult();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            ArrayAdapter<String> termAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, termList);
            termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTerm.setAdapter(termAdapter);
            spinnerTerm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (lastSelectTermPosition != position) {
                        if ("class".equals(typeValueList[spinnerSearchType.getSelectedItemPosition()])) {
                            getClassListData(termList.get(spinnerTerm.getSelectedItemPosition()));
                        }
                        lastSelectTermPosition = position;
                        if (courseSearchAdapter != null) {
                            courseSearchAdapter.clearSearchResult();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, typeTextList);
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSearchType.setAdapter(typeAdapter);
            spinnerSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (lastSelectTypePosition != position || !isDefaultTypeSet) {
                        isDefaultTypeSet = true;
                        isSpinnerMode = false;
                        isWeekTypeSearch = false;
                        valueAdapter.clear();

                        if ("class".equals(typeValueList[position])) {
                            isSpinnerMode = true;
                            getClassListData(termList.get(spinnerTerm.getSelectedItemPosition()));
                        } else if ("room".equals(typeValueList[position])) {
                            isSpinnerMode = true;
                            valueAdapter.addAll(roomList);
                        } else if ("dept".equals(typeValueList[position])) {
                            isSpinnerMode = true;
                            valueAdapter.addAll(deptList);
                        } else if ("week".equals(typeValueList[position])) {
                            isSpinnerMode = true;
                            isWeekTypeSearch = true;
                            valueAdapter.addAll(TimeMethod.getWeekStrArray(CourseSearchActivity.this));
                        } else {
                            Snackbar.make(findViewById(R.id.layout_course_search_content), R.string.search_use_full_name_warning, Snackbar.LENGTH_SHORT).show();
                        }

                        if (isSpinnerMode) {
                            valueAdapter.notifyDataSetChanged();
                            BaseMethod.hideKeyBoard(CourseSearchActivity.this);
                            spinnerSearchValue.setPrompt(typeTextList[position]);
                            spinnerSearchValue.setVisibility(View.VISIBLE);
                            editTextValue.setVisibility(View.GONE);
                        } else {
                            spinnerSearchValue.setVisibility(View.GONE);
                            editTextValue.setVisibility(View.VISIBLE);
                        }

                        if (courseSearchAdapter != null) {
                            courseSearchAdapter.clearSearchResult();
                        }
                        lastSelectTypePosition = position;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Button buttonSearch = findViewById(R.id.button_course_search);
            buttonSearch.setOnClickListener(v -> {
                CourseSearchInfo courseSearchInfo = new CourseSearchInfo();
                courseSearchInfo.setTerm(termList.get(spinnerTerm.getSelectedItemPosition()));
                courseSearchInfo.setSearchType(typeValueList[spinnerSearchType.getSelectedItemPosition()]);

                String value = null;
                if (isSpinnerMode) {
                    if (isWeekTypeSearch) {
                        value = String.valueOf(spinnerSearchValue.getSelectedItemPosition() + 1);
                    } else {
                        value = String.valueOf(spinnerSearchValue.getSelectedItem());
                    }
                } else {
                    Editable editable = editTextValue.getText();
                    if (editable != null) {
                        value = editable.toString();
                    }
                }
                if (value != null) {
                    value = value.trim();
                    if (value.isEmpty()) {
                        Snackbar.make(findViewById(R.id.layout_course_search_content), R.string.search_empty, Snackbar.LENGTH_SHORT).show();
                    } else {
                        courseSearchInfo.setValue(value);
                        searchDetail(courseSearchInfo);

                        if (spinnerTerm.getSelectedItemPosition() == 0 && sharedPreferences.getBoolean(Config.PREFERENCE_CAN_ADD_COURSE_WARNING, Config.DEFAULT_PREFERENCE_CAN_ADD_COURSE_WARNING)) {
                            Snackbar.make(findViewById(R.id.layout_course_search_content), R.string.click_to_add_course, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.no_alert_again, view -> sharedPreferences.edit().putBoolean(Config.PREFERENCE_CAN_ADD_COURSE_WARNING, false).apply())
                                    .setActionTextColor(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_light, getTheme()))
                                    .show();
                        }
                    }
                } else {
                    Snackbar.make(findViewById(R.id.layout_course_search_content), R.string.search_value_error, Snackbar.LENGTH_SHORT).show();
                }

                if (!isSpinnerMode) {
                    BaseMethod.hideKeyBoard(CourseSearchActivity.this);
                }
                if (recyclerView != null) {
                    recyclerView.smoothScrollToPosition(0);
                }
            });
        }
    }

    synchronized public void setSearchResult(List<CourseSearchDetail> courseSearchDetails) {
        if (courseSearchDetails != null && courseSearchAdapter != null) {
            if (courseSearchDetails.size() == 0) {
                Snackbar.make(findViewById(R.id.layout_course_search_content), R.string.search_no_result, Snackbar.LENGTH_SHORT).show();
            }
            courseSearchAdapter.updateSearchResult(courseSearchDetails);
        }
    }

    synchronized public void setClassNameList(List<String> classNameList) {
        if (classNameList != null && classNameList.size() > 0) {
            valueAdapter.clear();
            valueAdapter.addAll(classNameList);
            valueAdapter.notifyDataSetChanged();
            Spinner spinnerSearchValue = findViewById(R.id.spinner_course_search_value);
            int index = 0;
            for (int i = 0; i < classNameList.size(); i++) {
                if (classNameList.get(i) != null && classNameList.get(i).equals(lastSelectClassName)) {
                    index = i;
                    break;
                }
            }
            spinnerSearchValue.setSelection(index);
        }
    }

    public List<String> getTermList() {
        return termList;
    }

    public void lastViewSet() {
        closeLoadingDialog();
    }

    public void setHasTableUpdate(boolean hasTableUpdate) {
        this.hasTableUpdate = hasTableUpdate;
    }
}
