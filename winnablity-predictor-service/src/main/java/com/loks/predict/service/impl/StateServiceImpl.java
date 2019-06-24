package com.loks.predict.service.impl;

import com.loks.predict.dao.PredictorDao;
import com.loks.predict.dto.State;
import com.loks.predict.service.StateService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
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

    @Override
    public Flux<State> getStatesInfo() {
        return predictorDao.getDatasets().get("states").flatMapMany(new Function<Table, Publisher<State>>() {
            @Override
            public Publisher<State> apply(final Table table) {
                return Flux.fromStream(StreamSupport.stream(table.spliterator(), false).map(this::getState));
            }

            protected State getState(final Row row){
                return new State(row.getInt("INDEX"), row.getString("STATE"),
                        new BigDecimal(row.getDouble("LITERACY_RATE")).floatValue(),
                        new BigDecimal(row.getDouble("SEAT_SHARE")).floatValue(),
                        new BigDecimal(row.getDouble("CURRENT_VOTER_TURNOUT")).floatValue(),
                        new BigDecimal(row.getDouble("PREVIOUS_VOTER_TURNOUT")).floatValue(),
                        row.getInt("NO_OF_PHASES"), row.getString("STATE_CODE"));
            }
        });
    }
}
