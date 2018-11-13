package tool.xfy9326.naucourse.Utils;

import androidx.annotation.Nullable;

/**
 * Created by xfy9326 on 18-2-21.
 * 学分信息
 */

public class StudentScore {
    @Nullable
    private String scoreXF = null;
    @Nullable
    private String scoreJD = null;
    @Nullable
    private String scoreZP = null;
    private int dataVersionCode = 1;

    public StudentScore() {

    }

    @Nullable
    public String getScoreXF() {
        return scoreXF;
    }

    public void setScoreXF(@Nullable String scoreXF) {
        this.scoreXF = scoreXF;
    }

    @Nullable
    public String getScoreJD() {
        return scoreJD;
    }

    public void setScoreJD(@Nullable String scoreJD) {
        this.scoreJD = scoreJD;
    }

    @Nullable
    public String getScoreZP() {
        return scoreZP;
    }

    public void setScoreZP(@Nullable String scoreZP) {
        this.scoreZP = scoreZP;
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
