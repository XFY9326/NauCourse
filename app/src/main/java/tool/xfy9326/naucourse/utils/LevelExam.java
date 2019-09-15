package tool.xfy9326.naucourse.utils;

public class LevelExam extends BaseData {
    private String[] examType;
    private String[] examName;
    private String[] score1;
    private String[] score2;
    private String[] term;
    private String[] ticketId;
    private String[] certificateId;
    private int examAmount;

    public LevelExam() {
        this.examAmount = 0;
    }

    public int getExamAmount() {
        return examAmount;
    }

    public void setExamAmount(int examAmount) {
        this.examAmount = examAmount;
    }

    public String[] getExamType() {
        return examType;
    }

    public void setExamType(String[] examType) {
        this.examType = examType;
    }

    public String[] getExamName() {
        return examName;
    }

    public void setExamName(String[] examName) {
        this.examName = examName;
    }

    public String[] getScore1() {
        return score1;
    }

    public void setScore1(String[] score1) {
        this.score1 = score1;
    }

    public String[] getScore2() {
        return score2;
    }

    public void setScore2(String[] score2) {
        this.score2 = score2;
    }

    public String[] getTerm() {
        return term;
    }

    public void setTerm(String[] term) {
        this.term = term;
    }

    public String[] getTicketId() {
        return ticketId;
    }

    public void setTicketId(String[] ticketId) {
        this.ticketId = ticketId;
    }

    public String[] getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String[] certificateId) {
        this.certificateId = certificateId;
    }
}
