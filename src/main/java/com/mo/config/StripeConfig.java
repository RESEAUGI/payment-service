package com.mo.config;

import org.springframework.stereotype.Service;

import com.stripe.Stripe;

@Service
public class StripeConfig {

    public StripeConfig() {
        Stripe.apiKey = "sk_test_51QCJZPRvLKDuwJKqzQf78cuvpPVkh3lMqmhX0cIy7CtmmK4tg3hNCumFbtp3Cxh9r3uNyRCQjgIJhSpUOp7xtnmM008YeLKiGh";
    }
}
