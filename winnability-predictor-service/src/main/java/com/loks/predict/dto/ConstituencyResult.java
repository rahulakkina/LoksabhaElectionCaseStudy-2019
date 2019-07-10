package com.loks.predict.dto;

import java.io.Serializable;
import java.util.Objects;

public class ConstituencyResult implements Serializable{

    private static final long serialVersionUID = 775664285425868L;

    private final String candidateName;

    private final Double votingPercentage;

    private final Boolean test;

    public ConstituencyResult(final String candidateName, final Double votingPercentage) {
        this(candidateName, votingPercentage, false);
    }

    public ConstituencyResult(final String candidateName, final Double votingPercentage, final Boolean test) {
        this.candidateName = candidateName;
        this.votingPercentage = votingPercentage * 100.0;
        this.test = test;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public Double getVotingPercentage() {
        return votingPercentage;
    }

    public Boolean getTest() {
        return test;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstituencyResult)) return false;
        final ConstituencyResult constituencyResult = (ConstituencyResult) o;
        return Objects.equals(getCandidateName(), constituencyResult.getCandidateName()) &&
                Objects.equals(getVotingPercentage(), constituencyResult.getVotingPercentage());
    }

    @Override
    public String toString() {
        return "ConstituencyResult{" +
                "candidateName='" + candidateName + '\'' +
                ", votingPercentage=" + votingPercentage +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCandidateName(), getVotingPercentage());
    }

}
