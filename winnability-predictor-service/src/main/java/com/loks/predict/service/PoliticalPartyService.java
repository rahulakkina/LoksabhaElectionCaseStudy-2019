package com.loks.predict.service;

import com.loks.predict.dto.PoliticalParty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PoliticalPartyService {
    Flux<PoliticalParty> getPoliticalPartiesInfo();
    Mono<PoliticalParty> getPoliticalParty(final Integer id);
    Mono<PoliticalParty> getPoliticalParty(final String partyName);
}
