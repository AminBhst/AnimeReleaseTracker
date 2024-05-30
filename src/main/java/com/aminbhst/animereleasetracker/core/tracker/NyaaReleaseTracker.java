package com.aminbhst.animereleasetracker.core.tracker;

import com.aminbhst.animereleasetracker.core.model.AnimeTitle;
import com.aminbhst.animereleasetracker.core.repository.AnimeTitleRepository;
import com.aminbhst.animereleasetracker.util.DateUtils;
import com.aminbhst.animereleasetracker.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class NyaaReleaseTracker extends AbstractAnimeReleaseTracker {

    public static final String URL_FORMAT_SEARCH = "https://nyaa.si/?f=0&c=0_0&q=%s";

    // Example :  ANIME_TITLE - 06 (1080p) [D1093E8B].mkv
    public static final String REGEX_TYPE_1 = "\\s-\\s\\d+\\s";

    public static final String REGEX_TYPE_2 = "\\sS\\d+E\\d+\\s";

    public NyaaReleaseTracker(@Autowired AnimeTitleRepository animeTitleRepository) {
        super(animeTitleRepository);
    }

    @Override
    public int getLatestEpisodeNumber(AnimeTitle animeTitle) {
        String url = String.format(URL_FORMAT_SEARCH, animeTitle.getTitle().replaceAll(" ", "+"));
        String htmlResponse = HttpUtils.getHtmlResponse(url);
        Document html = Jsoup.parse(htmlResponse);
        Elements tableResponsive = html.getElementsByClass("table-responsive");
        if (tableResponsive.isEmpty()) {
            log.info("Failed to find Anime {} on nyaa.si", animeTitle.getTitle());
            return 0;
        }
        Elements tableRows = tableResponsive.get(0)
                .getElementsByTag("table")
                .get(0)
                .getElementsByTag("tr");

        for (Element tableRow : tableRows) {
            Elements tds = tableRow.getElementsByTag("td");
            if (tds.isEmpty())
                continue;

            Element categoryElement = tds.get(0).getElementsByTag("a").get(0);
            String categoryHoverTitle = categoryElement.attr("title");
            if (categoryHoverTitle.contains("Non-English-translated"))
                continue;

            String title = tds.get(1)
                    .getElementsByTag("a")
                    .get(0)
                    .attr("title");

            if (matchesAnimeTitle(animeTitle, title)) {
                try {
                    int epNum = extractEpisodeNumber(title);
                    if (epNum > animeTitle.getNyaaLatestTrackedEpisode())
                        return epNum;
                } catch (Throwable ignored) {}
            }

//            else {
//                String titleStr = title;
//                if (title.startsWith("[")) {
//                    titleStr = title.substring(title.indexOf("] ") + 2);
//                }
//                int count = StringUtils.countMatches(animeTitle.getTitle(), " ");
//                int titleEndIndex = StringUtils.ordinalIndexOf(titleStr, " ", count + 1);
//                try {
//                    String extractedTitle = titleStr.substring(0, titleEndIndex);
//                    double similarity = StringUtilities.calculateSimilarity(extractedTitle, animeTitle.getTitle());
//                    extractedTitle.getBytes();
//                } catch (Throwable ignored) {
//
//                }
//            }

        }

        return 0;
    }

    @Override
    public int getLatestTrackedEpisode(AnimeTitle animeTitle) {
        return animeTitle.getNyaaLatestTrackedEpisode();
    }

    private int extractEpisodeNumber(String title) {
        int episodeNumber = 0;
        if (Pattern.compile(REGEX_TYPE_1).matcher(title).find()) {
            Pattern pattern = Pattern.compile("\\s-\\s\\d+");
            Matcher matcher = pattern.matcher(title);
            if (!matcher.find())
                return 0;

            String episodeStr = title.substring(matcher.start() + 3, matcher.end());
            episodeNumber = Integer.parseInt(episodeStr);
        } else {
            Matcher matcher = Pattern.compile(REGEX_TYPE_2).matcher(title);
            if (matcher.find()) {
                String episodeStr = title.substring(matcher.start() + 5, matcher.end() - 1);
                episodeNumber = Integer.parseInt(episodeStr);
            }
        }

        return episodeNumber;
    }


    private boolean matchesAnimeTitle(AnimeTitle animeTitle, String title) {
        return title.contains(animeTitle.getTitle()) ||
                (StringUtils.isNotBlank(animeTitle.getEnglishTitle()) && title.contains(animeTitle.getEnglishTitle()));
    }


    private boolean isReleasedToday(Elements tds) {
        for (Element td : tds) {
            String attr = td.attr("data-timestamp");
            if (StringUtils.isBlank(attr))
                continue;

            String dateStr = td.text();
            Calendar calTargetDate = DateUtils.calendarFromSimpleDateStr(dateStr);
            if (calTargetDate == null)
                continue;

            if (DateUtils.isToday(calTargetDate))
                return true;
        }
        return false;
    }
}
