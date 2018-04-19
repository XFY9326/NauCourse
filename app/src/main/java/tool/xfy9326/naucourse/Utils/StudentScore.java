package tool.xfy9326.naucourse.Utils;

/**
 * Created by xfy9326 on 18-2-21.
 * 学分信息
 */

public class StudentScore {
    private String scoreXF = null;
    private String scoreJD = null;
    private String scoreNP = null;
    private String scoreZP = null;
    private String scoreBP = null;
    private int dataVersionCode = 0;

    public StudentScore() {

    }

    public String getScoreXF() {
        return scoreXF;
    }

    public void setScoreXF(String scoreXF) {
        this.scoreXF = scoreXF;
    }

    public String getScoreJD() {
        return scoreJD;
    }

    public void setScoreJD(String scoreJD) {
        this.scoreJD = scoreJD;
    }

    public String getScoreNP() {
        return scoreNP;
    }

    public void setScoreNP(String scoreNP) {
        this.scoreNP = scoreNP;
    }

    public String getScoreZP() {
        return scoreZP;
    }

    public void setScoreZP(String scoreZP) {
        this.scoreZP = scoreZP;
    }

    public String getScoreBP() {
        return scoreBP;
    }

    public void setScoreBP(String scoreBP) {
        this.scoreBP = scoreBP;
    }

    public int getDataVersionCode() {
        return dataVersionCode;
    }

    public void setDataVersionCode(int dataVersionCode) {
        this.dataVersionCode = dataVersionCode;
    }
}
