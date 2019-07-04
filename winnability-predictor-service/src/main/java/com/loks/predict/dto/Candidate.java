package com.loks.predict.dto;

import java.io.Serializable;
import java.util.Objects;

public class Candidate implements Serializable {

    private static final long serialVersionUID = -14553663385344462L;

    private final Integer candidateId;

    private final String candidateName;

    private final Integer age;

    private final Integer partyId;

    private final Integer numberOfPendingCriminalCases;

    private final Integer educationGroupId;

    private final Double earnings;

    private final Double mediaPopularity;

    private final Boolean recontesting;

    private final Boolean sex;

    public Candidate(final Integer candidateId, final String candidateName,
                     final Integer age, final Integer partyId, final Integer educationGroupId,
                     final Integer numberOfPendingCriminalCases,
                     final Double earnings, final Double mediaPopularity,
                     final Boolean recontesting, final Boolean sex) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.age = age;
        this.partyId = partyId;
        this.educationGroupId = educationGroupId;
        this.numberOfPendingCriminalCases = numberOfPendingCriminalCases;
        this.earnings = earnings;
        this.mediaPopularity = mediaPopularity;
        this.recontesting = recontesting;
        this.sex = sex;
    }

    public Integer getCandidateId() {
        return candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public Integer getAge() {
        return age;
    }

    public Integer getPartyId() {
        return partyId;
    }

    public Integer getEducationGroupId() {
        return educationGroupId;
    }

    public Double getEarnings() {
        return earnings;
    }

    public Double getMediaPopularity() {
        return mediaPopularity;
    }

    public Boolean getRecontesting() {
        return recontesting;
    }

    public Boolean getSex() {
        return sex;
    }

    public Integer getNumberOfPendingCriminalCases() {
        return numberOfPendingCriminalCases;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Candidate)) return false;
        final Candidate that = (Candidate) o;
        return Objects.equals(getCandidateId(), that.getCandidateId()) &&
                Objects.equals(getCandidateName(), that.getCandidateName()) &&
                Objects.equals(getAge(), that.getAge()) &&
                Objects.equals(getPartyId(), that.getPartyId()) &&
                Objects.equals(getEducationGroupId(), that.getEducationGroupId()) &&
                Objects.equals(getEarnings(), that.getEarnings()) &&
                Objects.equals(getMediaPopularity(), that.getMediaPopularity()) &&
                Objects.equals(getRecontesting(), that.getRecontesting()) &&
                Objects.equals(getSex(), that.getSex());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCandidateId(), getCandidateName(), getAge(), getPartyId(), getEducationGroupId(),
                getEarnings(), getMediaPopularity(), getRecontesting(), getSex());
    }
}
