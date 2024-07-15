package com.mo.stripe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;





import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.model.Price;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;



//@RestController
//@RequestMapping("/api")
public class StipeController {
	
	
	 // Configurez votre clé secrète Stripe ici
    private static final String STRIPE_SECRET_KEY = "sk_test_51PVJciERlR7Uy2xt1I68IsVZWLzrQJkYxJwVhf8ie3cjFZ3Q7Pi9E68luHyBY7zA4BwmCpZgoomnJ5TngoybPUC400GIRU8bD0";

    static {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

//    @PostMapping("/test-stripe")
    public String testStripeEndpoint(@RequestBody String payload) {
        System.out.println("Got payload: " + payload);
        return payload;
    }

 
    
    @PostMapping("/links_pay")
    public LinkPay createPaymentLink() {
    	Stripe.apiKey = "sk_test_51PVJciERlR7Uy2xt1I68IsVZWLzrQJkYxJwVhf8ie3cjFZ3Q7Pi9E68luHyBY7zA4BwmCpZgoomnJ5TngoybPUC400GIRU8bD0";
        String YOUR_DOMAIN = "http://localhost:3000";
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(YOUR_DOMAIN + "/success.html")
                .setCancelUrl(YOUR_DOMAIN + "/cancel.html")
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(1000L)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Reservation de voyage")
                                                                .setDescription("Vous allez éffectué un paiement pour une réservation de voyage")
                                                                .build())
                                                .build())
                                .build())
                .build();

        try {
            Session session = Session.create(params);
            String url = session.getUrl();
            
            LinkPay linkPay = new LinkPay(url);
            System.out.print(session.getUrl());
            return linkPay;
        } catch (StripeException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    
    
    
    
    

    @PostMapping("/createPrice")
    public String createPriceEndpoint() {
        try {
            ProductCreateParams productParams =
                ProductCreateParams.builder()
                    .setName("Starter Subscription")
                    .setDescription("$12/Month subscription")
                    .build();
            Product product = Product.create(productParams);
            System.out.println("Success! Here is your starter subscription product id: " + product.getId());

            PriceCreateParams params =
                PriceCreateParams
                    .builder()
                    .setProduct(product.getId())
                    .setCurrency("usd")
                    .setUnitAmount(1200L)
                    .setRecurring(
                        PriceCreateParams.Recurring
                            .builder()
                            .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                            .build())
                    .build();
            Price price = Price.create(params);
            System.out.println("Success! Here is your starter subscription price id: " + price.getId());

            return "Price created successfully.";
        } catch (StripeException e) {
            e.printStackTrace();
            return "Error creating price: " + e.getMessage();
        }
    }
    
    
}

