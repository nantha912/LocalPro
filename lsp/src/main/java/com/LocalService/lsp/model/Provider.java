package com.LocalService.lsp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "providers")
public class Provider {
    @Id
    private String id;

    private String customerId;
    private String name;
    private String serviceCategory; // Matches DB key
    private String description;
    private Double price;
    private String workType;
    private String email;
    private String phoneNumber;
    private String whatsappNumber;
    private String instagramLink;
    private String facebookLink;
    private String youtubeLink;
    private String twitterLink;
    private String websiteLink;
    private String location; // Matches DB key
    private Double latitude;
    private Double longitude;
    private String upiId;
    private List<String> portfolioPhotos = new ArrayList<>();

    public Provider(String id, String customerId, String name, String serviceCategory, String description, Double price, String workType, String email, String phoneNumber, String whatsappNumber, String instagramLink, String facebookLink, String youtubeLink, String twitterLink, String websiteLink, String location, Double latitude, Double longitude, String upiId, List<String> portfolioPhotos) {
        this.id = id;
        this.customerId = customerId;
        this.name = name;
        this.serviceCategory = serviceCategory;
        this.description = description;
        this.price = price;
        this.workType = workType;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.whatsappNumber = whatsappNumber;
        this.instagramLink = instagramLink;
        this.facebookLink = facebookLink;
        this.youtubeLink = youtubeLink;
        this.twitterLink = twitterLink;
        this.websiteLink = websiteLink;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.upiId = upiId;
        this.portfolioPhotos = portfolioPhotos;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServiceCategory() { return serviceCategory; }
    public void setServiceCategory(String serviceCategory) { this.serviceCategory = serviceCategory; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }
    public String getInstagramLink() { return instagramLink; }
    public void setInstagramLink(String instagramLink) { this.instagramLink = instagramLink; }
    public String getFacebookLink() { return facebookLink; }
    public void setFacebookLink(String facebookLink) { this.facebookLink = facebookLink; }
    public String getYoutubeLink() { return youtubeLink; }
    public void setYoutubeLink(String youtubeLink) { this.youtubeLink = youtubeLink; }
    public String getTwitterLink() { return twitterLink; }
    public void setTwitterLink(String twitterLink) { this.twitterLink = twitterLink; }
    public String getWebsiteLink() { return websiteLink; }
    public void setWebsiteLink(String websiteLink) { this.websiteLink = websiteLink; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }
    public List<String> getPortfolioPhotos() { return portfolioPhotos; }
    public void setPortfolioPhotos(List<String> portfolioPhotos) { this.portfolioPhotos = portfolioPhotos; }
}