package com.example.tk3_safetyapp.backend.supermarket;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * This class can be used to work with locations of supermarkets
 */
public class SupermarketHelper {
    private RequestQueue queue;

    private LinkedList<Supermarket> supermarkets = new LinkedList<>();

    public SupermarketHelper(Activity parent) {
        queue = Volley.newRequestQueue(parent.getBaseContext());
        loadSupermarketLocations("Darmstadt");
    }

    /**
     * Download all supermarket locations in a certain city
     *
     * @param city: The city in which to search for supermarkets
     */
    private void loadSupermarketLocations(String city) {
        // Request the supermarkets that are available as nodes
        StringBuilder requestUrl = new StringBuilder("https://overpass-api.de/api/interpreter?data=");
        requestUrl.append("node%5B%22addr%3Acity%22%3D%22");
        requestUrl.append(city);
        requestUrl.append("%22%5D%5B%22shop%22%3D%22supermarket%22%5D%3Bout%3B");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseSupermarketNodes(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("api req", error.toString());
            }
        });

        queue.add(stringRequest);
    }

    private void parseSupermarketNodes(String xmlResponse) {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = new ByteArrayInputStream(xmlResponse.getBytes());
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            int eventType = parser.getEventType();
            double lat = 0.0;
            double lon = 0.0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String eltName;


                if (eventType == XmlPullParser.START_TAG) {
                        eltName = parser.getName();

                        if ("node".equals(eltName)) {
                            lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                            lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                        } else if ("tag".equals(eltName)) {
                            if (parser.getAttributeValue(null, "k").equals("name")) {
                                supermarkets.addLast(new SupermarketNode(parser.getAttributeValue(null, "v"), lon, lat));
                            }
                        }
                        break;
                }

                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the user location is near a supermarket
     *
     * @param longitudeUser: longitude of the users current location
     * @param latitudeUser:  latitude of the users current location
     */
    public boolean nearSupermarket(double longitudeUser, double latitudeUser) {
        double distance;
        for (Supermarket s : supermarkets) {
            distance = s.distance(longitudeUser, latitudeUser);
            // TODO find a good value, when is location considered near supermarket
            if(distance < 100){
                return true;
            }
        }

        return false;
    }
}
