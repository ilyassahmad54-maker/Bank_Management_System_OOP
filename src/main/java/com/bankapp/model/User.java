package com.bankapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private int userId;
    private String username;
    private String password; // BCrypt hashed
    private int age;
    private String cnic;
    private String address;
    private String phone;
}
