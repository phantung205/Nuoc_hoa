package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.dto.UserAddressDTO;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.entity.UserAddress;
import com.perfumes.nuochoa.repository.UserAddressRepository;
import com.perfumes.nuochoa.repository.UserRepository;
import com.perfumes.nuochoa.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    public UserAddressServiceImpl(UserAddressRepository userAddressRepository, UserRepository userRepository) {
        this.userAddressRepository = userAddressRepository;
        this.userRepository = userRepository;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    @Override
    public List<UserAddress> getUserAddresses(String username) {
        User user = getUserByUsername(username);
        return userAddressRepository.findByUserId(user.getId());
    }

    @Override
    public UserAddress getAddressById(String username, Long id) {
        User user = getUserByUsername(username);
        UserAddress address = userAddressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Không có quyền truy cập địa chỉ này");
        }
        return address;
    }

    @Override
    @Transactional
    public UserAddress addAddress(String username, UserAddressDTO dto) {
        User user = getUserByUsername(username);
        List<UserAddress> existingAddresses = userAddressRepository.findByUserId(user.getId());
        
        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setReceiverName(dto.getReceiverName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setReceiverAddress(dto.getReceiverAddress());
        address.setAddressType(dto.getAddressType());
        address.setNote(dto.getNote());
        
        // Nếu đây là địa chỉ đầu tiên hoặc được tick làm mặc định
        if (existingAddresses.isEmpty() || Boolean.TRUE.equals(dto.getIsDefault())) {
            if (Boolean.TRUE.equals(dto.getIsDefault())) {
                existingAddresses.forEach(a -> {
                    a.setIsDefault(false);
                    userAddressRepository.save(a);
                });
            }
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }
        
        return userAddressRepository.save(address);
    }

    @Override
    @Transactional
    public void updateAddress(String username, Long id, UserAddressDTO dto) {
        UserAddress address = getAddressById(username, id);
        
        address.setReceiverName(dto.getReceiverName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setReceiverAddress(dto.getReceiverAddress());
        address.setAddressType(dto.getAddressType());
        address.setNote(dto.getNote());
        
        if (Boolean.TRUE.equals(dto.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            setDefaultAddress(username, id);
        } else {
            userAddressRepository.save(address);
        }
    }

    @Override
    @Transactional
    public void deleteAddress(String username, Long id) {
        UserAddress address = getAddressById(username, id);
        userAddressRepository.delete(address);
        
        // Nếu xóa địa chỉ mặc định, set địa chỉ khác làm mặc định (nếu có)
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            List<UserAddress> remaining = userAddressRepository.findByUserId(address.getUser().getId());
            if (!remaining.isEmpty()) {
                UserAddress newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                userAddressRepository.save(newDefault);
            }
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(String username, Long id) {
        User user = getUserByUsername(username);
        List<UserAddress> addresses = userAddressRepository.findByUserId(user.getId());
        
        for (UserAddress a : addresses) {
            if (a.getId().equals(id)) {
                a.setIsDefault(true);
            } else {
                a.setIsDefault(false);
            }
            userAddressRepository.save(a);
        }
    }
}
