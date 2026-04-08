package com.example.beautyparlourapp.models;

public class Offer {
    private String id;
    private String title;
    private String promoCode;
    private String discount;
    private String terms;
    private boolean active;

    public Offer() {}

    public Offer(String id, String title, String promoCode, String discount, String terms, boolean active) {
        this.id = id;
        this.title = title;
        this.promoCode = promoCode;
        this.discount = discount;
        this.terms = terms;
        this.active = active;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public String getDiscount() { return discount; }
    public void setDiscount(String discount) { this.discount = discount; }
    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
