package com.loks.predict.dao.impl;

import com.google.common.collect.Maps;
import com.loks.predict.dao.PredictorDao;
import com.loks.predict.util.HttpUtility;
import ml.dmlc.xgboost4j.java.Booster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Component
public class PredictorDaoImpl implements PredictorDao {

    private static final Logger logger = LoggerFactory.getLogger(PredictorDaoImpl.class);

    @Autowired
    @Qualifier("datasetResourceMap")
    private Map<String, String> datasetResourceMap;

    @Value("${resource.parent}")
    private String parentUrl;

    @Value("${resource.model}")
    private String modelPostFix;

    @Autowired
    private HttpUtility httpUtility;

    private Map<String, Mono<Table>> datasets;

    private Mono<Booster> booster;

    private Map<Integer, Integer> ageGroupMappings = Maps.newHashMap();

    private Map<Integer, Double> ageGroupedEarningsMappings = Maps.newHashMap();

    @PostConstruct
    @Scheduled(fixedDelayString = "${poll.job.schedule}")
    public void load(){
        logger.info("Refereshing the data tier");

        datasets = Maps.newConcurrentMap();

        final Map<Integer, List<Double>> ageGroupedMap = Maps.newHashMap();

        getDatasetResources().entrySet().parallelStream().forEach(entry -> buildDataset(entry));

        booster = httpUtility.getData(getModelUrl(), httpUtility.getBoosterFunction());

        StreamSupport.stream(datasets.get("age").block().spliterator(), false)
                .forEach(this::buildAgeGroupMap);

        StreamSupport.stream(datasets.get("candidate-analysed").block().spliterator(), false)
                .forEach(row -> collectRow(row, ageGroupedMap));

        ageGroupedMap.entrySet()
                .stream()
                .forEach(entry -> ageGroupedEarningsMappings.put(entry.getKey(), getAverage(entry.getValue())));
    }


    protected Double getAverage(final List<Double> rows){
       return rows.stream().mapToDouble(r -> (double)r).average().getAsDouble();
    }

    protected Map<String, String> getDatasetResources(){
        final Map<String, String> resources = Maps.newConcurrentMap();
        datasetResourceMap.entrySet()
                .parallelStream()
                .forEach(entry ->
                        resources.put(entry.getKey(),
                                new StringBuilder().append(parentUrl).append(entry.getValue()).toString()));
        return resources;
    }

    public void buildAgeGroupMap(final Row row){
        final Integer ageGroup = row.getInt("POINTS");
        IntStream.range(row.getInt("FROM"), row.getInt("TO") + 1)
                .forEach(i -> ageGroupMappings.put(i,ageGroup));
    }

    protected void collectRow(final Row row, final Map<Integer, List<Double>> ageGroupedMap){
        final Integer ageGroup = ageGroupMappings.get(row.getInt("AGE"));
        final Double earnings = row.getDouble("EARNINGS");
        if(!ageGroupedMap.containsKey(ageGroup))
            ageGroupedMap.put(ageGroup, new ArrayList<Double>());
        ageGroupedMap.get(ageGroup).add(earnings);
    }

    public String getModelUrl(){
        return new StringBuilder().append(parentUrl).append(modelPostFix).toString();
    }

    protected void buildDataset(final Map.Entry<String, String> entry){
       datasets.put(entry.getKey(), httpUtility.getData(entry.getValue(), httpUtility.getTableFunction()));
       if(logger.isDebugEnabled()) {
           logger.debug(String.format("Loaded %s dataset", entry.getKey()));
       }
    }

    public Map<String, Mono<Table>> getDatasets(){
        return datasets;
    }

    public Mono<Booster> getModel(){
        return booster;
    }

    public Map<Integer, Integer> getAgeGroupMappings() {
        return ageGroupMappings;
    }

    public Map<Integer, Double> getAgeGroupedEarningsMappings() {
        return ageGroupedEarningsMappings;
    }


}
