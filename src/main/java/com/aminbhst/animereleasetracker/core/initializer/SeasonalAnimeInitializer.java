package com.aminbhst.animereleasetracker.core.initializer;

import com.aminbhst.animereleasetracker.core.model.ConfigData;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.core.repository.ConfigDataRepository;
import com.aminbhst.animereleasetracker.util.DateUtils;
import com.aminbhst.animereleasetracker.core.provider.MyAnimeListApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SeasonalAnimeInitializer {

    private final MyAnimeListApi myAnimeListApi;
    private final AnimeTitleRepository animeTitleRepository;
    private final ConfigDataRepository configDataRepository;

    @Autowired
    public SeasonalAnimeInitializer(MyAnimeListApi myAnimeListApi,
                                    AnimeTitleRepository animeTitleRepository,
                                    ConfigDataRepository configDataRepository) {
        this.myAnimeListApi = myAnimeListApi;
        this.animeTitleRepository = animeTitleRepository;
        this.configDataRepository = configDataRepository;
    }

    public void initializeCurrentSeasonalAnime() throws IOException {
        if (shouldSkipInitialization())
            return;

        myAnimeListApi.initializeSeasonalAnime(
                DateUtils.getCurrentYear(),
                DateUtils.getCurrentSeason()
        );
        this.updateConfigData();
    }

    public void initializeSeason(int year, String season) {
        myAnimeListApi.initializeSeasonalAnime(year, season);
    }

    private void updateConfigData() throws JsonProcessingException {
        ConfigData configData = configDataRepository.findById(1L).orElse(null);
        if (configData == null) {
            configData = new ConfigData();
            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("latestInitializedSeason", DateUtils.getCurrentSeason());
            objectNode.put("latestInitializedYear", DateUtils.getCurrentYear());
            configData.setJsonData(objectNode.toString());
            configDataRepository.save(configData);
            return;
        }
        ObjectNode jsonNode = ((ObjectNode) new ObjectMapper().readTree(configData.getJsonData()));
        jsonNode.put("latestInitializedSeason", DateUtils.getCurrentSeason());
        jsonNode.put("latestInitializedYear", DateUtils.getCurrentYear());
        configData.setJsonData(jsonNode.toString());
        configDataRepository.save(configData);
    }

    private boolean shouldSkipInitialization() throws JsonProcessingException {
        String currentSeason = DateUtils.getCurrentSeason();
        String latestInitializedSeason = getLatestInitializedSeason();
        return latestInitializedSeason != null && latestInitializedSeason.equalsIgnoreCase(currentSeason);
    }

    private String getLatestInitializedSeason() throws JsonProcessingException {
        ConfigData configData = configDataRepository.findById(1L).orElse(null);
        if (configData != null && configData.getJsonData() != null) {
            JsonNode jsonNode = new ObjectMapper().readTree(configData.getJsonData());
            JsonNode initializedSeason = jsonNode.get("latestInitializedSeason");
            if (initializedSeason != null && !initializedSeason.isNull())
                return initializedSeason.asText();
        }
        return null;
    }

}
