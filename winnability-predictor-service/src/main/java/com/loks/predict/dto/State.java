package com.loks.predict.dto;

import org.apache.commons.math3.util.Precision;

import java.io.Serializable;
import java.util.Objects;

public class State implements Serializable, Comparable<State> {

    private static final long serialVersionUID = 424562754139543L;

    private final Integer id;

    private final String stateName;

    private final Float literacyRate;

    private final Float seatShare;

    private final Float currentVoterTurnout;

    private final Float previousVoterTurnout;

    private final Integer noOfPhases;

    private final String stateCode;

    public State(final Integer id, final String stateName, final Float literacyRate, final Float seatShare,
                 final Float currentVoterTurnout, final Float previousVoterTurnout,
                 final Integer noOfPhases, final String stateCode) {
        this.id = id;
        this.stateName = stateName;
        this.literacyRate = literacyRate;
        this.seatShare = seatShare;
        this.currentVoterTurnout = currentVoterTurnout;
        this.previousVoterTurnout = previousVoterTurnout;
        this.noOfPhases = noOfPhases;
        this.stateCode = stateCode;
    }

    public Integer getId() {
        return id;
    }

    public String getStateName() {
        return stateName;
    }

    public Float getLiteracyRate() {
        return literacyRate;
    }

    public Float getSeatShare() {
        return seatShare;
    }

    protected Float getCurrentVoterTurnout() {
        return currentVoterTurnout;
    }

    protected Float getPreviousVoterTurnout() {
        return previousVoterTurnout;
    }

    public Integer getNoOfPhases() {
        return noOfPhases;
    }

    public String getStateCode() {
        return stateCode;
    }

    public Float getDeltaVoterTurnout(){
        return Precision.round(getCurrentVoterTurnout() - getPreviousVoterTurnout(), 4);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state = (State) o;
        return Objects.equals(getId(), state.getId()) &&
                Objects.equals(getStateName(), state.getStateName()) &&
                Objects.equals(getLiteracyRate(), state.getLiteracyRate()) &&
                Objects.equals(getSeatShare(), state.getSeatShare()) &&
                Objects.equals(getCurrentVoterTurnout(), state.getCurrentVoterTurnout()) &&
                Objects.equals(getPreviousVoterTurnout(), state.getPreviousVoterTurnout()) &&
                Objects.equals(getNoOfPhases(), state.getNoOfPhases()) &&
                Objects.equals(getStateCode(), state.getStateCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getStateName(), getLiteracyRate(), getSeatShare(), getCurrentVoterTurnout(), getPreviousVoterTurnout(), getNoOfPhases(), getStateCode());
    }


    @Override
    public int compareTo(final State state) {
        return this.equals(state) ? 0 : this.id.compareTo(state.id);
    }
}
