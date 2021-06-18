package com.loks.predict.dao.impl;

import com.google.common.collect.Maps;
import com.loks.predict.dao.PredictorDao;
import com.loks.predict.util.ResourceUtility;
import ml.dmlc.xgboost4j.java.Booster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Component
public class PredictorDaoImpl implements PredictorDao {

    private static final Logger logger = LoggerFactory.getLogger(PredictorDaoImpl.class);

    @Autowired
    @Qualifier("datasetResourceMap")
    private Map<String, String> datasetResourceMap;

    @Autowired
    @Qualifier("parentUrl")
    private String parentUrl;

    @Autowired
    @Qualifier("modelPostFix")
    private String modelPostFix;

    @Autowired
    @Qualifier("resourceUtility")
    private ResourceUtility resourceUtility;

    private Map<String, Mono<Table>> datasets;

    private Mono<Booster> booster;

    private final Map<Integer, Integer> ageGroupMappings = Maps.newHashMap();

    private final Map<Integer, Double> ageGroupedEarningsMappings = Maps.newConcurrentMap();

    @PostConstruct
    @Scheduled(fixedDelayString = "${poll.job.schedule}")
    public void load(){
        logger.info("Refreshing the data tier");

        datasets = Maps.newConcurrentMap();

        final Map<Integer, List<Double>> ageGroupedMap = Maps.newHashMap();

        getDatasetResources().entrySet().parallelStream().forEach(this::buildDataset);

        booster = resourceUtility.getData(getModelUrl(), resourceUtility.getBoosterFunction());

        StreamSupport.stream(
                Objects.requireNonNull(datasets.get("age").block()).spliterator(), false)
                .forEach(this::buildAgeGroupMap);

        StreamSupport.stream(
                Objects.requireNonNull(datasets.get("candidate-analysed").block())
                        .spliterator(), false)
                .forEach(row -> collectRow(row, ageGroupedMap));

        ageGroupedMap.entrySet().parallelStream()
                .forEach(entry -> ageGroupedEarningsMappings.put(entry.getKey(), getAverage(entry.getValue())));
    }


    protected Double getAverage(final List<Double> rows){
       return rows.stream()
               .mapToDouble(Objects::requireNonNull)
               .average()
               .orElse(0.0);
    }

    protected Map<String, String> getDatasetResources(){
        final Map<String, String> resources = Maps.newConcurrentMap();
        datasetResourceMap.entrySet()
                .parallelStream()
                .forEach(entry ->
                        resources.put(entry.getKey(), getUrl(parentUrl,entry.getValue())));
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
        if(!ageGroupedMap.containsKey(ageGroup)) {
            ageGroupedMap.put(ageGroup, new ArrayList<>());
        }
        ageGroupedMap.get(ageGroup).add(earnings);
    }

    public String getModelUrl(){
        return getUrl(parentUrl, modelPostFix);
    }

    protected void buildDataset(final Map.Entry<String, String> entry){
       datasets.put(entry.getKey(), resourceUtility.getData(entry.getValue(), resourceUtility.getTableFunction()));
       if(logger.isDebugEnabled()) {
           logger.debug(String.format("Loaded %s dataset", entry.getKey()));
       }
    }

    protected String getUrl(final String parentUrl, final String postFix){
        return parentUrl + postFix;
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
