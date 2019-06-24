package com.loks.predict.service.impl;

import com.loks.predict.dao.PredictorDao;
import com.loks.predict.dto.Education;
import com.loks.predict.service.EducationInfoService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.function.Function;
import java.util.stream.StreamSupport;

@Component
public class EducationInfoServiceImpl implements EducationInfoService {

    private final PredictorDao predictorDao;

    @Autowired
    public EducationInfoServiceImpl(final PredictorDao predictorDao) {
        this.predictorDao = predictorDao;
    }

    @Override
    public Flux<Education> getEducationInfo() {
        return predictorDao.getDatasets().get("education").flatMapMany(new Function<Table, Publisher<Education>>() {
            @Override
            public Publisher<Education> apply(final Table table) {
                return Flux.fromStream(StreamSupport.stream(table.spliterator(), false).map(this::getEducation));
            }

            private Education getEducation(final Row row){
                return new Education(row.getInt("POINTS"), row.getString("EDUCATION"));
            }
        });
    }
}
