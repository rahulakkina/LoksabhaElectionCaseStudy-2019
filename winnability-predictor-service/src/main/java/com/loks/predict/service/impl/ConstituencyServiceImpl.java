package com.loks.predict.service.impl;

import com.loks.predict.dao.PredictorDao;
import com.loks.predict.dto.Constituency;
import com.loks.predict.service.ConstituenciesService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.function.Function;
import java.util.stream.StreamSupport;

@Component
public class ConstituencyServiceImpl implements ConstituenciesService {

    private final PredictorDao predictorDao;

    @Autowired
    public ConstituencyServiceImpl(final PredictorDao predictorDao) {
        this.predictorDao = predictorDao;
    }

    private static Constituency getConstituency(final Row row){
        return new Constituency(row.getInt("INDEX"), row.getString("CONSTITUENCY"),
                row.getString("STATE"),  row.getInt("POSTFIX_CODE"));
    }

    /**
     *
     * @return
     */
    @Override
    public Flux<Constituency> getConstituenciesInfo() {
        return predictorDao.getDatasets().get("constituencies").flatMapMany(new Function<Table, Publisher<Constituency>>() {
            @Override
            public Publisher<Constituency> apply(final Table table) {
                return Flux.fromStream(StreamSupport.stream(table.spliterator(), false).map(ConstituencyServiceImpl::getConstituency));
            }
        });
    }

    /**
     *
     * @param stateName
     * @return
     */
    @Override
    public Flux<Constituency> getConstituenciesByState(final String stateName) {
        return predictorDao.getDatasets().get("constituencies").flatMapMany(new Function<Table, Publisher<Constituency>>() {
            @Override
            public Publisher<Constituency> apply(final Table table) {
                return Flux.fromStream(StreamSupport.stream(table.spliterator(), false)
                        .filter(row -> stateName.equalsIgnoreCase(row.getString("STATE")))
                        .map(ConstituencyServiceImpl::getConstituency));
            }
        });
    }
}
