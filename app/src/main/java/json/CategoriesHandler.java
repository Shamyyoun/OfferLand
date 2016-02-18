package json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import datamodels.Category;

public class CategoriesHandler {
    private String response;

    public CategoriesHandler(String response) {
        this.response = response;
    }

    public List<Category> handle() {
        try {
            JSONArray jsonArray = new JSONArray(response);
            List<Category> categories = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Category category = handleCategory(jsonObject);

                categories.add(category);
            }
            return categories;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Category handleCategory(JSONObject jsonObject) {
        Category category;
        try {
            int id = jsonObject.getInt("id");
            String title = jsonObject.getString("name");

            category = new Category(id, title);
        } catch (JSONException e) {
            category = null;
            e.printStackTrace();
        }

        return category;
    }
}
