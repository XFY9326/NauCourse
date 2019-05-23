package tool.xfy9326.naucourse.activities;

import android.app.Dialog;
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
    private ArrayAdapter<String> value_adapter = null;
    private List<String> termList;
    private AdvancedRecyclerView recyclerView;
    private String lastSelectClassName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);
        BaseMethod.getApp(this).setCourseSearchActivity(this);
        ToolBarSet();
        this.courseSearchMethod = new CourseSearchMethod();
        ViewSet();
    }

    private void ToolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void ViewSet() {
        if (value_adapter == null) {
            value_adapter = new ArrayAdapter<>(CourseSearchActivity.this, android.R.layout.simple_list_item_1);
            value_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        if (hasTableUpdate) {
            MainHandler mainHandler = new MainHandler(this);
            mainHandler.sendEmptyMessage(Config.HANDLER_RELOAD_TABLE_DATA);
        }
        BaseMethod.getApp(this).setCourseSearchActivity(null);
        super.onDestroy();
    }

    private void getBaseSearchData() {
        if (loadingDialog == null) {
            loadingDialog = BaseMethod.showLoadingDialog(this, true, dialog -> finish());
        }
        new CourseSearchInfoAsync().execute(courseSearchMethod, getApplicationContext());
    }

    private void searchDetail(CourseSearchInfo courseSearchInfo) {
        if (loadingDialog == null) {
            loadingDialog = BaseMethod.showLoadingDialog(this, true, null);
        }
        new CourseSearchAsync().execute(courseSearchInfo, courseSearchMethod, getApplicationContext());
    }

    private void getClassListData(String term) {
        if (loadingDialog == null) {
            loadingDialog = BaseMethod.showLoadingDialog(this, false, null);
        }
        new CourseSearchClassNameAsync().execute(courseSearchMethod, term, getApplicationContext());
    }

    synchronized public void setBaseSearchView(final LinkedHashMap<String, String> searchTypeList, final List<String> termList, final List<String> roomList, final List<String> deptList) {
        if (searchTypeList != null && termList != null && roomList != null && deptList != null) {
            this.termList = termList;

            final String[] typeValueList = searchTypeList.keySet().toArray(new String[]{});
            final String[] typeTextList = searchTypeList.values().toArray(new String[]{});

            final Spinner spinner_term = findViewById(R.id.spinner_course_search_term);
            final Spinner spinner_search_type = findViewById(R.id.spinner_course_search_type);
            final Spinner spinner_search_value = findViewById(R.id.spinner_course_search_value);
            final EditText editText_value = findViewById(R.id.editText_course_search_value);

            spinner_search_value.setAdapter(value_adapter);
            spinner_search_value.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    lastSelectClassName = (String) spinner_search_value.getSelectedItem();
                    if (courseSearchAdapter != null) {
                        courseSearchAdapter.clearSearchResult();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            ArrayAdapter<String> term_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, termList);
            term_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_term.setAdapter(term_adapter);
            spinner_term.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (lastSelectTermPosition != position) {
                        if ("class".equals(typeValueList[spinner_search_type.getSelectedItemPosition()])) {
                            getClassListData(termList.get(spinner_term.getSelectedItemPosition()));
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

            ArrayAdapter<String> type_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, typeTextList);
            type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_search_type.setAdapter(type_adapter);
            spinner_search_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (lastSelectTypePosition != position || !isDefaultTypeSet) {
                        isDefaultTypeSet = true;
                        isSpinnerMode = false;
                        isWeekTypeSearch = false;
                        value_adapter.clear();

                        if ("class".equals(typeValueList[position])) {
                            isSpinnerMode = true;
                            getClassListData(termList.get(spinner_term.getSelectedItemPosition()));
                        } else if ("room".equals(typeValueList[position])) {
                            isSpinnerMode = true;
                            value_adapter.addAll(roomList);
                        } else if ("dept".equals(typeValueList[position])) {
                            isSpinnerMode = true;
                            value_adapter.addAll(deptList);
                        } else if ("week".equals(typeValueList[position])) {
                            isSpinnerMode = true;
                            isWeekTypeSearch = true;
                            value_adapter.addAll(TimeMethod.getWeekStrArray(CourseSearchActivity.this));
                        } else {
                            Snackbar.make(findViewById(R.id.layout_course_search_content), R.string.search_use_full_name_warning, Snackbar.LENGTH_SHORT).show();
                        }

                        if (isSpinnerMode) {
                            value_adapter.notifyDataSetChanged();
                            BaseMethod.hideKeyBoard(CourseSearchActivity.this);
                            spinner_search_value.setPrompt(typeTextList[position]);
                            spinner_search_value.setVisibility(View.VISIBLE);
                            editText_value.setVisibility(View.GONE);
                        } else {
                            spinner_search_value.setVisibility(View.GONE);
                            editText_value.setVisibility(View.VISIBLE);
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

            Button button_search = findViewById(R.id.button_course_search);
            button_search.setOnClickListener(v -> {
                CourseSearchInfo courseSearchInfo = new CourseSearchInfo();
                courseSearchInfo.setTerm(termList.get(spinner_term.getSelectedItemPosition()));
                courseSearchInfo.setSearchType(typeValueList[spinner_search_type.getSelectedItemPosition()]);

                String value = null;
                if (isSpinnerMode) {
                    if (isWeekTypeSearch) {
                        value = String.valueOf(spinner_search_value.getSelectedItemPosition() + 1);
                    } else {
                        value = String.valueOf(spinner_search_value.getSelectedItem());
                    }
                } else {
                    Editable editable = editText_value.getText();
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
            value_adapter.clear();
            value_adapter.addAll(classNameList);
            value_adapter.notifyDataSetChanged();
            Spinner spinner_search_value = findViewById(R.id.spinner_course_search_value);
            int index = 0;
            for (int i = 0; i < classNameList.size(); i++) {
                if (classNameList.get(i) != null && classNameList.get(i).equals(lastSelectClassName)) {
                    index = i;
                    break;
                }
            }
            spinner_search_value.setSelection(index);
        }
    }

    public List<String> getTermList() {
        return termList;
    }

    public void lastBaseViewSet() {
        closeLoadingDialog();
    }

    public void lastSearchViewSet() {
        closeLoadingDialog();
    }

    public void lastClassListViewSet() {
        closeLoadingDialog();
    }

    public void setHasTableUpdate(boolean hasTableUpdate) {
        this.hasTableUpdate = hasTableUpdate;
    }
}
