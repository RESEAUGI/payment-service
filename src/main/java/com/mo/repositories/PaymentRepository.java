package com.mo.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.mo.entities.Payment;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, String> {
	
    Optional<Iterable<Payment>> findByConsumerId(String consumer_id);
}
