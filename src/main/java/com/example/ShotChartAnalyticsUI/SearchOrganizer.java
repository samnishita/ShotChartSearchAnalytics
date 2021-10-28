package com.example.ShotChartAnalyticsUI;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class SearchOrganizer {
    private String timeRange;
    private PriorityQueue<Search> searches;
    private int totalSearches, simpleSearches, advancedSearches,
            totalShotCount, simpleShotCount, advancedShotCount,
            hoursMax;
    private HashMap<String, Integer> mapSearchSourceToSearchCount;
    private double totalSeconds, simpleSeconds, advancedSeconds;

    public SearchOrganizer(String timeRange, int hoursMax) {
        this.timeRange = timeRange;
        this.hoursMax = hoursMax;
        this.searches = new PriorityQueue<>(Comparator.comparingLong(Search::getMillisFromSearchTime));
        this.totalSearches = 0;
        this.simpleSearches = 0;
        this.advancedSearches = 0;
        this.totalShotCount = 0;
        this.simpleShotCount = 0;
        this.advancedShotCount = 0;
        this.totalSeconds = 0.0;
        this.simpleSeconds = 0.0;
        this.advancedSeconds = 0.0;
        this.mapSearchSourceToSearchCount = new HashMap<>();
        this.mapSearchSourceToSearchCount.put("https://customnbashotcharts.com/", 0);
        this.mapSearchSourceToSearchCount.put("https://samnishita.github.io/", 0);
        this.mapSearchSourceToSearchCount.put("desktop", 0);
    }

    public void addToSearchList(Search search) {
        searches.add(search);
        if (search.getSearchType().equals("Simple")) {
            simpleSearches++;
            simpleSeconds += search.getDurationSeconds();
            simpleShotCount += search.getShotCount();
        } else {
            advancedSearches++;
            advancedSeconds += search.getDurationSeconds();
            advancedShotCount += search.getShotCount();
        }
        totalSearches++;
        totalSeconds += search.getDurationSeconds();
        totalShotCount += search.getShotCount();
        if (search.getSource() == null) {
            this.mapSearchSourceToSearchCount.put("desktop", this.mapSearchSourceToSearchCount.get("desktop") + 1);
        } else if (this.mapSearchSourceToSearchCount.containsKey(search.getSearchType())) {
            this.mapSearchSourceToSearchCount.put(search.getSearchType(), this.mapSearchSourceToSearchCount.get(search.getSearchType()) + 1);
        }
    }

    public void checkForOutdatedSearches() {
        long millisMin = System.currentTimeMillis() - 3600000 * hoursMax;
        while (!searches.isEmpty()) {
            Search peekedSearch = searches.peek();
            if (peekedSearch.getMillisFromSearchTime() < millisMin) {
                removeFromSearchList(searches.poll());
            } else {
                break;
            }
        }
    }

    public void removeFromSearchList(Search search) {
        searches.remove(search);
        if (search.getSearchType().equals("Simple")) {
            simpleSearches--;
            simpleSeconds -= search.getDurationSeconds();
            simpleShotCount -= search.getShotCount();

        } else {
            advancedSearches--;
            advancedSeconds -= search.getDurationSeconds();
            advancedShotCount -= search.getShotCount();
        }
        totalSearches--;
        totalSeconds -= search.getDurationSeconds();
        totalShotCount -= search.getShotCount();
        if (search.getSource() == null) {
            this.mapSearchSourceToSearchCount.put("desktop", this.mapSearchSourceToSearchCount.get("desktop") - 1);
        } else if (this.mapSearchSourceToSearchCount.containsKey(search.getSearchType())) {
            this.mapSearchSourceToSearchCount.put(search.getSearchType(), this.mapSearchSourceToSearchCount.get(search.getSearchType()) - 1);
        }
    }

    public int getTotalSearches() {
        return totalSearches;
    }

    public int getSimpleSearches() {
        return simpleSearches;
    }

    public int getAdvancedSearches() {
        return advancedSearches;
    }

    public double getTotalSeconds() {
        return totalSeconds;
    }

    public double getSimpleSeconds() {
        return simpleSeconds;
    }

    public double getAdvancedSeconds() {
        return advancedSeconds;
    }

    public int getTotalShotCount() {
        return totalShotCount;
    }

    public int getSimpleShotCount() {
        return simpleShotCount;
    }

    public int getAdvancedShotCount() {
        return advancedShotCount;
    }
}
