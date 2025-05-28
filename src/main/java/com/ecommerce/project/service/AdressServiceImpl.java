package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdressServiceImpl implements AddressService{

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;
    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO,Address.class);
        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);
        address.setUser(user);
        Address savedAddress = addressRepository.save(address);
        AddressDTO addressDTO1 = modelMapper.map(savedAddress,AddressDTO.class);
        return addressDTO1;


    }

    @Override
    public List<AddressDTO> getAddress() {
       List<Address> addresses =  addressRepository.findAll();
       List<AddressDTO> addressDTO = new ArrayList<>();
       for(int i=0; i<addresses.size(); i++){
           AddressDTO addressDTO1 = modelMapper.map(addresses.get(i),AddressDTO.class);
           addressDTO.add(addressDTO1);
       }
       return addressDTO;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address =  addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("address","addressId",addressId));

        AddressDTO addressDTO = modelMapper.map(address,AddressDTO.class);

        return addressDTO;


    }

    @Override
    public List<AddressDTO> getAddressByUser(Long userId) {
       User user = userRepository.findById(userId)
               .orElseThrow(()->new UsernameNotFoundException("user not found!"));
       List<Address> addresses = user.getAddresses();
       List<AddressDTO> addressDTOS = new ArrayList<>();
       for(int i=0; i<addresses.size(); i++){
           AddressDTO addressDTO = modelMapper.map(addresses.get(i),AddressDTO.class);
           addressDTOS.add(addressDTO);
       }
       return addressDTOS;
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address addresses =  addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("address","addressId",addressId));
        addresses.setCity(addressDTO.getCity());
        addresses.setCountry(addressDTO.getCountry());
        addresses.setStreet(addressDTO.getStreet());
        addresses.setBuildingName(addressDTO.getBuildingName());
        addresses.setState(addressDTO.getState());
        addresses.setPinCode(addressDTO.getPincode());
        Address updatedAddress = addressRepository.save(addresses);
        User user = addresses.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address addressFromDatabase = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        User user = addressFromDatabase.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        userRepository.save(user);

        addressRepository.delete(addressFromDatabase);

        return "Address deleted successfully with addressId: " + addressId;

    }
}
