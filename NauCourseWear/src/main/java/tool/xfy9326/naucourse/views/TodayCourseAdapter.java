package tool.xfy9326.naucourse.views;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.BoxInsetLayout;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.beans.course.TodayCourses;

public class TodayCourseAdapter extends RecyclerView.Adapter<TodayCourseAdapter.TodayCourseViewHolder> {
    private final int boxPadding;
    private final AdvancedRecyclerView recyclerView;
    private final int screenHeight;
    private final Context context;
    private final RecyclerView.SmoothScroller smoothScroller;
    private TodayCourses todayCourses;
    private boolean expanded = false;

    public TodayCourseAdapter(Context context, AdvancedRecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        this.boxPadding = (int) context.getResources().getDimension(R.dimen.box_inset_layout_padding);
        this.smoothScroller = new LinearSmoothScroller(context) {
            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 100f / displayMetrics.densityDpi;
            }

            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
    }

    public void updateTodayCourses(TodayCourses todayCourses) {
        setExpand(false);
        this.todayCourses = todayCourses;
        if (todayCourses != null) {
            recyclerView.setUseBottomListener(todayCourses.getNextCourse().getCourseName() != null);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull TodayCourseViewHolder holder, int position) {
        if (holder.getAdapterPosition() == 0) {
            setNextCourseCard(holder);
        } else if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (todayCourses.getNextCourse().getCourseName() != null) {
                setPreScrollSpace(holder);
            } else {
                setTodayCourseCardView(holder);
            }
        } else {
            setTodayCourseCardView(holder);
        }
    }

    private void setNextCourseCard(TodayCourseViewHolder holder) {
        //下一节课
        holder.layout_box_border.setVisibility(View.VISIBLE);
        holder.layout_box_border.setPadding(boxPadding, 0, boxPadding, 0);
        holder.layout_today_courses.setVisibility(View.GONE);

        if (todayCourses.getNextCourse().getCourseName() == null) {
            holder.layout_box_border.setPadding(boxPadding, boxPadding + 10, boxPadding, 0);
            holder.layout_course_item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            holder.layout_next_course.setVisibility(View.GONE);
            holder.textView_no_next_course.setVisibility(View.VISIBLE);
        } else {
            holder.layout_box_border.setPadding(boxPadding, boxPadding, boxPadding, boxPadding);
            holder.layout_course_item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

            holder.layout_next_course.setVisibility(View.VISIBLE);
            holder.textView_no_next_course.setVisibility(View.GONE);

            holder.textView_next_course_name.setText(todayCourses.getNextCourse().getCourseName());
            holder.textView_next_course_time.setText(todayCourses.getNextCourse().getCourseTime());
            holder.textView_next_course_location.setText(todayCourses.getNextCourse().getCourseLocation());
        }
    }

    private void setPreScrollSpace(TodayCourseViewHolder holder) {
        //预留下拉空间
        if (todayCourses.getNextCourse().getCourseName() != null) {
            holder.layout_box_border.setVisibility(View.GONE);
            holder.layout_course_item.setVisibility(View.VISIBLE);
            if (!expanded) {
                holder.layout_course_item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, screenHeight / 6));
            }
        } else {
            holder.layout_box_border.setVisibility(View.GONE);
            holder.layout_course_item.setVisibility(View.GONE);
        }
    }

    private void setTodayCourseCardView(TodayCourseViewHolder holder) {
        //今日课程
        int offset = todayCourses.getNextCourse().getCourseName() != null ? 2 : 1;
        if (holder.getAdapterPosition() == getItemCount() - offset) {
            holder.layout_box_border.setPadding(boxPadding, 0, boxPadding, boxPadding);
        } else {
            holder.layout_box_border.setPadding(boxPadding, 0, boxPadding, 0);
        }
        holder.layout_box_border.setVisibility(View.VISIBLE);
        holder.layout_course_item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        holder.layout_next_course.setVisibility(View.GONE);
        holder.textView_no_next_course.setVisibility(View.GONE);
        holder.layout_today_courses.setVisibility(View.VISIBLE);

        holder.textView_course_info.setText(todayCourses.getCourses()[holder.getAdapterPosition() - 1].replace("\n\n", "\n"));
        holder.textView_course_time.setText(todayCourses.getCoursesTime()[holder.getAdapterPosition() - 1]);
    }

    @SuppressWarnings("SameParameterValue")
    private void smoothScrollToCenter(int position) {
        RecyclerView.LayoutManager mManager = recyclerView.getLayoutManager();
        if (mManager != null) {
            smoothScroller.setTargetPosition(position);
            mManager.startSmoothScroll(smoothScroller);
        }
    }

    public void setExpand(boolean expand) {
        if (todayCourses != null) {
            if (expand && !expanded) {
                expanded = true;
                notifyItemChanged(0);
                notifyItemRangeInserted(1, todayCourses.getCourses().length);
                smoothScrollToCenter(1);
            }
            if (!expand && expanded) {
                expanded = false;
                notifyItemChanged(0);
                notifyItemRangeRemoved(1, todayCourses.getCourses().length);
                recyclerView.smoothScrollToPosition(0);
            }
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    @NonNull
    @Override
    public TodayCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_course, parent, false);
        return new TodayCourseViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if (todayCourses != null) {
            if (todayCourses.getCourses().length == 0) {
                return 0;
            }
            if (todayCourses.getNextCourse().getCourseName() == null) {
                return todayCourses.getCourses().length + 1;
            }

            if (expanded) {
                return todayCourses.getCourses().length + 2;
            } else {
                return 2;
            }
        }
        return 0;
    }

    static class TodayCourseViewHolder extends RecyclerView.ViewHolder {
        final AppCompatTextView textView_course_info;
        final AppCompatTextView textView_course_time;
        final AppCompatTextView textView_next_course_name;
        final AppCompatTextView textView_next_course_location;
        final AppCompatTextView textView_next_course_time;
        final AppCompatTextView textView_no_next_course;
        final LinearLayout layout_next_course;
        final LinearLayout layout_today_courses;
        final LinearLayout layout_course_item;
        final BoxInsetLayout layout_box_border;

        TodayCourseViewHolder(View view) {
            super(view);
            textView_course_info = view.findViewById(R.id.textView_course_info);
            textView_course_time = view.findViewById(R.id.textView_course_time);
            textView_next_course_name = view.findViewById(R.id.textView_next_course_name);
            textView_next_course_location = view.findViewById(R.id.textView_next_course_location);
            textView_next_course_time = view.findViewById(R.id.textView_next_course_time);
            textView_no_next_course = view.findViewById(R.id.textView_no_next_course);
            layout_next_course = view.findViewById(R.id.layout_next_course);
            layout_today_courses = view.findViewById(R.id.layout_today_courses);
            layout_course_item = view.findViewById(R.id.layout_course_item);
            layout_box_border = view.findViewById(R.id.layout_box_border);
        }
    }
}
