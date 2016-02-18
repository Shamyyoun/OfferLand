package datamodels;

import java.io.Serializable;
import java.util.Date;

import utils.DateUtil;

/**
 * Created by Shamyyoun on 6/26/2015.
 */
public class Offer implements Serializable {
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    private int id;
    private String title;
    private String desc;
    private int originalPrice;
    private int newPrice;
    private String image;
    private Date date;
    private String storeName;
    private String storePhoto;
    private double lat;
    private double lng;

    public Offer(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(int originalPrice) {
        this.originalPrice = originalPrice;
    }

    public int getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(int newPrice) {
        this.newPrice = newPrice;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getDate() {
        return date;
    }

    public String getDateAsString() {
        return DateUtil.convertToString(date, DATE_FORMAT);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String date) {
        this.date = DateUtil.convertToDate(date, DATE_FORMAT);
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStorePhoto() {
        return storePhoto;
    }

    public void setStorePhoto(String storePhoto) {
        this.storePhoto = storePhoto;
    }

    public float getDiscount() {
        float discount = (((float) (originalPrice - newPrice)) / originalPrice) * 100;
        return discount;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getFormattedDate() {
        return DateUtil.convertToString(date, "dd/MM/yyyy");
    }
}
