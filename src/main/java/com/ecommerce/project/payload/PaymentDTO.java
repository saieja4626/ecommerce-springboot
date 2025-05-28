package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long paymentId;
    private String paymentMethod;
    private String pgStatus;
    private String pgResponseMessage;
    private String pgName;
    private String pgPaymentId;
}
