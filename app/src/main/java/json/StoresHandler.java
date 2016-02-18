package json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import datamodels.Store;

public class StoresHandler {
    private String response;

    public StoresHandler(String response) {
        this.response = response;
    }

    public List<Store> handle() {
        try {
            JSONArray jsonArray = new JSONArray(response);
            List<Store> stores = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Store store = handleStore(jsonObject);

                stores.add(store);
            }
            return stores;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Store handleStore(JSONObject jsonObject) {
        Store store;
        try {
            int id = jsonObject.getInt("id");
            String firstName = jsonObject.getString("firstname");
            String lastName = jsonObject.getString("lastname");
            double lat = jsonObject.getDouble("lat");
            double lng = jsonObject.getDouble("long");

            store = new Store(id);
            store.setFirstName(firstName);
            store.setLastName(lastName);
            store.setLat(lat);
            store.setLng(lng);

        } catch (JSONException e) {
            store = null;
            e.printStackTrace();
        }

        return store;
    }
}
