package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class OrderDao {

    @PersistenceContext
    private EntityManager entityManager;

    public OrderEntity createOrder(OrderEntity orderEntity) {
        entityManager.persist(orderEntity);
        return orderEntity;
    }

    public void mapOrderToItem(OrderItemEntity orderItemEntity) {
        entityManager.persist(orderItemEntity);
    }

    public CouponEntity getCouponByUuid(final String couponUuid) {
        try {
            return entityManager.createNamedQuery("couponByUuid", CouponEntity.class)
                    .setParameter("couponUuid", couponUuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
