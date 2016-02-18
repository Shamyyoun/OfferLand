package json;

import org.json.JSONException;
import org.json.JSONObject;

import datamodels.User;

public class UserHandler {
    private String response;

    public UserHandler(String response) {
        this.response = response;
    }

    public User handle() {
        try {
            JSONObject jsonObject = new JSONObject(response);
            User user = handleUser(jsonObject);
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private User handleUser(JSONObject jsonObject) {
        User user;
        try {
            int id = jsonObject.getInt("id");
            String email = jsonObject.getString("email");
            String phone = jsonObject.getString("phone");
            String firstName = jsonObject.getString("firstname");
            String lastName = jsonObject.getString("lastname");
            String photo = jsonObject.getString("photo");
            String birthDate = jsonObject.getString("birthdate");

            user = new User(id);
            user.setEmail(email);
            user.setPhone(phone);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoto(photo);
            user.setBirthDate(birthDate);
        } catch (JSONException e) {
            user = null;
            e.printStackTrace();
        }

        return user;
    }
}
