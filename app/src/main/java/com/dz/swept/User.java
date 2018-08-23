package com.dz.swept;

public class User {

    String stringCreator;
    String stringStakeName;
    String stringStatus;
    String stringParticipants;
    String stringFormat;
    String stringReward;
    String stringInvitees;
    String userId;


    public User(String userID, String stringCreator, String stringStakeName, String stringStatus, String stringParticipants, String stringFormat, String stringReward, String stringInvitees){
        setUserId(userID);
        this.stringCreator = stringCreator;
        this.stringStakeName = stringStakeName;
        this.stringStatus = stringStatus;
        this.stringParticipants = stringParticipants;
        this.stringFormat = stringFormat;
        this.stringReward = stringReward;
        this.stringInvitees = stringInvitees;

    }

    public String getStringCreator() {
        return stringCreator;
    }

    public void setStringCreator(String stringCreator) {
        this.stringCreator = stringCreator;
    }

    public String getStringFormat() {
        return stringFormat;
    }

    public void setStringFormat(String stringFormat) {
        this.stringFormat = stringFormat;
    }

    public String getStringStakeName() {
        return stringStakeName;
    }

    public void setStringStakeName(String stringStakeName) {
        this.stringStakeName = stringStakeName;
    }

    public String getStringStatus() {
        return stringStatus;
    }

    public void setStringStatus(String stringStatus) {
        this.stringStatus = stringStatus;
    }

    public String getStringParticipants() {
        return stringParticipants;
    }

    public void setStringParticipants(String stringParticipants) {
        this.stringParticipants = stringParticipants;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStringReward() {
        return stringReward;
    }

    public void setStringReward(String stringReward) {
        this.stringReward = stringReward;
    }

    public String getStringInvitees() {
        return stringInvitees;
    }

    public void setStringInvitees(String stringInvitees) {
        this.stringInvitees = stringInvitees;
    }
}
