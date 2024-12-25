package com.example;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;



import java.io.IOException;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static Gson GSON = new Gson();

    private static final String ORS_API_KEY = "5b3ce3597851110001cf6248d088d764b8ec4d7cb9f98ecb5889292e";
    private static final String ORS_URL = "https://api.openrouteservice.org/v2/directions/driving-car";
    private static final String OSM_SEARCH_URL = "https://nominatim.openstreetmap.org/search";
    private static final String OSM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse";

    // openrouteservice token

    public static void main(String[] args) throws URISyntaxException, IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите начальный адрес: ");
        final String addressA = scanner.nextLine();
        final JsonObject locationAInfo = doGetRequest(
                OSM_SEARCH_URL,
                Map.of("q", addressA, "format", "json")
        ).getAsJsonArray().get(0).getAsJsonObject();

        String coordinatesA = locationAInfo.getAsJsonPrimitive("lon").getAsString()
                + ","
                + locationAInfo.getAsJsonPrimitive("lat").getAsString();

        System.out.print("Введите конечный адрес: ");
        final String addressB = scanner.nextLine();
        final JsonObject locationsBInfo = doGetRequest(
                OSM_SEARCH_URL,
                Map.of("q", addressB, "format", "json")
        ).getAsJsonArray().get(0).getAsJsonObject();

        String coordinatesB = locationsBInfo.getAsJsonPrimitive("lon").getAsString()
                + ","
                + locationsBInfo.getAsJsonPrimitive("lat").getAsString();

        final JsonObject routSegment = doGetRequest(
                ORS_URL,
                Map.of(
                        "api_key", ORS_API_KEY,
                        "start", coordinatesA,
                        "end", coordinatesB
                )
        ).getAsJsonObject()
                .getAsJsonArray("features")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("properties")
                .getAsJsonArray("segments")
                .get(0)
                .getAsJsonObject();

        System.out.println("Расстояние: "   +  routSegment.getAsJsonPrimitive("distance") + " m.");
        System.out.println("Продолжительность маршрута: " + routSegment.getAsJsonPrimitive("duration") + " sec.");

        for (JsonElement step : routSegment.getAsJsonArray("steps")) {
            System.out.println("-----------------------------------");
            System.out.println("Дистанция: "   + step.getAsJsonObject().getAsJsonPrimitive("distance") + " m.");
            System.out.println("Продолжительность: " + step.getAsJsonObject().getAsJsonPrimitive("duration") + " sec.");
            System.out.println("Инструкция: " + step.getAsJsonObject().getAsJsonPrimitive("instruction"));
        }
    }

    static JsonElement doGetRequest(final String url,
                                    final Map<String, String> queryParams)
            throws URISyntaxException, IOException {
        String queryString = buildQueryParams(queryParams);
        final HttpURLConnection connection = (HttpURLConnection) new URI(url + queryString)
                .toURL()
                .openConnection();
        connection.setRequestMethod("GET");
        System.out.println(connection.getResponseCode());
        System.out.println(connection.getResponseMessage());


        final Scanner scanner = new Scanner(connection.getInputStream());


        String response = "";
        while (scanner.hasNext()) {
            response += scanner.nextLine();
        }
        return GSON.fromJson(response.toString(), JsonElement.class);
    }

    static String buildQueryParams(final Map<String, String> queryParams) {
        if (queryParams.isEmpty()) {
            return "";
        }


        //[key1=value1, key2=value2]
        List<String> formattedParams = new ArrayList<>();
        for (final Map.Entry<String, String> param : queryParams.entrySet()) {
            formattedParams.add(param.getKey() + "=" + URLEncoder.encode(param.getValue()));
        }

        //?key1=value1&key2=value2...
        StringBuilder stringBuilder = new StringBuilder("?");
        for (int i = 0; i < formattedParams.size(); i++) {
            final String param = formattedParams.get(i);
            stringBuilder.append(i != 0 ? "&" : "").append(param);
        }
        return stringBuilder.toString();
    }
}