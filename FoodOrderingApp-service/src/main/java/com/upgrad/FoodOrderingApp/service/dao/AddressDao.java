package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class AddressDao {
    @PersistenceContext
    private EntityManager entityManager;


    public AddressEntity createAddress(AddressEntity addressEntity) {
        System.out.println(addressEntity.getFlatBuilNumber());
        System.out.println(addressEntity.getLocality());
        System.out.println(addressEntity.getCity());
        System.out.println(addressEntity.getPincode());
        System.out.println(addressEntity.getState().getId());

        entityManager.persist(addressEntity);
        return addressEntity;
    }

    public StateEntity getStateByUuid(final String stateUuid) {
        try {
            return entityManager.createNamedQuery("stateByUuid", StateEntity.class)
                    .setParameter("uuid", stateUuid)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
