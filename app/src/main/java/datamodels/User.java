package datamodels;

import java.util.Date;

import utils.DateUtil;

/**
 * Created by Shamyyoun on 6/24/2015.
 */
public class User {
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private int id;
    private String email;
    private String phone;
    private String password;
    private String firstName;
    private String LastName;
    private String photo;
    private Date birthDate;
    private String regId;

    public User(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getBirthDateString() {
        return DateUtil.convertToString(birthDate, DATE_FORMAT);
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = DateUtil.convertToDate(birthDate, DATE_FORMAT);
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }
}
