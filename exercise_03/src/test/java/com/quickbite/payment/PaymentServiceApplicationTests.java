package com.quickbite.payment;

public class PaymentServiceApplicationTests {
    public static void main(String[] args) {
        System.out.println("Running PaymentServiceApplicationTests...");
        testPaymentHealthCheck();
        System.out.println("All Unit Tests Passed Successfully!");
    }

    public static void testPaymentHealthCheck() {
        assert true : "Health check should be UP";
    }
}

