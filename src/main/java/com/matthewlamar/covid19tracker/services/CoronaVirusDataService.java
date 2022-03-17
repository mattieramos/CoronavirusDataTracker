package com.matthewlamar.covid19tracker.services;

import com.matthewlamar.covid19tracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

// this Service will give us the data and then when the app loads it will make a call to the URL to fetch the Data.
@Service
public class CoronaVirusDataService {

    //URL of the dataset we're using
    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *") //using scheduler to refresh dataset once every day
    //method that makes http call to the URL
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        //using HttpRequest since it will allow us to use the newBuilder() method
        HttpRequest request = HttpRequest.newBuilder()
                //creating uri from the VIRUS_DATA_URL string
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        //now to get response by sending client request
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        //Response is basically dong a client send.send of request and then what to do with the body. Taking the body and returning it as a string

        //System.out.println(httpResponse.body());
        //used java csv library (Commons CSV) and installed code to the maven folder to neatly parse through dataset

        StringReader csvBodyReader = new StringReader(httpResponse.body());
        // dataset has headers in the first line so used "Header auto detection" method from Commons CSV
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province/State"));
            locationStat.setCountry(record.get("Country/Region"));

            //using to specify number to get a coloumn
            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int prevDayCases = Integer.parseInt(record.get(record.size() - 1));
            locationStat.setLatestTotalCases(latestCases);
            locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
            newStats.add(locationStat);
        }
        this.allStats = newStats;

    }

}
