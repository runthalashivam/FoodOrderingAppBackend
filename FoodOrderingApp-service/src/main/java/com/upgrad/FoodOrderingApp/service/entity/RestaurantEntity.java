package com.upgrad.FoodOrderingApp.service.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "restaurant")
@NamedQueries(
        {
                @NamedQuery(name = "restaurantByUuid", query = "select r from RestaurantEntity r where r.uuid = :restaurantUuid"),
                @NamedQuery(
                name = "restaurantsByRating",
                query = "select r from RestaurantEntity r order  by customerRating desc"),
                @NamedQuery(
                        name = "getRestaurantByName",
                        query =
                                "select r from RestaurantEntity r where lower(restaurantName) like lower(:searchString) "
                                        + "order by r.restaurantName asc"),
                @NamedQuery(
                        name = "restaurantByCategory",
                        query =
                                "Select r from RestaurantEntity r where id in (select rc.restaurantId from RestaurantCategoryEntity rc where rc.categoryId = "
                                        + "(select c.id from CategoryEntity c where "
                                        + "c.uuid=:categoryUuid) ) order by restaurant_name")
        }
)

public class RestaurantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Column(name = "uuid")
    @Size(max = 200)
    private String uuid;

    @NotNull
    @Column(name = "restaurant_name")
    @Size(max = 50)
    private String restaurantName;

    @NotNull
    @Column(name = "photo_url")
    @Size(max = 255)
    private String photoUrl;

    @Column(name = "customer_rating")
    private Double customerRating;

    @NotNull
    @Column(name = "average_price_for_two")
    private Integer averagePriceForTwo;

    @NotNull
    @Column(name = "number_of_customers_rated")
    private Integer numberOfCustomersRated;

    @NotNull
    @OneToOne
    @JoinColumn(name = "address_id")
    private AddressEntity address;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Double getCustomerRating() {
        return customerRating;
    }

    public void setCustomerRating(Double customerRating) {
        this.customerRating = customerRating;
    }

    public Integer getAveragePriceForTwo() {
        return averagePriceForTwo;
    }

    public void setAveragePriceForTwo(Integer averagePriceForTwo) {
        this.averagePriceForTwo = averagePriceForTwo;
    }

    public Integer getNumberOfCustomersRated() {
        return numberOfCustomersRated;
    }

    public void setNumberOfCustomersRated(Integer numberOfCustomersRated) {
        this.numberOfCustomersRated = numberOfCustomersRated;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object obj) {
        return new EqualsBuilder().append(this, obj).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this).hashCode();
    }
}
