package tool.xfy9326.naucourse.Utils;

import android.support.annotation.Nullable;

/**
 * Created by xfy9326 on 18-2-22.
 * 学期信息
 */

public class SchoolTime {
    //yyyy-MM-dd
    @Nullable
    private String startTime = null;
    //yyyy-MM-dd
    @Nullable
    private String endTime = null;
    private int weekNum = 0;
    private int dataVersionCode = 0;

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

    @SuppressWarnings("unused")
    public int getDataVersionCode() {
        return dataVersionCode;
    }

    @SuppressWarnings("SameParameterValue")
    public void setDataVersionCode(int dataVersionCode) {
        this.dataVersionCode = dataVersionCode;
    }
}
