package com.mo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.pulsar.core.DefaultSchemaResolver;
import org.springframework.pulsar.core.SchemaResolver.SchemaResolverCustomizer;
import com.mo.entities.MyProduct;
import com.mo.entities.PaymentType;
import com.mo.entities.StripeOperator;
import com.mo.providers.PaymentProvider;
import com.stripe.model.checkout.Session;
import com.datastax.oss.driver.api.core.CqlSession;
import com.google.gson.Gson;
import com.mo.controllers.CommunMethodsController;

import com.mo.entities.MyCoolPayOperator;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;

@Configuration
public class DataConfig {
	
	private PaymentType paymentType;

	@Bean
	public SchemaResolverCustomizer<DefaultSchemaResolver> schemaResolverCustomizer() {
	    return (schemaResolver) -> {
	        schemaResolver.addCustomSchemaMapping(MyProduct.class, Schema.JSON(MyProduct.class));
	        
	    };
    }
	
	@Bean
	public PaymentProvider paymentProvider(MyCoolPayOperator myCoolPayOperator, StripeOperator stripeOperator) {
	     return new PaymentProvider(myCoolPayOperator, stripeOperator);
	}
	
	
	
	@Bean
    public MyCoolPayOperator myCoolPayOperator(CommunMethodsController communMethodsController, PulsarClient client) {
        return new MyCoolPayOperator(communMethodsController, client);
    }
	
	@Bean
	public CommunMethodsController communMethodsController() {
	   return new CommunMethodsController ();
	}
    
    
	@Bean
    public StripeOperator stripeOperator(CommunMethodsController communMethodsController, PulsarClient client) {
        return new StripeOperator(communMethodsController, client);
    }
	
	 @Bean
	 public PaymentType paymentType() {
		 return this.paymentType = paymentType;
	 }

}
