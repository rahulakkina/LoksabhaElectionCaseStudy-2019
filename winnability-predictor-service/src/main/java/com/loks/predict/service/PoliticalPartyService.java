package com.loks.predict.service;

import com.loks.predict.dto.PoliticalParty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PoliticalPartyService {
    Flux<PoliticalParty> getPoliticalPartiesInfo();
    public Mono<PoliticalParty> getPoliticalParty(final Integer id);
}
