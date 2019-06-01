package tool.xfy9326.naucourse.utils;

import androidx.annotation.Nullable;

/**
 * Created by xfy9326 on 18-2-21.
 * 学分信息
 */

public class StudentScore extends BaseData {
    @Nullable
    private String scoreXF = null;
    @Nullable
    private String scoreJD = null;
    @Nullable
    private String scoreNP = null;
    @Nullable
    private String scoreZP = null;
    @Nullable
    private String scoreBP = null;

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

    @Nullable
    public String getScoreNP() {
        return scoreNP;
    }

    public void setScoreNP(@Nullable String scoreNP) {
        this.scoreNP = scoreNP;
    }

    @Nullable
    public String getScoreBP() {
        return scoreBP;
    }

    public void setScoreBP(@Nullable String scoreBP) {
        this.scoreBP = scoreBP;
    }
}