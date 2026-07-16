package com.bankapp.model;

public class User {
    private int userId;
    private String username;
    private String password;
    private int age;
    private String cnic;
    private String address;
    private String phone;

    public User() {}
    public User(int userId, String username, String password, int age, String cnic, String address, String phone) {
        this.userId = userId; this.username = username; this.password = password;
        this.age = age; this.cnic = cnic; this.address = address; this.phone = phone;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getCnic() { return cnic; }
    public void setCnic(String cnic) { this.cnic = cnic; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private int userId; private String username; private String password;
        private int age; private String cnic; private String address; private String phone;
        public Builder userId(int v) { userId = v; return this; }
        public Builder username(String v) { username = v; return this; }
        public Builder password(String v) { password = v; return this; }
        public Builder age(int v) { age = v; return this; }
        public Builder cnic(String v) { cnic = v; return this; }
        public Builder address(String v) { address = v; return this; }
        public Builder phone(String v) { phone = v; return this; }
        public User build() { return new User(userId, username, password, age, cnic, address, phone); }
    }
}
