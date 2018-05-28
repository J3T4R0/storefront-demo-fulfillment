package com.storefront.controller;

import com.storefront.Utility;
import com.storefront.model.Customer;
import com.storefront.respository.CustomerRepository;
import com.storefront.respository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private CustomerRepository customerRepository;

    private ProductRepository productRepository;

    @Autowired
    public CustomerController(CustomerRepository customerRepository, ProductRepository productRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @RequestMapping(path = "/sample", method = RequestMethod.GET)
    public ResponseEntity<String> sampleData() {
        Utility utility = new Utility(customerRepository, productRepository);
        utility.createTestProducts();
        utility.createOrderHistory();
//        utility.createTestCustomers();
        return new ResponseEntity("Sample data created", HttpStatus.CREATED);
    }

    @RequestMapping(path = "/summary", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, List<Customer>>> candidateSummary() {
        List<Customer> candidateList = customerRepository.findAll();
        return new ResponseEntity<>(Collections.singletonMap("candidates", candidateList), HttpStatus.OK);
    }
}
