package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthTokenEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.time.ZonedDateTime;

@Service
public class AddressBusinessService {

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private CustomerDao customerDao;

    @Transactional
    public AddressEntity saveAddress(AddressEntity addressEntity, final String authorizationToken) throws AddressNotFoundException, SaveAddressException, AuthorizationFailedException {
        if(addressEntity.getState()==null) {
            throw new AddressNotFoundException("ANF-002","No state by this id");
        } else if (addressEntity.getFlatBuilNumber()==null || addressEntity.getLocality()==null || addressEntity.getCity()==null || addressEntity.getPincode()==null || addressEntity.getState()==null) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        } else if (!addressEntity.getPincode().matches("[0-9]+") ||
                    addressEntity.getPincode().length() != 6){
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }

        CustomerAuthTokenEntity customerAuthTokenEntity = customerDao.getCustomerAuthToken(authorizationToken);
        if (customerAuthTokenEntity==null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        } else if (customerAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        } else if (customerAuthTokenEntity.getExpiresAt().compareTo(ZonedDateTime.now()) < 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
        return addressDao.createAddress(addressEntity);
    }


}
