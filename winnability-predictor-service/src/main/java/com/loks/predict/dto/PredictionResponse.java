package com.loks.predict.dto;

import org.apache.commons.math3.util.Precision;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PredictionResponse implements Serializable {

    private static final long serialVersionUID = -1326029218394687L;

    private Double score;

    private List<ConstituencyResult> rankings;

    public void setScore(final Double score) {
        this.score = Precision.round(score, 4);
    }

    public void setRankings(final List<ConstituencyResult> rankings) {
        this.rankings = rankings;
    }

    public Double getScore() {
        return score;
    }

    public List<ConstituencyResult> getRankings() {
        return rankings;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PredictionResponse)) return false;
        final PredictionResponse that = (PredictionResponse) o;
        return Objects.equals(getScore(), that.getScore()) &&
                Objects.equals(getRankings(), that.getRankings());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScore(), getRankings());
    }

    @Override
    public String toString() {
        return "PredictionResponse{" +
                "score=" + score +
                ", rankings=" + rankings +
                '}';
    }
}
