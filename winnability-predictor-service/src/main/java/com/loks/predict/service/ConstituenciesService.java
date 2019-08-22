package com.loks.predict.service;

import com.loks.predict.dto.Constituency;
import com.loks.predict.dto.ConstituencyResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ConstituenciesService {

    Flux<Constituency> getConstituenciesInfo();

    Flux<Constituency> getConstituenciesByState(final String stateName);

    Mono<Constituency> getConstituencyInfo(final Integer id);

    List<ConstituencyResult> getConstituencyResults(final Integer constituencyId);
}
