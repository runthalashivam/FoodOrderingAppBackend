package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.transaction.annotation.Propagation;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    @Transactional
    public CustomerEntity saveCustomer(CustomerEntity customerEntity) throws SignUpRestrictedException {

        if (customerDao.getCustomerByContactNumber(customerEntity.getContactNumber()) != null) {
            throw new SignUpRestrictedException("SGR-001",
                    "This contact number is already registered! Try other contact number.");
        } else if (customerEntity.getFirstName() == null
                || customerEntity.getEmail() == null
                || customerEntity.getContactNumber() == null
                || customerEntity.getPassword() == null) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }
        if (!EmailValidator.getInstance().isValid(customerEntity.getEmail())) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        } else if (!customerEntity.getContactNumber().matches("[0-9]+")
                || customerEntity.getContactNumber().length() != 10) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        } else if (isPasswordWeak(customerEntity.getPassword())) {
            throw new SignUpRestrictedException("SGR-004", "Weak password!");
        } else {
            String[] encryptedText = passwordCryptographyProvider.encrypt(customerEntity.getPassword());
            customerEntity.setSalt(encryptedText[0]);
            customerEntity.setPassword(encryptedText[1]);
            return customerDao.createCustomer(customerEntity);
        }
    }

    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(final String contactNumber, final String password) throws AuthenticationFailedException {

        CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(contactNumber);
        if (customerEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }

        final String encryptedPassword = passwordCryptographyProvider.encrypt(password, customerEntity.getSalt());
        if (encryptedPassword.equals(customerEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
            customerAuthEntity.setCustomer(customerEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(
                    customerAuthEntity.getUuid(), now, expiresAt));
            customerAuthEntity.setLoginAt(now);
            customerAuthEntity.setExpiresAt(expiresAt);
            customerAuthEntity.setUuid(UUID.randomUUID().toString());
            customerDao.createAuthToken(customerAuthEntity);
            return customerAuthEntity;
        } else {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public CustomerAuthEntity logout(final String authorizationToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(authorizationToken);

        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        } else if (customerAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002",
                    "Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime now = ZonedDateTime.now();
        if (now.isAfter(customerAuthEntity.getExpiresAt())) {
            throw new AuthorizationFailedException("ATHR-003",
                    "Your session is expired. Log in again to access this endpoint.");
        }
        customerAuthEntity.setLogoutAt(now);
        customerDao.updateCustomerAuth(customerAuthEntity);
        return customerAuthEntity;
    }

    @Transactional
    public CustomerEntity updateCustomer(CustomerEntity customerEntity)
            throws AuthorizationFailedException {
        customerDao.updateCustomer(customerEntity);
        return customerEntity;
    }

    @Transactional
    public CustomerEntity updateCustomerPassword(final String oldPassword, final String newPassword, final CustomerEntity customerEntity)
            throws AuthorizationFailedException, UpdateCustomerException {

        final String encryptedPassword = passwordCryptographyProvider.encrypt(oldPassword, customerEntity.getSalt());

        if (!encryptedPassword.equals(customerEntity.getPassword())) {
            throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
        } else if (isPasswordWeak(newPassword)) {
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }
        customerEntity.setPassword(newPassword);
        String[] encryptedText = passwordCryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);
        customerDao.updateCustomer(customerEntity);
        return customerEntity;
    }

    public CustomerEntity getCustomer(String accessToken) throws AuthorizationFailedException {
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(accessToken);
        if (customerAuthEntity != null) {
            if (customerAuthEntity.getLogoutAt() != null) {
                throw new AuthorizationFailedException("ATHR-002",
                        "Customer is logged out. Log in again to access this endpoint.");
            }
            if (ZonedDateTime.now().isAfter(customerAuthEntity.getExpiresAt())) {
                throw new AuthorizationFailedException("ATHR-003",
                        "Your session is expired. Log in again to access this endpoint.");
            }
            return customerAuthEntity.getCustomer();
        } else {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
    }

    private boolean isPasswordWeak(String password) {
        return (password.length() < 8
                || !Pattern.compile("\\p{Nd}").matcher(password).find()
                || !Pattern.compile("\\p{Ll}").matcher(password).find()
                || !Pattern.compile("[#@$%&*!^\\[\\]]").matcher(password).find());
    }
}