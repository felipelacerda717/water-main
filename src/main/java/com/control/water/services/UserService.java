package com.control.water.services;

import com.control.water.models.User;
import com.control.water.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User registrar(User user) {
        // Verifica se já existe usuário com este email
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email já cadastrado");
        }
        return userRepository.save(user);
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}