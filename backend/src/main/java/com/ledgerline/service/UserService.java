package com.ledgerline.service;

import com.ledgerline.dto.request.UpdateUserRequest;
import com.ledgerline.dto.response.RoleResponse;
import com.ledgerline.dto.response.UserResponse;
import com.ledgerline.entity.Role;
import com.ledgerline.entity.User;
import com.ledgerline.exception.ResourceNotFoundException;
import com.ledgerline.mapper.UserMapper;
import com.ledgerline.repository.RoleRepository;
import com.ledgerline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUserProfile(Long userId, UpdateUserRequest request, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFullName(request.getFullName().trim());
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        User updated = userRepository.save(user);

        auditLogService.log(updated, "USER_PROFILE_UPDATE", "USER", String.valueOf(updated.getId()), ipAddress,
                "User profile details updated");

        return userMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapRoleToResponse)
                .collect(Collectors.toList());
    }

    private RoleResponse mapRoleToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .build();
    }
}
