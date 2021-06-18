package com.loks.predict.service.impl;

import com.loks.predict.dao.PredictorDao;
import com.loks.predict.dto.State;
import com.loks.predict.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.tablesaw.api.Row;

import java.math.BigDecimal;

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
                BigDecimal.valueOf(row.getDouble("LITERACY_RATE")).floatValue(),
                BigDecimal.valueOf(row.getDouble("SEAT_SHARE")).floatValue(),
                BigDecimal.valueOf(row.getDouble("CURRENT_VOTER_TURNOUT")).floatValue(),
                BigDecimal.valueOf(row.getDouble("PREVIOUS_VOTER_TURNOUT")).floatValue(),
                row.getInt("NO_OF_PHASES"),
                row.getString("STATE_CODE"));

    }

    @Override
    public Flux<State> getStatesInfo() {
        return predictorDao.getDatasets().get("states")
                .flatMapMany(
                         table ->
                                 Flux.fromStream(StreamSupport.stream(table.spliterator(), false)
                                         .map(this::getState)));
    }

    @Override
    public Mono<State> getStateInfo(final Integer id) {
        return predictorDao.getDatasets().get("states")
                .flatMap(
                         table ->
                                 Mono.justOrEmpty(
                                         StreamSupport.stream(table.spliterator(), false)
                                                 .filter(row -> row.getInt("INDEX") == id)
                                                 .map(this::getState)
                                                 .findAny()
                                 )
                );
    }

    @Override
    public Mono<State> getStateInfo(final String stateName) {
        return predictorDao.getDatasets().get("states")
                .flatMap(
                        table ->
                                Mono.justOrEmpty(
                                        StreamSupport.stream(table.spliterator(), false)
                                                .filter(row ->
                                                        row.getString("STATE").equalsIgnoreCase(stateName))
                                                .map(this::getState)
                                                .findAny()
                                )
                );
    }
}
