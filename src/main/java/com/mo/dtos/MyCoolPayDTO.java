package com.mo.dtos;

import java.util.Date;
import java.util.Map;
import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class MyCoolPayDTO {
    private Long transaction_amount;
    private String transaction_currency;
    private String transaction_reason;
    private String app_transaction_ref;
    private String customer_phone_number;
    private String customer_name;
    private String customer_email;
    private String customer_lang;

}
