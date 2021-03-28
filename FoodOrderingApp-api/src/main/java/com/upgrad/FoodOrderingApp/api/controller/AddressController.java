package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressBusinessService;
import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.apache.tomcat.jni.Address;
import org.hibernate.sql.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class AddressController {

    @Autowired
    private AddressBusinessService addressBusinessService;

    @Autowired
    private AddressDao addressDao;

    @RequestMapping(method = RequestMethod.POST, path = "/address", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(final SaveAddressRequest saveAddressRequest,
                                                           @RequestHeader("authorization") final String authorization) throws AddressNotFoundException, SaveAddressException, AuthorizationFailedException {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity.setFlatBuilNumber(saveAddressRequest.getFlatBuildingName());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setPincode(saveAddressRequest.getPincode());
        addressEntity.setState(addressDao.getStateByUuid(saveAddressRequest.getStateUuid()));
        addressEntity.setActive(1);

        String[] bearerToken = authorization.split("Bearer ");

        final AddressEntity createdAddressEntity = addressBusinessService.saveAddress(addressEntity, bearerToken[1]);

        SaveAddressResponse saveAddressResponse = new SaveAddressResponse().id(createdAddressEntity.getUuid())
                .status("ADDRESS SUCCESSFULLY SAVED");

        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.CREATED);
    }


    //Method to get all the saved addresses of a particular customer
    @RequestMapping(method = RequestMethod.GET, path = "/address/customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAllPermanentAddresses(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {

        String[] bearerToken = authorization.split("Bearer ");

        List<AddressEntity> addressEntities = addressBusinessService.getAllAddressesByCustomer(bearerToken[1]);

        List<AddressList> addresses = new ArrayList<AddressList>();

        //Populate the new list with the list of addresses returned by the addressBusinessService class
        if(addressEntities != null) {
            for (AddressEntity addressEntity : addressEntities) {
                AddressList addressListObject = new AddressList().id(UUID.fromString(addressEntity.getUuid()))
                        .flatBuildingName(addressEntity.getFlatBuilNumber())
                        .locality(addressEntity.getLocality())
                        .city(addressEntity.getCity())
                        .pincode(addressEntity.getPincode())
                        .state(new AddressListState().id(UUID.fromString(addressEntity.getState().getUuid()))
                                .stateName(addressEntity.getState().getStateName()));
                addresses.add(addressListObject);
            }
        }

        //Attach the list to the AddressListResponse before returning it in the ResponseEntity
        AddressListResponse addressListResponse = new AddressListResponse().addresses(addresses);

        return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.OK);
    }

    //Method to delete an address of a customer
    @RequestMapping(method = RequestMethod.DELETE, path = "/address/delete/{addressId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteAddress(@PathVariable String addressId, @RequestHeader("authorization") String authorization) throws AuthorizationFailedException, AddressNotFoundException {
        String[] bearerToken = authorization.split("Bearer ");
        addressBusinessService.deleteAddress(addressId, bearerToken[1]);
        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse()
                .id(UUID.fromString(addressId))
                .status("ADDRESS DELETED SUCCESSFULLY");
        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);
    }

    //Method to get all states
    @RequestMapping(method = RequestMethod.GET, path = "/states", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StatesListResponse> getAllStates() {
        List<StateEntity> states = addressBusinessService.getAllStates();
        List<StatesList> statesLists = new ArrayList<>();
        for(StateEntity state : states) {
            statesLists.add(new StatesList().id(UUID.fromString(state.getUuid())).stateName(state.getStateName()));
        }
        StatesListResponse statesListResponse = new StatesListResponse().states(statesLists);
        return new ResponseEntity<StatesListResponse>(statesListResponse, HttpStatus.OK);
    }

}
