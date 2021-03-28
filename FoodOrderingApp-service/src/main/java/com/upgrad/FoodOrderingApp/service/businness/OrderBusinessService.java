package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class OrderBusinessService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private RestaurantDao restaurantDao;

    @Transactional
    public OrderEntity saveOrder(OrderEntity order, final String authorizationToken, List<OrderItemEntity> orderItemList) throws AuthorizationFailedException, CouponNotFoundException, AddressNotFoundException, PaymentMethodNotFoundException, RestaurantNotFoundException, ItemNotFoundException {
        //Validate customer.
        CustomerAuthEntity customerAuthTokenEntity = customerDao.getCustomerAuthToken(authorizationToken);
        if (customerAuthTokenEntity==null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        } else if (customerAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        } else if (customerAuthTokenEntity.getExpiresAt().compareTo(ZonedDateTime.now()) < 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        //Once customer is validated, set the customer to the order.
        CustomerEntity customer = customerAuthTokenEntity.getCustomer();
        order.setCustomer(customer);


        //Check if coupon id entered by customer exists in database
        if(order.getCoupon() == null) {
            throw new CouponNotFoundException("CPF-002", "No coupon by this id");
        }

        //Check if address id entered by customer exists in database
        if(order.getAddress() == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        //Check if address id entered by customer belongs to that customer
        CustomerEntity loggedInCustomer = customerAuthTokenEntity.getCustomer();
        CustomerEntity addressOwner = addressDao.getCustomerByAddress(order.getAddress());
        if(loggedInCustomer != addressOwner) {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }

        //Check if payment type exists in the database
        if(order.getPayment() == null) {
            throw new PaymentMethodNotFoundException("PNF-002", "No payment method found by this id");
        }

        //Check if the restaurant exists in the database
        if(order.getRestaurant() == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }

        OrderEntity createdOrder = orderDao.createOrder(order);

        for(OrderItemEntity orderItemEntity : orderItemList) {
            if(orderItemEntity.getItem() == null) {
                throw new ItemNotFoundException("INF-003", "No item by this id exist");
            } else {
                orderItemEntity.setOrder(createdOrder);
                orderDao.mapOrderToItem(orderItemEntity);
            }
        }

        return createdOrder;
    }

}
