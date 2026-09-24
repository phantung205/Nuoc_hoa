package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.dto.UserAddressDTO;
import com.perfumes.nuochoa.entity.UserAddress;
import java.util.List;

public interface UserAddressService {
    List<UserAddress> getUserAddresses(String username);
    UserAddress getAddressById(String username, Long id);
    UserAddress addAddress(String username, UserAddressDTO dto);
    void updateAddress(String username, Long id, UserAddressDTO dto);
    void deleteAddress(String username, Long id);
    void setDefaultAddress(String username, Long id);
}
