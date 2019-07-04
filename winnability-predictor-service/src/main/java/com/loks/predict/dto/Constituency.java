package com.loks.predict.dto;

import java.io.Serializable;
import java.util.Objects;

public class Constituency implements Serializable, Comparable<Constituency> {

    private static final long serialVersionUID = 524562854239578L;

    private final Integer id;

    private final String constituencyName;

    private final String stateName;

    private final Integer postfixCode;

    public Constituency(final Integer id, final String constituencyName, final String stateName, final Integer postfixCode) {
        this.id = id;
        this.constituencyName = constituencyName;
        this.stateName = stateName;
        this.postfixCode = postfixCode;
    }

    public Integer getId() {
        return id;
    }

    public String getConstituencyName() {
        return constituencyName;
    }

    public String getStateName() {
        return stateName;
    }

    public Integer getPostfixCode() {
        return postfixCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Constituency)) return false;
        Constituency that = (Constituency) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getConstituencyName(), that.getConstituencyName()) &&
                Objects.equals(getStateName(), that.getStateName()) &&
                Objects.equals(getPostfixCode(), that.getPostfixCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getConstituencyName(), getStateName(), getPostfixCode());
    }


    @Override
    public int compareTo(Constituency constituency) {
        return this.id.compareTo(constituency.id);
    }
}
