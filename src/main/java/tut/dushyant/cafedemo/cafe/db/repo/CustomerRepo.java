package tut.dushyant.cafedemo.cafe.db.repo;

import org.springframework.data.repository.CrudRepository;
import tut.dushyant.cafedemo.cafe.db.entity.Customer;

public interface CustomerRepo extends CrudRepository<Customer, Long>{}
