package com.loks.predict.dto;

import java.io.Serializable;
import java.util.Objects;

public class PredictionParameters implements Serializable{

    private static final long serialVersionUID = -8706029218394789L;

    private String candidateName;

    private Integer age;

    private Integer constituencyId;

    private Integer stateId;

    private Integer partyId;

    private Integer numberOfPendingCriminalCases;

    private Double earnedIncome;

    private Double liabilities;

    private Double stateLiteracyRate;

    private Double stateSeatShare;

    private Integer partyGroupId;

    private Integer educationGroupId;

    private Double deltaStateVoterTurnout;

    private Integer numberOfPhases;

    private Boolean recontest;

    private Boolean sex;

    private Integer numberOfMediaItems;

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(final String candidateName) {
        this.candidateName = candidateName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    public Integer getConstituencyId() {
        return constituencyId;
    }

    public void setConstituencyId(final Integer constituencyId) {
        this.constituencyId = constituencyId;
    }

    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(final Integer stateId) {
        this.stateId = stateId;
    }

    public Integer getPartyId() {
        return partyId;
    }

    public void setPartyId(final Integer partyId) {
        this.partyId = partyId;
    }

    public Integer getNumberOfPendingCriminalCases() {
        return numberOfPendingCriminalCases;
    }

    public void setNumberOfPendingCriminalCases(final Integer numberOfPendingCriminalCases) {
        this.numberOfPendingCriminalCases = numberOfPendingCriminalCases;
    }

    public Double getEarnedIncome() {
        return earnedIncome;
    }

    public void setEarnedIncome(final Double earnedIncome) {
        this.earnedIncome = earnedIncome;
    }

    public Double getLiabilities() {
        return liabilities;
    }

    public void setLiabilities(final Double liablities) {
        this.liabilities = liablities;
    }

    public Double getStateLiteracyRate() {
        return stateLiteracyRate;
    }

    public void setStateLiteracyRate(final Double stateLiteracyRate) {
        this.stateLiteracyRate = stateLiteracyRate;
    }

    public Double getStateSeatShare() {
        return stateSeatShare;
    }

    public void setStateSeatShare(final Double stateSeatShare) {
        this.stateSeatShare = stateSeatShare;
    }

    public Integer getPartyGroupId() {
        return partyGroupId;
    }

    public void setPartyGroupId(final Integer partyGroupId) {
        this.partyGroupId = partyGroupId;
    }

    public Integer getEducationGroupId() {
        return educationGroupId;
    }

    public void setEducationGroupId(final Integer educationGroupId) {
        this.educationGroupId = educationGroupId;
    }

    public Double getDeltaStateVoterTurnout() {
        return deltaStateVoterTurnout;
    }

    public void setDeltaStateVoterTurnout(final Double deltaStateVoterTurnout) {
        this.deltaStateVoterTurnout = deltaStateVoterTurnout;
    }

    public Integer getNumberOfPhases() {
        return numberOfPhases;
    }

    public void setNumberOfPhases(final Integer numberOfPhases) {
        this.numberOfPhases = numberOfPhases;
    }

    public Boolean getRecontest() {
        return recontest;
    }

    public void setRecontest(final Boolean recontest) {
        this.recontest = recontest;
    }

    public Boolean getSex() {
        return sex;
    }

    public void setSex(final Boolean sex) {
        this.sex = sex;
    }

    public Integer getNumberOfMediaItems() {
        return numberOfMediaItems;
    }

    public void setNumberOfMediaItems(final Integer numberOfMediaItems) {
        this.numberOfMediaItems = numberOfMediaItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PredictionParameters)) return false;
        PredictionParameters that = (PredictionParameters) o;
        return Objects.equals(getCandidateName(), that.getCandidateName()) &&
                Objects.equals(getAge(), that.getAge()) &&
                Objects.equals(getConstituencyId(), that.getConstituencyId()) &&
                Objects.equals(getStateId(), that.getStateId()) &&
                Objects.equals(getPartyId(), that.getPartyId()) &&
                Objects.equals(getNumberOfPendingCriminalCases(), that.getNumberOfPendingCriminalCases()) &&
                Objects.equals(getEarnedIncome(), that.getEarnedIncome()) &&
                Objects.equals(getLiabilities(), that.getLiabilities()) &&
                Objects.equals(getStateLiteracyRate(), that.getStateLiteracyRate()) &&
                Objects.equals(getStateSeatShare(), that.getStateSeatShare()) &&
                Objects.equals(getPartyGroupId(), that.getPartyGroupId()) &&
                Objects.equals(getEducationGroupId(), that.getEducationGroupId()) &&
                Objects.equals(getDeltaStateVoterTurnout(), that.getDeltaStateVoterTurnout()) &&
                Objects.equals(getNumberOfPhases(), that.getNumberOfPhases()) &&
                Objects.equals(getRecontest(), that.getRecontest()) &&
                Objects.equals(getSex(), that.getSex()) &&
                Objects.equals(getNumberOfMediaItems(), that.getNumberOfMediaItems());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCandidateName(), getAge(), getConstituencyId(),
                getStateId(), getPartyId(), getNumberOfPendingCriminalCases(), getEarnedIncome(),
                getLiabilities(), getStateLiteracyRate(), getStateSeatShare(), getPartyGroupId(),
                getEducationGroupId(), getDeltaStateVoterTurnout(), getNumberOfPhases(),
                getRecontest(), getSex(), getNumberOfMediaItems());
    }

    @Override
    public String toString() {
        return "{" +
                "candidateName='" + candidateName + '\'' +
                ", age=" + age +
                ", constituencyId=" + constituencyId +
                ", stateId=" + stateId +
                ", partyId=" + partyId +
                ", numberOfPendingCriminalCases=" + numberOfPendingCriminalCases +
                ", earnedIncome=" + earnedIncome +
                ", liabilities=" + liabilities +
                ", stateLiteracyRate=" + stateLiteracyRate +
                ", stateSeatShare=" + stateSeatShare +
                ", partyGroupId=" + partyGroupId +
                ", educationGroupId=" + educationGroupId +
                ", deltaStateVoterTurnout=" + deltaStateVoterTurnout +
                ", numberOfPhases=" + numberOfPhases +
                ", recontest=" + recontest +
                ", sex=" + sex +
                ", numberOfMediaItems=" + numberOfMediaItems +
                '}';
    }
}
