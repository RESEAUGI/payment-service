package com.mo.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mo.entities.Payment;
import com.mo.repositories.PaymentRepository;

@Service
public class PaymentService {
	
	@Autowired
	PaymentRepository paymentRepository;
	
	public Iterable<Payment> getPaymentHistory(){
		return paymentRepository.findAll();
	}
	
	 public void schedulePaymentUpdateTask(String paymentId) {
	        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	        executor.schedule(() -> {
	            Payment payment = paymentRepository.findById(paymentId).orElse(null);
	            if (payment != null && "pending".equals(payment.getTransaction_status())) {
	                payment.setTransaction_status("failed");
	                paymentRepository.save(payment);
	            }
	        }, 60, TimeUnit.MINUTES);
	    }
	
	

}
