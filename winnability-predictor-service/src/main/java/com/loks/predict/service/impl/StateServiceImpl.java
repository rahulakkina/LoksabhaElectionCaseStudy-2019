package com.loks.predict.service.impl;

import com.loks.predict.dao.PredictorDao;
import com.loks.predict.dto.State;
import com.loks.predict.service.StateService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Component
public class StateServiceImpl implements StateService {

    private final PredictorDao predictorDao;

    @Autowired
    public StateServiceImpl(final PredictorDao predictorDao) {
        this.predictorDao = predictorDao;
    }

    protected State getState(final Row row){
        return new State(row.getInt("INDEX"), row.getString("STATE"),
                new BigDecimal(row.getDouble("LITERACY_RATE")).floatValue(),
                new BigDecimal(row.getDouble("SEAT_SHARE")).floatValue(),
                new BigDecimal(row.getDouble("CURRENT_VOTER_TURNOUT")).floatValue(),
                new BigDecimal(row.getDouble("PREVIOUS_VOTER_TURNOUT")).floatValue(),
                row.getInt("NO_OF_PHASES"), row.getString("STATE_CODE"));
    }

    @Override
    public Flux<State> getStatesInfo() {
        return predictorDao.getDatasets().get("states").flatMapMany(new Function<Table, Publisher<State>>() {
            @Override
            public Publisher<State> apply(final Table table) {
                return Flux.fromStream(StreamSupport.stream(table.spliterator(), false).map(row -> getState(row)));
            }
        });
    }

    @Override
    public Mono<State> getStateInfo(final Integer id) {
        return predictorDao.getDatasets().get("states").flatMap(new Function<Table, Mono<State>>() {
            @Override
            public Mono<State> apply(final Table table) {
                return Mono.justOrEmpty(StreamSupport.stream(table.spliterator(), false)
                        .filter(row -> row.getInt("INDEX") == id)
                        .map(row -> getState(row)).findAny());

            }
        });
    }

    @Override
    public Mono<State> getStateInfo(final String stateName) {
        return predictorDao.getDatasets().get("states").flatMap(new Function<Table, Mono<State>>() {
            @Override
            public Mono<State> apply(final Table table) {
                return Mono.justOrEmpty(StreamSupport.stream(table.spliterator(), false)
                        .filter(row -> row.getString("STATE").equalsIgnoreCase(stateName))
                        .map(row -> getState(row)).findAny());

            }
        });
    }
}
