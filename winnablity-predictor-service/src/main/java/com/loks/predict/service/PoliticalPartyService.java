package com.loks.predict.service;

import com.loks.predict.dto.PoliticalParty;
import reactor.core.publisher.Flux;

public interface PoliticalPartyService {
    Flux<PoliticalParty> getPoliticalPartiesInfo();
}
