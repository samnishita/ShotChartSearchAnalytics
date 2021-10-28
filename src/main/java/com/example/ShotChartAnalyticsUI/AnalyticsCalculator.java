package com.example.ShotChartAnalyticsUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class AnalyticsCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsCalculator.class);
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");

    public static void getPastWeekSearches(HashMap<String, SearchOrganizer> stringSearchOrganizerHashMap) {
        ResourceBundle reader = ResourceBundle.getBundle("application");
        try (Connection conn = DriverManager.getConnection(reader.getString("spring.searchdatabase.jdbc-url"), reader.getString("spring.searchdatabase.username"), reader.getString("spring.searchdatabase.password"))) {
            for (int i = 6; i >= 0; i--) {
                try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + dateFormat.format(new Date(System.currentTimeMillis() - 3600000 * 24 * i)) + "_searches ORDER BY calendar");
                     ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        Search search = new Search(
                                resultSet.getString(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getString(4),
                                resultSet.getInt(5),
                                resultSet.getDouble(6),
                                resultSet.getInt(7) == 1);
                        processSearch(search);
                    }
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public static void doAnalytics(HashMap<String, SearchOrganizer> stringSearchOrganizerHashMap) {
        stringSearchOrganizerHashMap.keySet().forEach(eachKey -> stringSearchOrganizerHashMap.get(eachKey).checkForOutdatedSearches());
    }

    public static Search processNewSearchFromKafka(String message) {
        String[] temp;
        String searchType;
        if (message.contains("Simple Search")) {
            temp = message.split("Simple Search");
            searchType = "Simple";
        } else {
            temp = message.split("Advanced Search");
            searchType = "Advanced";
        }
        String dateTime = temp[0].trim();
        String source;
        if (temp[1].contains("https")) {
            source = "https://" + temp[1].replace("from https://", "").split(":")[0].trim();
        } else {
            source = temp[1].split(":")[0].replace("from", "").trim();
        }
        temp = temp[1].split(source + ":")[1].split("\\(Shots:");
        String request = temp[0].trim();
        int shotCount = Integer.parseInt(temp[1].split("\\)")[0].trim());
        temp = temp[1].split("\\)");
        double durationSeconds = Double.parseDouble(temp[1].split("\\[Elapsed Time:")[1].split("seconds")[0]);
        boolean isCached = message.contains("(Cached)");
        Search search = new Search(dateTime, source, searchType, request, shotCount, durationSeconds, isCached);
        processSearch(search);
        return search;
    }

    private static void processSearch(Search newSearch) {
        long weekAgoMillis = System.currentTimeMillis() - 3600000 * 24 * 7;
        long threeDaysAgoMillis = System.currentTimeMillis() - 3600000 * 24 * 3;
        long oneDayAgoMillis = System.currentTimeMillis() - 3600000 * 24;
        long oneHourAgoMillis = System.currentTimeMillis() - 3600000;
        long searchMillis = newSearch.getMillisFromSearchTime();
        HashMap<String, SearchOrganizer> stringSearchOrganizerHashMap = AnalyticsController.getMapTimeToSearchData();
        if (searchMillis > weekAgoMillis) {
            SearchOrganizer oneWeekSearchOrganizer = stringSearchOrganizerHashMap.get("7 Days");
            oneWeekSearchOrganizer.addToSearchList(newSearch);
            if (searchMillis > threeDaysAgoMillis) {
                SearchOrganizer threeDaysSearchOrganizer = stringSearchOrganizerHashMap.get("3 Days");
                threeDaysSearchOrganizer.addToSearchList(newSearch);
                if (searchMillis >= oneDayAgoMillis) {
                    SearchOrganizer oneDaySearchOrganizer = stringSearchOrganizerHashMap.get("24 Hours");
                    oneDaySearchOrganizer.addToSearchList(newSearch);
                    if (searchMillis >= oneHourAgoMillis) {
                        SearchOrganizer oneHourSearchOrganizer = stringSearchOrganizerHashMap.get("1 Hour");
                        oneHourSearchOrganizer.addToSearchList(newSearch);
                    }
                }
            }
        }
    }

}
