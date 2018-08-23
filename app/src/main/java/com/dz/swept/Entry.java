package com.dz.swept;

public class Entry {

    String stringUserEmail;
    String stringSingleScore;
    String stringScoreScore1;
    String stringScoreScore2;
    String stringWinner;
    String stringformat;
    String userId;


    public Entry(String userID, String stringUserEmail, String stringSingleScore, String  stringScoreScore1, String  stringScoreScore2, String stringWinner,String stringformat ){
        setUserId(userID);
        this.stringUserEmail = stringUserEmail;
        this.stringSingleScore =  stringSingleScore;
        this.stringScoreScore1 =  stringScoreScore1;
        this.stringScoreScore2 = stringScoreScore2;
        this.stringWinner= stringWinner;
        this.stringformat= stringformat;


    }

    public String getStringUserEmail() {
        return stringUserEmail;
    }

    public void setStringUserEmail(String stringUserEmail) {
        this.stringUserEmail = stringUserEmail;
    }

    public String getStringSingleScore() {
        return stringSingleScore;
    }

    public void setStringSingleScore(String stringSingleScore) {
        this.stringSingleScore = stringSingleScore;
    }

    public String getStringScoreScore1() {
        return stringScoreScore1;
    }

    public void setStringScoreScore1(String stringScoreScore1) {
        this.stringScoreScore1 = stringScoreScore1;
    }

    public String getStringScoreScore2() {
        return stringScoreScore2;
    }

    public void setStringScoreScore2(String stringScoreScore2) {
        this.stringScoreScore2 = stringScoreScore2;
    }

    public String getStringWinner() {
        return stringWinner;
    }

    public void setStringWinner(String stringWinner) {
        this.stringWinner = stringWinner;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStringformat() {
        return stringformat;
    }

    public void setStringformat(String stringformat) {
        this.stringformat = stringformat;
    }
}
