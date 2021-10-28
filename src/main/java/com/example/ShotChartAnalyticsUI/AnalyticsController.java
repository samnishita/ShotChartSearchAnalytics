package com.example.ShotChartAnalyticsUI;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * JavaFX controller for the UI
 */
@Component
public class AnalyticsController implements Initializable {
    @FXML
    ComboBox timecombobox;

    @FXML
    Label totalsearchescount, simplesearchescount, advancedsearchescount,
            totalshotsgathered, simpleshotsgathered, advancedshotsgathered,
            totalsecondspersearch, simplesecondspersearch, advancedsecondspersearch;

    @FXML
    TableView searchtable;

    @FXML
    TableColumn datecolumn, searchcolumn, shotcountcolumn, durationcolumn, sourcecolumn, cachehitcolumn;

    private String timeSelection = "7 Days";

    private int maxRecentSearches = 1000;

    private static HashMap<String, SearchOrganizer> mapTimeToSearchData;

    private NumberFormat formatter = new DecimalFormat("#0.0000");

    private LinkedList<Search> latestSearches;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchtable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        latestSearches = new LinkedList<>();
        datecolumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        setColumnTextWrap(datecolumn);
        searchcolumn.setCellValueFactory(new PropertyValueFactory<>("request"));
        setColumnTextWrap(searchcolumn);
        shotcountcolumn.setCellValueFactory(new PropertyValueFactory<>("shotCount"));
        durationcolumn.setCellValueFactory(new PropertyValueFactory<>("durationSeconds"));
        sourcecolumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        cachehitcolumn.setCellValueFactory(new PropertyValueFactory<>("isCached"));
        mapTimeToSearchData = new HashMap<>();
        mapTimeToSearchData.put("7 Days", new SearchOrganizer("7 Days", 24 * 7));
        mapTimeToSearchData.put("3 Days", new SearchOrganizer("3 Days", 24 * 3));
        mapTimeToSearchData.put("24 Hours", new SearchOrganizer("24 Hours", 24));
        mapTimeToSearchData.put("1 Hour", new SearchOrganizer("1 Hour", 1));
        ArrayList<String> timeOptions = new ArrayList<>(Arrays.asList("7 Days", "3 Days", "24 Hours", "1 Hour"));
        timecombobox.setItems(FXCollections.observableList(timeOptions));
        AnalyticsCalculator.getPastWeekSearches(mapTimeToSearchData);
        updateStatistics();
        timecombobox.setOnAction(event -> {
            timeSelection = timecombobox.getValue().toString();
            updateStatistics();
        });
    }

    public void updateStatistics() {
        AnalyticsCalculator.doAnalytics(mapTimeToSearchData);
        SearchOrganizer searchOrganizer = mapTimeToSearchData.get(timeSelection);
        this.totalsearchescount.setText(searchOrganizer.getTotalSearches() + "");
        this.simplesearchescount.setText(searchOrganizer.getSimpleSearches() + "");
        this.advancedsearchescount.setText(searchOrganizer.getAdvancedSearches() + "");
        this.totalshotsgathered.setText(searchOrganizer.getTotalShotCount() + "");
        this.simpleshotsgathered.setText(searchOrganizer.getSimpleShotCount() + "");
        this.advancedshotsgathered.setText(searchOrganizer.getAdvancedShotCount() + "");
        if (searchOrganizer.getTotalSeconds() == 0) {
            this.totalsecondspersearch.setText("0");
        } else {
            this.totalsecondspersearch.setText(formatter.format(searchOrganizer.getTotalSeconds() / searchOrganizer.getTotalSearches()) + "");
        }
        if (searchOrganizer.getSimpleSeconds() == 0) {
            this.simplesecondspersearch.setText("0");
        } else {
            this.simplesecondspersearch.setText(formatter.format(searchOrganizer.getSimpleSeconds() / searchOrganizer.getSimpleSearches()) + "");
        }
        if (searchOrganizer.getAdvancedSeconds() == 0) {
            this.advancedsecondspersearch.setText("0");
        } else {
            this.advancedsecondspersearch.setText(formatter.format(searchOrganizer.getAdvancedSeconds() / searchOrganizer.getAdvancedSearches()) + "");
        }
    }

    public void addRowToTable(Search search) {
        if (latestSearches.size() == maxRecentSearches) {
            latestSearches.removeLast();
        }
        latestSearches.addFirst(search);
        searchtable.setItems(FXCollections.observableList(latestSearches));
    }


    public static HashMap<String, SearchOrganizer> getMapTimeToSearchData() {
        return mapTimeToSearchData;
    }

    private void setColumnTextWrap(TableColumn col) {
        col.setCellFactory(tc -> {
            TableCell<Search, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(col.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
    }
}
