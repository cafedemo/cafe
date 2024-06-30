package tut.dushyant.cafedemo.cafe.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tut.dushyant.cafedemo.cafe.db.repo.CustomerRepo;
import tut.dushyant.cafedemo.cafe.web.data.Customer;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/customers")
@Slf4j
public class CustomerWebController {

    private final CustomerRepo repo;

    public CustomerWebController(CustomerRepo repo) {
        this.repo = repo;
    }

    @GetMapping("")
    List<Customer> getCustomers() {
        log.info("Getting all customers");
        return StreamSupport.stream(repo.findAll().spliterator(), false).map(Customer::new).toList();
    }

    @GetMapping("/{id}")
    Customer getCustomer(@PathVariable Long id) {
        log.info("Getting customer id: {}", id);
        return new Customer(repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")));
    }

    @PostMapping("")
    Customer addCustomer(@RequestBody Customer customer) {
        log.info("Adding customer: {}", customer);
        try {
            return new Customer(repo.save(customer.toEntity()));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate constraint violated", e);
        }
    }

    @PutMapping("")
    Customer updateCustomer(@RequestBody Customer customer) {
        log.info("Updating customer: {}", customer);
        try {
            // get customer data for given id. If id is not found, throw 404
            var customerDB = repo.findById(customer.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer with id "+customer.getId()+" not found"));
            // update customer found
            customerDB.setEmail(customer.getEmail());
            customerDB.setName(customer.getName());
            // save updated customer
            return new Customer(repo.save(customerDB));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate constraint violated", e);
        }
    }

    @DeleteMapping("/{id}")
    Customer deleteCustomer(@PathVariable Long id) {
        log.info("Deleting customer id: {}", id);
        // get customer data for given id. If id is not found, throw 404
        var customer = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer with id "+id+" not found"));
        // delete customer found
        repo.delete(customer);
        // return status of 200 with body of customer
        return new Customer(customer);
    }
}
