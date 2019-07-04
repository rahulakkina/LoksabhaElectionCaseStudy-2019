package com.loks.predict.service;

import com.loks.predict.dto.Candidate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CandidateService {
    Flux<Candidate> getContestantByKeyWord(final String keyWord);
    Mono<Candidate> getContestantByCandidateId(final Integer candidateId);
}
