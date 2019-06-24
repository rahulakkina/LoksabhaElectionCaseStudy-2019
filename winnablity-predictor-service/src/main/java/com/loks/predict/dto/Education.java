package com.loks.predict.dto;

import java.io.Serializable;
import java.util.Objects;

public class Education implements Serializable, Comparable<Education> {

    private static final long serialVersionUID = 371562854234298L;

    private final Integer points;

    private final String education;

    public Education(final Integer points, final String education) {
        this.points = points;
        this.education = education;
    }

    public Integer getPoints() {
        return points;
    }

    public String getEducation() {
        return education;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Education)) return false;
        Education education1 = (Education) o;
        return Objects.equals(getPoints(), education1.getPoints()) &&
                Objects.equals(getEducation(), education1.getEducation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPoints(), getEducation());
    }

    @Override
    public int compareTo(final Education education) {
        return education.points.compareTo(education.points);
    }
}
