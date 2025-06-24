package com.bighomework.planeTicketWeb.service;

import com.bighomework.planeTicketWeb.dto.requestDTO.RegisterRequest;
import com.bighomework.planeTicketWeb.entity.Customer;

public interface AuthService {
    Customer registerUser(RegisterRequest registerRequest);
}