package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

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
}
