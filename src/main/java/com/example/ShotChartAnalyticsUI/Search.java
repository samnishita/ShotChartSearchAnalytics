package com.example.ShotChartAnalyticsUI;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Search {
    private String dateTime, source, searchType, request;
    private int shotCount;
    private double durationSeconds;
    private boolean isCached;

    public Search(String dateTime, String source, String searchType, String request, int shotCount, double durationSeconds, boolean isCached) {
        this.dateTime = dateTime;
        this.source = source;
        this.searchType = searchType;
        this.request = request;
        this.shotCount = shotCount;
        this.durationSeconds = durationSeconds;
        this.isCached = isCached;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getSource() {
        return source;
    }

    public String getSearchType() {
        return searchType;
    }

    public String getRequest() {
        return request;
    }

    public int getShotCount() {
        return shotCount;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    public boolean getIsCached() {
        return isCached;
    }

    public long getMillisFromSearchTime() {
        return LocalDateTime.parse(this.dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .atZone(ZoneId.of("America/Los_Angeles")).toInstant().toEpochMilli();
    }
}

