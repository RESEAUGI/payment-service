package com.mo.interfaces;

import com.mo.entities.MyProduct;
import com.mo.stripe.LinkPay;

public interface PaymentInterface {
	public LinkPay payIn(MyProduct product);
	public int  operator();
	
}
