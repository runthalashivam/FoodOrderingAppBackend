package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AddressBusinessService {

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private CustomerDao customerDao;

    @Transactional
    public AddressEntity saveAddress(AddressEntity addressEntity, final String authorizationToken) throws AddressNotFoundException, SaveAddressException, AuthorizationFailedException {

        //Validate customer
        CustomerAuthEntity customerAuthTokenEntity = customerDao.getCustomerAuthToken(authorizationToken);
        if (customerAuthTokenEntity==null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        } else if (customerAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        } else if (customerAuthTokenEntity.getExpiresAt().compareTo(ZonedDateTime.now()) < 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        //Check requirements for request
        if(addressEntity.getState()==null) {
            throw new AddressNotFoundException("ANF-002","No state by this id");
        } else if (addressEntity.getFlatBuilNumber()==null || addressEntity.getLocality()==null || addressEntity.getCity()==null || addressEntity.getPincode()==null || addressEntity.getState()==null) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        } else if (!addressEntity.getPincode().matches("[0-9]+") ||
                    addressEntity.getPincode().length() != 6){
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }

        //Once customer and request are validated, save address in database
        AddressEntity savedAddressEntity = addressDao.createAddress(addressEntity);

        //Map the saved address entity to the corresponding customer in customer_address table of database
        CustomerEntity customer = customerAuthTokenEntity.getCustomer();
        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setCustomer(customer);
        customerAddressEntity.setAddress(savedAddressEntity);
        addressDao.mapCustomerToAddress(customerAddressEntity);

        return savedAddressEntity;
    }


    @Transactional
    public List<AddressEntity> getAllAddressesByCustomer(final String authorizationToken) throws AuthorizationFailedException {

        //Validate customer
        CustomerAuthEntity customerAuthTokenEntity = customerDao.getCustomerAuthToken(authorizationToken);
        if (customerAuthTokenEntity==null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        } else if (customerAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        } else if (customerAuthTokenEntity.getExpiresAt().compareTo(ZonedDateTime.now()) < 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        CustomerEntity customer = customerAuthTokenEntity.getCustomer();
        List<AddressEntity> customerAddresses = addressDao.getAllAddressesByCustomer(customer);
        Collections.reverse(customerAddresses);
        return customerAddresses;
    }

    @Transactional
    public boolean deleteAddress(final String addressId, final String authorizationToken) throws AuthorizationFailedException, AddressNotFoundException {

        //Validate customer
        CustomerAuthEntity customerAuthTokenEntity = customerDao.getCustomerAuthToken(authorizationToken);
        if (customerAuthTokenEntity==null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        } else if (customerAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        } else if (customerAuthTokenEntity.getExpiresAt().compareTo(ZonedDateTime.now()) < 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        //Check if address id field is empty
        if(addressId == null) {
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }

        //Check if address to be deleted exists in the database
        AddressEntity addressToBeDeleted = addressDao.getAddressByUuid(addressId);
        if(addressToBeDeleted == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        //Check if customer is authorized to delete the address
        CustomerEntity addressOwner = addressDao.getCustomerByAddress(addressToBeDeleted);
        CustomerEntity loggedInCustomer = customerAuthTokenEntity.getCustomer();
        if(addressOwner.getId() != loggedInCustomer.getId()) {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }

        return addressDao.deleteAddress(addressToBeDeleted);
    }

    //Method to get list of all states from the database
    @Transactional
    public List<StateEntity> getAllStates() {
        return addressDao.getAllStates();
    }


}
