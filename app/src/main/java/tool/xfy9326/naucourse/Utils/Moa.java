package tool.xfy9326.naucourse.Utils;

public class Moa {
    private String[] id;
    private String[] title;
    private String[] reporter;
    private String[] type;
    private String[] location;
    private String[] time;
    private String[] applyUnit;

    private int count = 0;

    private int dataVersionCode = 0;

    public Moa() {

    }

    @SuppressWarnings("unused")
    public String[] getId() {
        return id;
    }

    public void setId(String[] id) {
        this.id = id;
    }

    public String[] getTitle() {
        return title;
    }

    public void setTitle(String[] title) {
        this.title = title;
    }

    public String[] getReporter() {
        return reporter;
    }

    public void setReporter(String[] reporter) {
        this.reporter = reporter;
    }

    public String[] getType() {
        return type;
    }

    public void setType(String[] type) {
        this.type = type;
    }

    public String[] getLocation() {
        return location;
    }

    public void setLocation(String[] location) {
        this.location = location;
    }

    public String[] getTime() {
        return time;
    }

    public void setTime(String[] time) {
        this.time = time;
    }

    public String[] getApplyUnit() {
        return applyUnit;
    }

    public void setApplyUnit(String[] applyUnit) {
        this.applyUnit = applyUnit;
    }

    @SuppressWarnings("unused")
    public int getDataVersionCode() {
        return dataVersionCode;
    }

    @SuppressWarnings("SameParameterValue")
    public void setDataVersionCode(int dataVersionCode) {
        this.dataVersionCode = dataVersionCode;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
