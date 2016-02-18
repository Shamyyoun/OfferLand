package json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import datamodels.Offer;

public class OffersHandler {
    private String response;

    public OffersHandler(String response) {
        this.response = response;
    }

    public List<Offer> handle() {
        try {
            JSONArray jsonArray = new JSONArray(response);
            List<Offer> offers = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Offer offer = handleOffer(jsonObject);

                offers.add(offer);
            }
            return offers;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Offer handleOffer(JSONObject jsonObject) {
        Offer offer;
        try {
            int id = jsonObject.getInt("id");
            String title = jsonObject.getString("title");
            String desc = jsonObject.getString("desc");
            int originalPrice = jsonObject.getInt("original_price");
            int newPrice = jsonObject.getInt("discount_price");
            String image = jsonObject.getString("image");
            String date = jsonObject.getString("created_at");

            String storeName = "Carrefour";
            String storePhoto = "";
            JSONObject storeObject = jsonObject.getJSONObject("store");
            if (storeObject != null) {
                storeName = storeObject.getString("firstname") + " " + storeObject.getString("lastname");
                storePhoto = storeObject.getString("photo");
            }

            offer = new Offer(id);
            offer.setTitle(title);
            offer.setDesc(desc);
            offer.setOriginalPrice(originalPrice);
            offer.setNewPrice(newPrice);
            offer.setImage(image);
            offer.setDate(date);
            offer.setStoreName(storeName);
            offer.setStorePhoto(storePhoto);

        } catch (JSONException e) {
            offer = null;
            e.printStackTrace();
        }

        return offer;
    }
}
