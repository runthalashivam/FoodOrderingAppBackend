package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AddressDao {
    @PersistenceContext
    private EntityManager entityManager;


    public AddressEntity createAddress(AddressEntity addressEntity) {

        entityManager.persist(addressEntity);
        return addressEntity;
    }

    public void mapCustomerToAddress(CustomerAddressEntity customerAddressEntity) {
        entityManager.persist(customerAddressEntity);
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

    //Method to get a list of customer address entities specific to a certain customer from the database
    public List<AddressEntity> getAllAddressesByCustomer(final CustomerEntity customer) {
        try {
            List<CustomerAddressEntity> customerAddressEntityList = entityManager.createNamedQuery("customerAddressByCustId", CustomerAddressEntity.class)
                    .setParameter("customer", customer).getResultList();
            List<AddressEntity> customerAddresses = new ArrayList<AddressEntity>();
            for(CustomerAddressEntity customerAddressEntity : customerAddressEntityList) {
                customerAddresses.add(customerAddressEntity.getAddress());
            }
            return customerAddresses;
        } catch (NoResultException nre) {
            return null;
        }
    }
}
