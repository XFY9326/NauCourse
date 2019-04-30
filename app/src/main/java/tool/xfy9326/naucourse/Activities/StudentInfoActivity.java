package tool.xfy9326.naucourse.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.StudentInfo;
import tool.xfy9326.naucourse.Utils.StudentLearnProcess;

public class StudentInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);
        ToolBarSet();
        getData();
    }

    private void ToolBarSet() {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getData() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(Config.INTENT_STUDENT_INFO) && intent.hasExtra(Config.INTENT_STUDENT_LEARN_PROCESS)) {
                StudentInfo studentInfo = (StudentInfo) intent.getSerializableExtra(Config.INTENT_STUDENT_INFO);
                StudentLearnProcess studentLearnProcess = (StudentLearnProcess) intent.getSerializableExtra(Config.INTENT_STUDENT_LEARN_PROCESS);
                if (studentInfo != null) {
                    viewSet(studentInfo);
                }
                if (studentLearnProcess != null) {
                    learnProcessView(studentLearnProcess);
                }
            } else {
                finish();
            }
        }
    }

    private void viewSet(StudentInfo studentInfo) {
        ((TextView) findViewById(R.id.textView_info_stdId)).setText(getString(R.string.std_id, studentInfo.getStd_id()));
        ((TextView) findViewById(R.id.textView_info_stdClass)).setText(getString(R.string.std_class, studentInfo.getStd_class()));
        ((TextView) findViewById(R.id.textView_info_stdCollage)).setText(getString(R.string.std_collage, studentInfo.getStd_collage()));
        ((TextView) findViewById(R.id.textView_info_stdDirection)).setText(getString(R.string.std_direction, studentInfo.getStd_direction()));
        ((TextView) findViewById(R.id.textView_info_stdGrade)).setText(getString(R.string.std_grade, studentInfo.getStd_grade()));
        ((TextView) findViewById(R.id.textView_info_stdMajor)).setText(getString(R.string.std_major, studentInfo.getStd_major()));
        ((TextView) findViewById(R.id.textView_info_stdName)).setText(getString(R.string.std_name, studentInfo.getStd_name()));
    }

    private void learnProcessView(StudentLearnProcess studentLearnProcess) {
        if (studentLearnProcess != null) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            LinearLayout linearLayout = findViewById(R.id.layout_info_learn_process);
            for (int i = 0; i < 4; i++) {
                View view = layoutInflater.inflate(R.layout.item_learn_process, findViewById(R.id.layout_learn_process_item), false);
                String name = null;
                String aim = null;
                String now = null;
                String still = null;
                String award = null;
                int process = 0;
                switch (i) {
                    case 0:
                        name = getString(R.string.process_name, getString(R.string.process_bx));
                        aim = getString(R.string.process_aim, studentLearnProcess.getScoreBXAim());
                        now = getString(R.string.process_now, studentLearnProcess.getScoreBXNow());
                        still = getString(R.string.process_still, studentLearnProcess.getScoreBXStill());
                        if (studentLearnProcess.getScoreBXNow() != null && studentLearnProcess.getScoreBXAim() != null) {
                            process = (int) (Float.valueOf(studentLearnProcess.getScoreBXNow()) / Float.valueOf(studentLearnProcess.getScoreBXAim()) * 100);
                        } else {
                            process = 0;
                        }
                        break;
                    case 1:
                        name = getString(R.string.process_name, getString(R.string.process_zx));
                        aim = getString(R.string.process_aim, studentLearnProcess.getScoreZXAim());
                        now = getString(R.string.process_now, studentLearnProcess.getScoreZXNow());
                        still = getString(R.string.process_still, studentLearnProcess.getScoreZXStill());
                        if (studentLearnProcess.getScoreZXNow() != null && studentLearnProcess.getScoreZXAim() != null) {
                            process = (int) (Float.valueOf(studentLearnProcess.getScoreZXNow()) / Float.valueOf(studentLearnProcess.getScoreZXAim()) * 100);
                        } else {
                            process = 0;
                        }
                        break;
                    case 2:
                        name = getString(R.string.process_name, getString(R.string.process_rx));
                        aim = getString(R.string.process_aim, studentLearnProcess.getScoreRXAim());
                        now = getString(R.string.process_now, studentLearnProcess.getScoreRXNow());
                        still = getString(R.string.process_still, studentLearnProcess.getScoreRXStill());
                        award = getString(R.string.process_award, studentLearnProcess.getScoreRXAward());
                        if (studentLearnProcess.getScoreRXNow() != null && studentLearnProcess.getScoreRXAim() != null) {
                            process = (int) (Float.valueOf(studentLearnProcess.getScoreRXNow()) / Float.valueOf(studentLearnProcess.getScoreRXAim()) * 100);
                        } else {
                            process = 0;
                        }
                        break;
                    case 3:
                        name = getString(R.string.process_name, getString(R.string.process_sj));
                        aim = getString(R.string.process_aim, studentLearnProcess.getScoreSJAim());
                        now = getString(R.string.process_now, studentLearnProcess.getScoreSJNow());
                        still = getString(R.string.process_still, studentLearnProcess.getScoreSJStill());
                        if (studentLearnProcess.getScoreSJNow() != null && studentLearnProcess.getScoreSJAim() != null) {
                            process = (int) (Float.valueOf(studentLearnProcess.getScoreSJNow()) / Float.valueOf(studentLearnProcess.getScoreSJAim()) * 100);
                        } else {
                            process = 0;
                        }
                        break;
                }
                ((TextView) view.findViewById(R.id.textView_process_name)).setText(name);
                ((TextView) view.findViewById(R.id.textView_process_aim)).setText(aim);
                ((TextView) view.findViewById(R.id.textView_process_now)).setText(now);
                ((TextView) view.findViewById(R.id.textView_process_still)).setText(still);
                if (i == 2) {
                    TextView textView_award = view.findViewById(R.id.textView_process_award);
                    textView_award.setVisibility(View.VISIBLE);
                    textView_award.setText(award);
                }
                ProgressBar progressBar = view.findViewById(R.id.progressBar_process_learn);
                progressBar.setMax(100);
                progressBar.setProgress(process);

                linearLayout.addView(view);
            }
        }
    }
}
