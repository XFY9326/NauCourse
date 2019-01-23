package tool.xfy9326.naucourse.Utils;

import java.io.Serializable;

@SuppressWarnings("unused")
public class BaseData implements Serializable {
    private int dataVersionCode = 0;

    public int getDataVersionCode() {
        return dataVersionCode;
    }

    public void setDataVersionCode(int dataVersionCode) {
        this.dataVersionCode = dataVersionCode;
    }
}
