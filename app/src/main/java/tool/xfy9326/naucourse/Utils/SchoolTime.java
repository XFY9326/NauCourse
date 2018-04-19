package tool.xfy9326.naucourse.Utils;

/**
 * Created by xfy9326 on 18-2-22.
 * 学期信息
 */

public class SchoolTime {
    //yyyy-MM-dd
    private String startTime = null;
    //yyyy-MM-dd
    private String endTime = null;
    private int weekNum = 0;
    private int dataVersionCode = 0;

    public SchoolTime() {
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getWeekNum() {
        return weekNum;
    }

    public void setWeekNum(int weekNum) {
        this.weekNum = weekNum;
    }

    public int getDataVersionCode() {
        return dataVersionCode;
    }

    public void setDataVersionCode(int dataVersionCode) {
        this.dataVersionCode = dataVersionCode;
    }
}
