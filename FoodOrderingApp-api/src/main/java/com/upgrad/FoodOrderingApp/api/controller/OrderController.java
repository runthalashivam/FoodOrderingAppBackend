package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.ItemQuantity;
import com.upgrad.FoodOrderingApp.api.model.SaveOrderRequest;
import com.upgrad.FoodOrderingApp.api.model.SaveOrderResponse;
import com.upgrad.FoodOrderingApp.service.businness.OrderBusinessService;
import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class OrderController {

    @Autowired
    private OrderBusinessService orderBusinessService;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private RestaurantDao restaurantDao;

    @Autowired
    private ItemDao itemDao;

    @RequestMapping(method = RequestMethod.POST, path = "/order", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveOrderResponse> saveOrder(final SaveOrderRequest saveOrderRequest, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, CouponNotFoundException, AddressNotFoundException, RestaurantNotFoundException, PaymentMethodNotFoundException, ItemNotFoundException {

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUuid(UUID.randomUUID().toString());
        orderEntity.setAddress(addressDao.getAddressByUuid(saveOrderRequest.getAddressId()));
        orderEntity.setPayment(paymentDao.getPaymentByUuid(saveOrderRequest.getPaymentId().toString()));
        orderEntity.setBill(saveOrderRequest.getBill());
        orderEntity.setDiscount(saveOrderRequest.getDiscount());
        orderEntity.setDate(ZonedDateTime.now());
        orderEntity.setCoupon(orderDao.getCouponByUuid(saveOrderRequest.getCouponId().toString()));
        orderEntity.setRestaurant(restaurantDao.getRestaurantByUuid(saveOrderRequest.getRestaurantId().toString()));

        String[] bearerToken = authorization.split("Bearer ");

        List<ItemQuantity> itemQuantityList = saveOrderRequest.getItemQuantities();
        List<OrderItemEntity> orderItemList = new ArrayList<>();
        for(ItemQuantity itemQuantity : itemQuantityList) {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setItem(itemDao.getItemByUuid(itemQuantity.getItemId().toString()));
            System.out.println("Item: " + orderItemEntity.getItem());
            orderItemEntity.setPrice(itemQuantity.getPrice());
            orderItemEntity.setQuantity(itemQuantity.getQuantity());
            orderItemList.add(orderItemEntity);
        }
        final OrderEntity createdOrderEntity = orderBusinessService.saveOrder(orderEntity, bearerToken[1], orderItemList);

        SaveOrderResponse saveOrderResponse = new SaveOrderResponse().id(createdOrderEntity.getUuid())
                .status("ORDER SUCCESSFULLY PLACED");
        return new ResponseEntity<SaveOrderResponse>(saveOrderResponse, HttpStatus.CREATED);
    }
}
