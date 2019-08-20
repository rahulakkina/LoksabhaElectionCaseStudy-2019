package com.loks.predict.service.impl;

import com.loks.predict.dao.PredictorDao;
import com.loks.predict.dto.PoliticalParty;
import com.loks.predict.service.PoliticalPartyService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.function.Function;
import java.util.stream.StreamSupport;

@Component
public class PoliticalPartyServiceImpl implements PoliticalPartyService {

    private final PredictorDao predictorDao;

    @Autowired
    public PoliticalPartyServiceImpl(final PredictorDao predictorDao) {
        this.predictorDao = predictorDao;
    }

    @Override
    public Flux<PoliticalParty> getPoliticalPartiesInfo() {
        return predictorDao.getDatasets().get("political-parties").flatMapMany(new Function<Table, Publisher<PoliticalParty>>() {
            @Override
            public Publisher<PoliticalParty> apply(final Table table) {
                return Flux.fromStream(StreamSupport.stream(table.spliterator(), false).map(row -> getPoliticalParty(row)));
            }
        });
    }

    @Override
    public Mono<PoliticalParty> getPoliticalParty(final Integer id) {
        return predictorDao.getDatasets().get("political-parties").flatMap(new Function<Table, Mono<PoliticalParty>>() {
            @Override
            public Mono<PoliticalParty> apply(final Table table) {
                return Mono.justOrEmpty(StreamSupport.stream(table.spliterator(), false)
                        .filter(row -> row.getInt("INDEX") == id)
                        .map(row -> getPoliticalParty(row)).findAny());

            }
        });
    }

    private static PoliticalParty getPoliticalParty(final Row row){
        return new PoliticalParty(row.getInt("INDEX"), row.getString("PARTY"),
                row.getInt("POINTS"));
    }
}
