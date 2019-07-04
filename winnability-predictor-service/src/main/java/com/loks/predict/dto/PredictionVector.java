package com.loks.predict.dto;

import org.apache.commons.math3.util.Precision;

import java.io.Serializable;
import java.util.Arrays;

public class PredictionVector implements Serializable {

    private static final long serialVersionUID = -206110488394799L;

    private float vector[] = new float[15];

    private String labels[] = {"AGE GROUP", "CONSTITUENCY", "STATE", "PARTY", "NUMBER OF PENDING CRIMINAL CASES",
            "EARNINGS", "STATE LITERACY", "STATE SEAT SHARE", "NATIONAL/STATE PARTY", "EDUCATION",
            "STATE WIDE VOTER TURNOUT", "RECONTESTANT", "SEX", "POPULARITY IN MEDIA"};

    public void setAgeGroupId(final Integer ageGroupId){
        vector[0] = ageGroupId;
    }

    public void setConstituencyId(final Integer constituencyId){
        vector[1] = constituencyId;
    }

    public void setStateId(final Integer stateId){
        vector[2] = stateId;
    }

    public void setPartyId(final Integer partyId){
        vector[3] = partyId;
    }

    public void setNumberOfPendingCriminalCases(final Integer numberOfPendingCriminalCases){
        vector[4] = numberOfPendingCriminalCases;
    }

    public void setEarningPoints(final Integer earningPoints){
        vector[5] = earningPoints;
    }

    public void setStateLiteracyRate(final double stateLiteracyRate){
        vector[6] = (float)stateLiteracyRate;
    }

    public void setStateSeatShare(final double stateSeatShare){
        vector[7] = (float)stateSeatShare;
    }

    public void setPartyGroupId(final Integer partyGroupId){
        vector[8] = partyGroupId;
    }

    public void setEducationGroupId(final Integer educationGroupId) {
        vector[9] = educationGroupId;
    }

    public void setDeltaStateVoterTurnout(final double deltaStateVoterTurnout){
        vector[10] = (float)deltaStateVoterTurnout;
    }

    public void setNumberOfPhases(final Integer numberOfPhases) {
        vector[11] = numberOfPhases;
    }

    public void setRecontest(final Boolean recontest) {
        vector[12] = recontest ? 1 : 0;
    }

    public void setSex(final Boolean sex) {
        vector[13] = sex ? 1 : 0;
    }

    public void setNumberOfMediaItems(final Integer numberOfMediaItems){
        vector[14] = Precision.round(((float)numberOfMediaItems / 100.0f), 4);
    }

    public float[] getVector() {
        return vector;
    }

    public Integer vectorSize(){
        return vector.length;
    }

    public String getLabel(final Integer index){
        return labels[index];
    }

    @Override
    public String toString() {
        return "PredictionVector{" +
                "vector=" + Arrays.toString(vector) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PredictionVector)) return false;
        final PredictionVector that = (PredictionVector) o;
        return Arrays.equals(getVector(), that.getVector());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getVector());
    }

}
