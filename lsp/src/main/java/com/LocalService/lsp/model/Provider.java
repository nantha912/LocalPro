package com.LocalService.lsp.model;

import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Data
@Document(collection = "providers")
public class Provider {
    @Id
    private String id;
    private String name;
    private int age;
    private String gender;
    private String profession;
    private String experience;
    private String services;
    private String pricing;
    private String address;
    private String city;
    private String pincode;
    private String locationLink;

}

