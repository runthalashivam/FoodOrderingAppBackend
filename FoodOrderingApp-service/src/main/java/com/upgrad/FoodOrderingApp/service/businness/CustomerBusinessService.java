package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.validator.routines.EmailValidator;

import javax.transaction.Transactional;
import java.util.regex.Pattern;

@Service
public class CustomerBusinessService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    @Transactional
    public CustomerEntity signup(CustomerEntity customerEntity) throws SignUpRestrictedException {

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

    private boolean isPasswordWeak(String password) {
        return (password.length() < 8
                || !Pattern.compile("\\p{Nd}").matcher(password).find()
                || !Pattern.compile("\\p{Ll}").matcher(password).find()
                || !Pattern.compile("[#@$%&*!^\\[\\]]").matcher(password).find());
    }
}