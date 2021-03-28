package com.upgrad.FoodOrderingApp.service.dao;


import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class PaymentDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<PaymentEntity> getPaymentMethods(){
        try {
            return this.entityManager.createNamedQuery("allPaymentMethods", PaymentEntity.class).getResultList();
    public PaymentEntity getPaymentByUuid(final String paymentUuid) {
        try {
            return entityManager.createNamedQuery("paymentByUuid", PaymentEntity.class)
                    .setParameter("paymentUuid", paymentUuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
