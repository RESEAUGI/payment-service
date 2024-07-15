package com.mo.providers;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;

import com.mo.entities.MyCoolPayOperator;
import com.mo.entities.Product;
import com.mo.entities.MyProduct;
import com.mo.entities.Payment;
import com.mo.entities.PaymentType;
import com.mo.entities.StripeOperator;
import com.mo.interfaces.PaymentInterface;
import com.mo.stripe.LinkPay;

public class PaymentProvider {
	private MyCoolPayOperator mycoolpay;
	private StripeOperator stripe;
	
	
	

   public PaymentProvider(MyCoolPayOperator mycoolpay, StripeOperator stripe) {
		super();
		this.mycoolpay = mycoolpay;
		this.stripe = stripe;
		
	}
   



	public MyCoolPayOperator getMycoolpay() {
		return mycoolpay;
	}
	
	
	
	
	public void setMycoolpay(MyCoolPayOperator mycoolpay) {
		this.mycoolpay = mycoolpay;
	}
	
	
	
	
	public StripeOperator getStripe() {
		return stripe;
	}
	
	
	
	
	public void setStripe(StripeOperator stripe) {
		this.stripe = stripe;
	}
	
	
	
	
	
	
	public String suscribe(MyProduct product) {
		LinkPay linkPay = null;
	    switch (product.getPayment_type()) {
	        case card:
	            if (stripe.isValid(product.getAmount(), product.getCurrency())) {
	            	linkPay = stripe.payIn(product);
	            }
	            break;
	        case mobile:
	            if (mycoolpay.isValid(product.getAmount(), product.getCurrency())) {
	            	linkPay = mycoolpay.payIn(product);
	            }
	            break;
	    }
	    return linkPay.getUrl();
	}
}