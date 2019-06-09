package tool.xfy9326.naucourse.views.recyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.activities.CourseSearchActivity;
import tool.xfy9326.naucourse.methods.CourseEditMethod;
import tool.xfy9326.naucourse.utils.CourseSearchDetail;

public class CourseSearchAdapter extends RecyclerView.Adapter<CourseSearchAdapter.CourseSearchViewHolder> {
    @NonNull
    private final CourseSearchActivity activity;
    private List<CourseSearchDetail> courseSearchDetails;
    private List<String> termList;

    public CourseSearchAdapter(@NonNull CourseSearchActivity activity) {
        this.activity = activity;
    }

    private static String getTimeStr(CourseSearchDetail courseSearchDetail) {
        return String.format(Locale.CHINA, "%s 周%d 第%d-%d节", courseSearchDetail.getWeekDes(), courseSearchDetail.getWeekDay(), courseSearchDetail.getStartCourseTime(), courseSearchDetail.getEndCourseTime());
    }

    private static String getTeacherStr(CourseSearchDetail courseSearchDetail) {
        return String.format(Locale.CHINA, "%s %s", courseSearchDetail.getTeacher(), courseSearchDetail.getTeacherGrade());
    }

    public void updateSearchResult(List<CourseSearchDetail> courseSearchDetails) {
        this.courseSearchDetails = courseSearchDetails;
        this.termList = activity.getTermList();
        notifyDataSetChanged();
    }

    public void clearSearchResult() {
        if (courseSearchDetails != null) {
            courseSearchDetails.clear();
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public CourseSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_course_search_detail, parent, false);
        return new CourseSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseSearchViewHolder holder, int position) {
        final CourseSearchDetail courseSearchDetail = courseSearchDetails.get(holder.getAdapterPosition());
        holder.textView_course_name.setText(courseSearchDetail.getName());
        holder.textView_teacher.setText(activity.getString(R.string.course_card_teacher, getTeacherStr(courseSearchDetail)));
        holder.textView_time.setText(activity.getString(R.string.course_search_time, getTimeStr(courseSearchDetail)));
        holder.textView_location.setText(activity.getString(R.string.course_card_location, courseSearchDetail.getRoomName()));
        holder.cardView_detail.setOnClickListener(v -> showMoreInfoDialog(courseSearchDetail));
    }

    private void showMoreInfoDialog(final CourseSearchDetail courseSearchDetail) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_course_search_detail, activity.findViewById(R.id.layout_dialog_course_search_detail));

        ((TextView) view.findViewById(R.id.textView_course_search_name)).setText(courseSearchDetail.getName());
        ((TextView) view.findViewById(R.id.textView_course_search_detail_teacher)).setText(activity.getString(R.string.course_card_teacher, getTeacherStr(courseSearchDetail)));
        ((TextView) view.findViewById(R.id.textView_course_search_detail_score)).setText(activity.getString(R.string.course_card_score, courseSearchDetail.getScore()));
        ((TextView) view.findViewById(R.id.textView_course_search_detail_class)).setText(activity.getString(R.string.course_card_class, courseSearchDetail.getClassName()));
        ((TextView) view.findViewById(R.id.textView_course_search_detail_combined_class)).setText(activity.getString(R.string.course_card_combined_class, courseSearchDetail.getCombinedClassName()));
        ((TextView) view.findViewById(R.id.textView_course_search_detail_college)).setText(activity.getString(R.string.course_search_college, courseSearchDetail.getCollage()));
        ((TextView) view.findViewById(R.id.textView_course_search_detail_location)).setText(activity.getString(R.string.course_card_location, courseSearchDetail.getRoomName()));
        ((TextView) view.findViewById(R.id.textView_course_search_detail_time)).setText(activity.getString(R.string.course_search_time, getTimeStr(courseSearchDetail)));
        ((TextView) view.findViewById(R.id.textView_course_search_detail_person)).setText(activity.getString(R.string.course_search_person, courseSearchDetail.getStuNum()));

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        if (termList != null && courseSearchDetail.getTerm().equals(termList.get(0))) {
            builder.setNeutralButton(R.string.import_course, (dialog, which) -> importCourse(courseSearchDetail));
        }
        builder.setPositiveButton(android.R.string.yes, null);
        builder.show();
    }

    private void importCourse(CourseSearchDetail courseSearchDetail) {
        CourseEditMethod.AddSearchCourseResult result = CourseEditMethod.addSearchCourse(activity, false, courseSearchDetail);
        if (result.isAddSuccess() && result.isSaveSuccess()) {
            activity.setHasTableUpdate(true);
            Snackbar.make(activity.findViewById(R.id.layout_course_search_content), R.string.import_success, Snackbar.LENGTH_SHORT).show();
        } else if (!result.isAddSuccess()) {
            CourseEditMethod.CourseCheckResult checkResult = result.getCourseCheckResult();
            String checkName = checkResult.getCheckCourseName();
            String conflictName = checkResult.getConflictCourseName();
            if (checkName != null && conflictName != null) {
                Snackbar.make(activity.findViewById(R.id.layout_course_search_content), activity.getString(R.string.course_edit_error_conflict, checkName, conflictName), Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(activity.findViewById(R.id.layout_course_search_content), R.string.course_edit_error, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(activity.findViewById(R.id.layout_course_search_content), R.string.save_failed, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        if (courseSearchDetails == null) {
            return 0;
        } else {
            return courseSearchDetails.size();
        }
    }

    static class CourseSearchViewHolder extends RecyclerView.ViewHolder {
        final TextView textView_course_name;
        final TextView textView_time;
        final TextView textView_teacher;
        final TextView textView_location;
        final CardView cardView_detail;

        CourseSearchViewHolder(View view) {
            super(view);
            textView_course_name = view.findViewById(R.id.textView_course_search_detail_course_name);
            textView_teacher = view.findViewById(R.id.textView_course_search_detail_teacher);
            textView_time = view.findViewById(R.id.textView_course_search_detail_time);
            textView_location = view.findViewById(R.id.textView_course_search_detail_location);
            cardView_detail = view.findViewById(R.id.cardView_course_search_detail);
        }
    }
}
