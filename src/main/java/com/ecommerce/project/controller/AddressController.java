package com.ecommerce.project.controller;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/address")
    public ResponseEntity<AddressDTO> createAddress(@RequestBody AddressDTO addressDTO){
        User user = authUtil.loggedInUser();
        AddressDTO addressDTO1 = addressService.createAddress(addressDTO,user);
        return ResponseEntity.status(HttpStatus.CREATED).body(addressDTO1);
    }

    @GetMapping("/address")
    public ResponseEntity<List<AddressDTO>> getAddress(){

        List<AddressDTO> addressDTO = addressService.getAddress();
        return ResponseEntity.status(HttpStatus.OK).body(addressDTO);

    }
    @GetMapping("/address/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId){

        AddressDTO addressDTO = addressService.getAddressById(addressId);
        return ResponseEntity.status(HttpStatus.OK).body(addressDTO);

    }
    @GetMapping("/address/user")
    public ResponseEntity<List<AddressDTO>> getAddressByUser(){
        Long userId = authUtil.loggedInUserId();

        List<AddressDTO> addressDTO = addressService.getAddressByUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(addressDTO);

    }
    @PutMapping("/address/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId,
                                                              @RequestBody AddressDTO addressDTO){
        AddressDTO addressDTO1 = addressService.updateAddress(addressId, addressDTO);
        return ResponseEntity.status(HttpStatus.OK).body(addressDTO1);

    }
    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long addressId){
        String status = addressService.deleteAddress(addressId);
        return ResponseEntity.status(HttpStatus.OK).body(status);

    }
}
