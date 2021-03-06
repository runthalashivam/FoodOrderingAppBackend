package com.upgrad.FoodOrderingApp.service.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "item")
@NamedQueries(
        {
                @NamedQuery(name = "ItemByUuid", query = "select i from ItemEntity i where i.uuid = :ItemUuid"),
                @NamedQuery(
                        name = "getAllItemsInCategoryInRestaurant",
                        query =
                                "select i from ItemEntity i  where id in (select ri.itemId from RestaurantItemEntity ri "
                                        + "inner join CategoryItemEntity ci on ri.itemId = ci.itemId "
                                        + "where ri.restaurantId = (select r.id from RestaurantEntity r where "
                                        + "r.uuid=:restaurantUuid) and ci.categoryId = "
                                        + "(select c.id from CategoryEntity c where c.uuid=:categoryUuid ) )"
                                        + "order by lower(i.itemName) asc")
        }
)
@NamedNativeQueries({
        // Using native query as named queries do not support LIMIT in nested statements.
        @NamedNativeQuery(
                name = "topFivePopularItemsByRestaurant",
                query =
                        "select * from item where id in "
                                + "(select item_id from order_item where order_id in "
                                + "(select id from orders where restaurant_id = ? ) "
                                + "group by order_item.item_id "
                                + "order by (count(order_item.order_id)) "
                                + "desc LIMIT 5)",
                resultClass = ItemEntity.class)
})
public class ItemEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "uuid")
    @NotNull
    @Size(max = 200)
    private String uuid;

    @Column(name = "item_name")
    @NotNull
    @Size(max = 30)
    private String itemName;

    @Column(name = "price")
    @NotNull
    private Integer price;

    @Column(name = "type")
    @NotNull
    @Size(max = 10)
    private String type;

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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String itemType) {
        this.type = itemType;
    }

    @Override
    public boolean equals(Object obj) {
        return new EqualsBuilder().append(this, obj).isEquals();
    }

    /*@Override
    public int hashCode() {
        return new HashCodeBuilder().append(this).hashCode();
    }*/
}
