package com.mo.entities;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.mo.interfaces.CallbackInterface;
import com.mo.interfaces.PaymentInterface;
import com.mo.interfaces.ValidateDataInterface;
import com.mo.stripe.LinkPay;
import com.google.gson.Gson;



public class MyCoolPayOperator implements CallbackInterface, PaymentInterface, ValidateDataInterface {

	@Override
	public boolean isValid(Long amount, String currency) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkPay payIn(MyProduct product) {
		// TODO Auto-generated method stub
	
		return null;

	}

	@Override
	public int operator() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ResponseEntity<String> handle_payment(@RequestBody String payload) {
//		CoolpayResponse coolpayResponse = new CoolpayResponse()
//		System.out.print(payload);
		
		//String json = "{\"application\":\"{public_key}\",\"app_transaction_ref\":\"order_123\",\"operator_transaction_ref\":\"MP200618.1634.A34527\",\"transaction_ref\":\"18ac6335-2bdd-4b95-944e-ef029c49c5b5\",\"transaction_type\":\"PAYIN\",\"transaction_amount\":100,\"transaction_fees\":2,\"transaction_currency\":\"XAF\",\"transaction_operator\":\"CM_OM\",\"transaction_status\":\"SUCCESS\",\"transaction_reason\":\"Bic pen\",\"transaction_message\":\"Your transaction has been successfully completed\",\"customer_phone_number\":\"699009900\",\"signature\":\"d41d8cd98f00b204e9800998ecf8427e\"}";
		
		String json = payload; // Utilisez le payload reçu du formulaire

        Gson gson = new Gson();
        CoolpayResponse transaction = gson.fromJson(json, CoolpayResponse.class);

        // Affichez tous les attributs de l'objet transaction
        System.out.println("Application: " + transaction.getApplication());
        System.out.println("App Transaction Ref: " + transaction.getOperator_transaction_ref());
        System.out.println("Operator Transaction Ref: " + transaction.getApp_transaction_ref());
        System.out.println("Transaction Ref: " + transaction.getApp_transaction_ref());
        System.out.println("Transaction Type: " + transaction.getTransaction_type());
        System.out.println("Transaction Amount: " + transaction.getTransaction_amount());
        System.out.println("Transaction Fees: " + transaction.getTransaction_fees());
        System.out.println("Transaction Currency: " + transaction.getTransaction_currency());
        System.out.println("Transaction Operator: " + transaction.getTransaction_operator());
        System.out.println("Transaction Status: " + transaction.getTransaction_status());
        System.out.println("Transaction Reason: " + transaction.getTransaction_reason());
        System.out.println("Transaction Message: " + transaction.getTransaction_message());
        System.out.println("Customer Phone Number: " + transaction.getCustomer_phone_number());
        System.out.println("Signature: " + transaction.getSignature());

        return ResponseEntity.ok("Payment handled successfully.");

	}

}
