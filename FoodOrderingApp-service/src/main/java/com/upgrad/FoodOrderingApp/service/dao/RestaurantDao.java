package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class RestaurantDao {

    @PersistenceContext
    private EntityManager entityManager;

    public RestaurantEntity getRestaurantByUuid(final String restaurantUuid) {
        try {
            return entityManager.createNamedQuery("restaurantByUuid", RestaurantEntity.class)
                    .setParameter("restaurantUuid", restaurantUuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<RestaurantEntity> restaurantsByRating() {
        return entityManager
                .createNamedQuery("restaurantsByRating", RestaurantEntity.class)
                .getResultList();
    }

    public List<RestaurantEntity> restaurantsByName(final String searchString) {
        return entityManager
                .createNamedQuery("getRestaurantByName", RestaurantEntity.class)
                .setParameter("searchString", "%" + searchString + "%")
                .getResultList();
    }

    public RestaurantEntity updateRestaurantEntity(final RestaurantEntity restaurantEntity) {
        RestaurantEntity updatedRestaurantEntity = entityManager.merge(restaurantEntity);
        return updatedRestaurantEntity;
    }

    public List<RestaurantEntity> restaurantByCategory(final String categoryUuid) {

        return entityManager
                .createNamedQuery("restaurantByCategory", RestaurantEntity.class)
                .setParameter("categoryUuid", categoryUuid)
                .getResultList();
    }
}
