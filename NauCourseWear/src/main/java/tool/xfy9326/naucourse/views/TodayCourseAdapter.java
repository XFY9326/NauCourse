package tool.xfy9326.naucourse.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.beans.course.TodayCourses;
import tool.xfy9326.naucourse.views.viewHolders.NextCourseViewHolder;
import tool.xfy9326.naucourse.views.viewHolders.NoNextCourseViewHolder;
import tool.xfy9326.naucourse.views.viewHolders.PreScrollViewHolder;
import tool.xfy9326.naucourse.views.viewHolders.TodayCourseViewHolder;

public class TodayCourseAdapter extends RecyclerView.Adapter {
    private final int boxPadding;
    private final AdvancedRecyclerView recyclerView;
    private final int screenHeight;
    private final RecyclerView.SmoothScroller smoothCenterScroller;
    private LayoutInflater inflater;
    private TodayCourses todayCourses;
    private boolean expanded = false;

    public TodayCourseAdapter(Context context, AdvancedRecyclerView recyclerView) {
        this.inflater = LayoutInflater.from(context);
        this.recyclerView = recyclerView;
        this.screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        this.boxPadding = (int) context.getResources().getDimension(R.dimen.box_inset_layout_padding);
        this.smoothCenterScroller = new LinearSmoothCenterScroller(context);
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(holder.getAdapterPosition());
        switch (type) {
            case NextCourseViewHolder.VIEW_TYPE:
                setNextCourseCard(holder);
                break;
            case NoNextCourseViewHolder.VIEW_TYPE:
                setNoNextCourseCard(holder);
                break;
            case TodayCourseViewHolder.VIEW_TYPE:
                setTodayCourseCardView(holder);
                break;
            default:
                setPreScrollSpace(holder);
        }
    }

    private void setNextCourseCard(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof NextCourseViewHolder) {
            NextCourseViewHolder holder = (NextCourseViewHolder) viewHolder;
            //下一节课
            holder.textView_next_course_name.setText(todayCourses.getNextCourse().getCourseName());
            holder.textView_next_course_time.setText(todayCourses.getNextCourse().getCourseTime());
            holder.textView_next_course_location.setText(todayCourses.getNextCourse().getCourseLocation());
        }
    }

    private void setNoNextCourseCard(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof NoNextCourseViewHolder) {
            NoNextCourseViewHolder holder = (NoNextCourseViewHolder) viewHolder;
            holder.layout_box_border.setPadding(boxPadding, boxPadding + 10, boxPadding, 0);
        }
    }

    private void setPreScrollSpace(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof PreScrollViewHolder) {
            PreScrollViewHolder holder = (PreScrollViewHolder) viewHolder;
            //预留下拉空间
            if (!expanded) {
                holder.layout_empty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, screenHeight / 6));
            }
        }
    }

    private void setTodayCourseCardView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof TodayCourseViewHolder) {
            TodayCourseViewHolder holder = (TodayCourseViewHolder) viewHolder;
            //今日课程
            int offset = hasNextCourse() ? 2 : 1;
            if (holder.getAdapterPosition() == getItemCount() - offset) {
                holder.layout_box_border.setPadding(boxPadding, 0, boxPadding, boxPadding);
            } else {
                holder.layout_box_border.setPadding(boxPadding, 0, boxPadding, 0);
            }

            holder.textView_course_info.setText(todayCourses.getCourses()[holder.getAdapterPosition() - 1].replace("\n\n", "\n"));
            holder.textView_course_time.setText(todayCourses.getCoursesTime()[holder.getAdapterPosition() - 1]);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void smoothScrollToCenter(int position) {
        RecyclerView.LayoutManager mManager = recyclerView.getLayoutManager();
        if (mManager != null) {
            smoothCenterScroller.setTargetPosition(position);
            mManager.startSmoothScroll(smoothCenterScroller);
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

    private boolean hasNextCourse() {
        return todayCourses.getNextCourse().getCourseName() != null;
    }

    public boolean isExpanded() {
        return expanded;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case NextCourseViewHolder.VIEW_TYPE:
                view = inflater.inflate(R.layout.item_course_next, parent, false);
                return new NextCourseViewHolder(view);
            case NoNextCourseViewHolder.VIEW_TYPE:
                view = inflater.inflate(R.layout.item_course_no_next, parent, false);
                return new NoNextCourseViewHolder(view);
            case TodayCourseViewHolder.VIEW_TYPE:
                view = inflater.inflate(R.layout.item_course, parent, false);
                return new TodayCourseViewHolder(view);
            case PreScrollViewHolder.VIEW_TYPE:
            default:
                view = inflater.inflate(R.layout.item_empty, parent, false);
                return new PreScrollViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            if (hasNextCourse()) {
                return NextCourseViewHolder.VIEW_TYPE;
            } else {
                return NoNextCourseViewHolder.VIEW_TYPE;
            }
        } else if (position == getItemCount() - 1) {
            if (hasNextCourse()) {
                return PreScrollViewHolder.VIEW_TYPE;
            } else {
                return TodayCourseViewHolder.VIEW_TYPE;
            }
        } else {
            return TodayCourseViewHolder.VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        if (todayCourses != null) {
            if (todayCourses.getCourses().length == 0) {
                return 0;
            }
            if (!hasNextCourse()) {
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

}
