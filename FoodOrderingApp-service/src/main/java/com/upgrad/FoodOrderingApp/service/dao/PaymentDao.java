package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

@Repository
public class PaymentDao {

    @PersistenceContext
    private EntityManager entityManager;

    public PaymentEntity getPaymentByUuid(final String paymentUuid) {
        try {
            return entityManager.createNamedQuery("paymentByUuid", PaymentEntity.class)
                    .setParameter("paymentUuid", paymentUuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<PaymentEntity> getAllPaymentMethods() {
        List<PaymentEntity> paymentMethods =
                entityManager.createNamedQuery("getAllPaymentMethods", PaymentEntity.class).getResultList();
        if (paymentMethods != null) {
            return paymentMethods;
        }
        return Collections.emptyList();
    }
}
