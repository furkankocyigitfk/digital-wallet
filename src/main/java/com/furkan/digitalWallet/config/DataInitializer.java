package com.furkan.digitalWallet.config;

import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.entity.Wallet;
import com.furkan.digitalWallet.enums.Currency;
import com.furkan.digitalWallet.enums.Role;
import com.furkan.digitalWallet.repository.CustomerRepository;
import com.furkan.digitalWallet.repository.WalletRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class DataInitializer {
    private CustomerRepository customerRepository;
    private WalletRepository walletRepository;
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    @ConditionalOnProperty(name = "data.init", havingValue = "false")
    public void init() {
        Customer employee = new Customer();
        employee.setName("Ayşe");
        employee.setSurname("Yılmaz");
        employee.setTckn("11111111111");
        employee.setUsername("employee");
        employee.setPassword(passwordEncoder.encode("employee123"));
        employee.setRole(Role.EMPLOYEE);
        customerRepository.save(employee);

        Customer customer1 = new Customer();
        customer1.setName("Ali");
        customer1.setSurname("Kaya");
        customer1.setTckn("22222222222");
        customer1.setUsername("customer1");
        customer1.setPassword(passwordEncoder.encode("customer123"));
        customer1.setRole(Role.CUSTOMER);
        customerRepository.save(customer1);

        Customer customer2 = new Customer();
        customer2.setName("Mehmet");
        customer2.setSurname("Özkan");
        customer2.setTckn("33333333333");
        customer2.setUsername("customer2");
        customer2.setPassword(passwordEncoder.encode("password123"));
        customer2.setRole(Role.CUSTOMER);
        customerRepository.save(customer2);

        Customer customer3 = new Customer();
        customer3.setName("Fatma");
        customer3.setSurname("Demir");
        customer3.setTckn("44444444444");
        customer3.setUsername("customer3");
        customer3.setPassword(passwordEncoder.encode("test123"));
        customer3.setRole(Role.CUSTOMER);
        customerRepository.save(customer3);

        Customer customer4 = new Customer();
        customer4.setName("Ahmet");
        customer4.setSurname("Çelik");
        customer4.setTckn("55555555555");
        customer4.setUsername("customer4");
        customer4.setPassword(passwordEncoder.encode("demo123"));
        customer4.setRole(Role.CUSTOMER);
        customerRepository.save(customer4);

        createWalletsForCustomer(customer1);
        createWalletsForCustomer(customer2);
        createWalletsForCustomer(customer3);

        Wallet tryWallet4 = new Wallet();
        tryWallet4.setCustomer(customer4);
        tryWallet4.setWalletName("Ana TRY Cüzdan");
        tryWallet4.setCurrency(Currency.TRY);
        tryWallet4.setActiveForShopping(true);
        tryWallet4.setActiveForWithdraw(true);
        tryWallet4.setBalance(new BigDecimal("5000.00"));
        tryWallet4.setUsableBalance(new BigDecimal("5000.00"));
        tryWallet4.setCreatedAt(LocalDateTime.now());
        walletRepository.save(tryWallet4);
        
    }

    private void createWalletsForCustomer(Customer customer) {
        Wallet tryWallet = new Wallet();
        tryWallet.setCustomer(customer);
        tryWallet.setWalletName("Ana TRY Cüzdan");
        tryWallet.setCurrency(Currency.TRY);
        tryWallet.setActiveForShopping(true);
        tryWallet.setActiveForWithdraw(true);
        tryWallet.setBalance(new BigDecimal("10000.00"));
        tryWallet.setUsableBalance(new BigDecimal("10000.00"));
        tryWallet.setCreatedAt(LocalDateTime.now());
        walletRepository.save(tryWallet);

        Wallet usdWallet = new Wallet();
        usdWallet.setCustomer(customer);
        usdWallet.setWalletName("USD Cüzdan");
        usdWallet.setCurrency(Currency.USD);
        usdWallet.setActiveForShopping(true);
        usdWallet.setActiveForWithdraw(true);
        usdWallet.setBalance(new BigDecimal("1000.00"));
        usdWallet.setUsableBalance(new BigDecimal("1000.00"));
        usdWallet.setCreatedAt(LocalDateTime.now());
        walletRepository.save(usdWallet);

        Wallet eurWallet = new Wallet();
        eurWallet.setCustomer(customer);
        eurWallet.setWalletName("EUR Cüzdan");
        eurWallet.setCurrency(Currency.EUR);
        eurWallet.setActiveForShopping(true);
        eurWallet.setActiveForWithdraw(true);
        eurWallet.setBalance(new BigDecimal("800.00"));
        eurWallet.setUsableBalance(new BigDecimal("800.00"));
        eurWallet.setCreatedAt(LocalDateTime.now());
        walletRepository.save(eurWallet);
    }
}