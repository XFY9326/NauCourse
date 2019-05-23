package tool.xfy9326.naucourse.utils;

import androidx.annotation.Nullable;

/**
 * Created by xfy9326 on 18-2-22.
 * 学期信息
 */

public class SchoolTime extends BaseData {
    //yyyy-MM-dd
    @Nullable
    private String startTime = null;
    //yyyy-MM-dd
    @Nullable
    private String endTime = null;
    private int weekNum = 0;

    public SchoolTime() {
    }

    @Nullable
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(@Nullable String startTime) {
        this.startTime = startTime;
    }

    @Nullable
    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(@Nullable String endTime) {
        this.endTime = endTime;
    }

    public int getWeekNum() {
        return weekNum;
    }

    public void setWeekNum(int weekNum) {
        this.weekNum = weekNum;
    }
}
