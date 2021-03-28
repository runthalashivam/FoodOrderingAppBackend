package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.OrderBusinessService;
import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @Autowired
    private CustomerService customerService;

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

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/order/coupon/{coupon_name}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CouponDetailsResponse> getCouponByName(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("coupon_name") final String couponName)
            throws AuthorizationFailedException, CouponNotFoundException {

        String[] bearerToken = authorization.split("Bearer ");
        String accessToken = bearerToken[1];

        customerService.getCustomer(accessToken);

        CouponEntity couponEntity = orderBusinessService.getCouponByCouponName(couponName);

        CouponDetailsResponse couponDetailsResponse = new CouponDetailsResponse();
        couponDetailsResponse.setId(UUID.fromString(couponEntity.getUuid()));
        couponDetailsResponse.setCouponName(couponEntity.getCouponName());
        couponDetailsResponse.setPercent(couponEntity.getPercent());

        return new ResponseEntity<>(couponDetailsResponse, HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/order",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CustomerOrderResponse> getOrdersByCustomer(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {

        String[] bearerToken = authorization.split("Bearer ");
        String accessToken = bearerToken[1];

        // Identify customer from the access token.
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        // Get all the orders of the customer.
        List<OrderEntity> ordersOfCustomer =
                orderBusinessService.getOrdersByCustomers(customerEntity.getUuid());

        List<OrderList> orders = new ArrayList<>();
        for (OrderEntity orderEntity : ordersOfCustomer) {
            OrderList order = new OrderList();
            order.setId(UUID.fromString(orderEntity.getUuid()));
            order.setDate(orderEntity.getDate().toString());
            order.setBill(orderEntity.getBill());
            order.setDiscount(orderEntity.getDiscount());
            order.setCustomer(getOrderListCustomer(orderEntity.getCustomer()));
            order.setCoupon(getOrderListCoupon(orderEntity.getCoupon()));
            order.setAddress(getOrderListAddress(orderEntity.getAddress()));
            order.setPayment(getOrderListPayment(orderEntity.getPayment()));
            List<OrderItemEntity> orderItems = orderEntity.getOrderItems();
            order.setItemQuantities(getItemQuantityResponseList(orderItems));
            orders.add(order);
        }

        CustomerOrderResponse customerOrderResponse = new CustomerOrderResponse();
        customerOrderResponse.setOrders(orders);
        return new ResponseEntity<CustomerOrderResponse>(customerOrderResponse, HttpStatus.OK);
    }

    private OrderListCustomer getOrderListCustomer(CustomerEntity customer) {
        OrderListCustomer orderListCustomer = new OrderListCustomer();
        orderListCustomer.setId(UUID.fromString(customer.getUuid()));
        orderListCustomer.setFirstName(customer.getFirstName());
        orderListCustomer.setLastName(customer.getLastName());
        orderListCustomer.setEmailAddress(customer.getEmail());
        orderListCustomer.setContactNumber(customer.getContactNumber());
        return orderListCustomer;
    }

    private OrderListCoupon getOrderListCoupon(CouponEntity coupon) {
        OrderListCoupon orderListCoupon = new OrderListCoupon();
        orderListCoupon.setId(UUID.fromString(coupon.getUuid()));
        orderListCoupon.setCouponName(coupon.getCouponName());
        orderListCoupon.setPercent(coupon.getPercent());
        return orderListCoupon;
    }

    private OrderListPayment getOrderListPayment(PaymentEntity payment) {
        OrderListPayment orderListPayment = new OrderListPayment();
        orderListPayment.setId(UUID.fromString(payment.getUuid()));
        orderListPayment.setPaymentName(payment.getPaymentName());
        return orderListPayment;
    }

    private OrderListAddress getOrderListAddress(AddressEntity address) {
        OrderListAddress orderListAddress = new OrderListAddress();
        orderListAddress.setId(UUID.fromString(address.getUuid()));
        orderListAddress.setFlatBuildingName(address.getFlatBuilNumber());
        orderListAddress.setLocality(address.getLocality());
        orderListAddress.setCity(address.getCity());
        orderListAddress.setPincode(address.getPincode());
        OrderListAddressState orderListAddressState = new OrderListAddressState();
        orderListAddressState.setId(UUID.fromString(address.getState().getUuid()));
        orderListAddressState.setStateName(address.getState().getStateName());
        orderListAddress.setState(orderListAddressState);
        return orderListAddress;
    }

    private List<ItemQuantityResponse> getItemQuantityResponseList(List<OrderItemEntity> items) {
        List<ItemQuantityResponse> responseList = new ArrayList<>();

        for (OrderItemEntity orderItem : items) {
            ItemQuantityResponse response = new ItemQuantityResponse();

            ItemQuantityResponseItem responseItem = new ItemQuantityResponseItem();
            responseItem.setId(UUID.fromString(orderItem.getItem().getUuid()));
            responseItem.setItemName(orderItem.getItem().getItemName());
            responseItem.setItemPrice(orderItem.getItem().getPrice());
            ItemQuantityResponseItem.TypeEnum itemType =
                    Integer.valueOf(orderItem.getItem().getType()) == 0
                            ? ItemQuantityResponseItem.TypeEnum.VEG
                            : ItemQuantityResponseItem.TypeEnum.NON_VEG;
            responseItem.setType(itemType);
            response.setItem(responseItem);

            response.setQuantity(orderItem.getQuantity());
            response.setPrice(orderItem.getPrice());
            responseList.add(response);
        }
        return responseList;
    }
}
