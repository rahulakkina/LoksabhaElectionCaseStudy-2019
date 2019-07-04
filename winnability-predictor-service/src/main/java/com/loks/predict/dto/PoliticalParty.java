package com.loks.predict.dto;

import java.io.Serializable;
import java.util.Objects;

public class PoliticalParty implements Serializable, Comparable<PoliticalParty> {

    private static final long serialVersionUID = -110562938239414L;

    private final Integer id;

    private final String partyName;

    private final Integer points;

    public PoliticalParty(final Integer id, final String partyName, final Integer points) {
        this.id = id;
        this.partyName = partyName;
        this.points = points;
    }

    public Integer getId() {
        return id;
    }

    public String getPartyName() {
        return partyName;
    }

    public Integer getPoints() {
        return points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PoliticalParty)) return false;
        PoliticalParty that = (PoliticalParty) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getPartyName(), that.getPartyName()) &&
                Objects.equals(getPoints(), that.getPoints());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPartyName(), getPoints());
    }


    @Override
    public int compareTo(PoliticalParty politicalParty) {
        return this.id.compareTo(politicalParty.id);
    }
}
