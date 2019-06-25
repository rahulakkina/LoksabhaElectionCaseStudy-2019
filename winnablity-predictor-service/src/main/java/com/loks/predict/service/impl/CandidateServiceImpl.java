package com.loks.predict.service.impl;

import com.loks.predict.dao.PredictorDao;
import com.loks.predict.dto.Candidate;
import com.loks.predict.service.CandidateService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.function.Function;
import java.util.stream.StreamSupport;

@Component
public class CandidateServiceImpl implements CandidateService {

    private static final Logger logger = LoggerFactory.getLogger(CandidateServiceImpl.class);

    private final PredictorDao predictorDao;

    @Autowired
    public CandidateServiceImpl(final PredictorDao predictorDao) {
        this.predictorDao = predictorDao;
    }

    private static Candidate getCandidate(final Row row){
        return new Candidate(row.getInt("CANDIDATE_ID"),
                row.getString("CANDIDATE_NAME"),
                row.getInt("AGE"),
                row.getInt("PARTY_INDEX"),
                row.getInt("EDUCATION_GROUP_IDX"),
                row.getInt("NO_PENDING_CRIMINAL_CASES"),
                row.getDouble("EARNINGS"),
                row.getDouble("MEDIA_POPULARITY"),
                (row.getInt("RE_CONTEST") == 1 ? true : false),
                ("F".equalsIgnoreCase(row.getString("SEX")) ? true : false));
    }

    @Override
    public Flux<Candidate> getContestantByKeyWord(final String keyWord) {
        return predictorDao.getDatasets().get("candidate-analysed").flatMapMany(new Function<Table, Publisher<Candidate>>() {
            @Override
            public Publisher<Candidate> apply(final Table table) {
                return Flux.fromStream(StreamSupport.stream(table.spliterator(), false)
                        .filter(row ->
                                row.getString("CANDIDATE_NAME")
                                        .toUpperCase()
                                        .contains(keyWord.toUpperCase()))
                        .map(CandidateServiceImpl::getCandidate));
            }
        });
    }

    @Override
    public Mono<Candidate> getContestantByCandidateId(final Integer candidateId) {
        return predictorDao.getDatasets().get("candidate-analysed").flatMap(new Function<Table, Mono<Candidate>>() {
            @Override
            public Mono<Candidate> apply(final Table table) {
                return Mono.justOrEmpty(StreamSupport.stream(table.spliterator(), false)
                        .filter(row -> row.getInt("CANDIDATE_ID") == candidateId)
                        .map(CandidateServiceImpl::getCandidate).findAny());

            }
        });
    }
}
