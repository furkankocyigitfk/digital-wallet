package com.furkan.digitalWallet.repository;

import com.furkan.digitalWallet.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsername(String username);
    boolean existsByTckn(String tckn);
    boolean existsByUsername(String username);
}

