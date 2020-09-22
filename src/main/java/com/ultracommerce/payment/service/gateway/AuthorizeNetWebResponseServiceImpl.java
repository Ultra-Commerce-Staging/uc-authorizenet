/*
 * #%L
 * UltraCommerce Authorize.net
 * %%
 * Copyright (C) 2009 - 2014 Ultra Commerce
 * %%
 * Licensed under the Ultra Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.ultracommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Ultra in which case
 * the Ultra End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.ultracommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Ultra Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package com.ultracommerce.payment.service.gateway;

import com.ultracommerce.common.money.Money;
import com.ultracommerce.common.payment.PaymentTransactionType;
import com.ultracommerce.common.payment.PaymentType;
import com.ultracommerce.common.payment.dto.PaymentResponseDTO;
import com.ultracommerce.common.payment.service.AbstractPaymentGatewayWebResponseService;
import com.ultracommerce.common.payment.service.PaymentGatewayWebResponsePrintService;
import com.ultracommerce.common.payment.service.PaymentGatewayWebResponseService;
import com.ultracommerce.common.vendor.service.exception.PaymentException;
import com.ultracommerce.vendor.authorizenet.service.payment.AuthorizeNetCheckoutService;
import com.ultracommerce.vendor.authorizenet.service.payment.AuthorizeNetGatewayType;
import com.ultracommerce.vendor.authorizenet.service.payment.type.MessageConstants;
import org.springframework.stereotype.Service;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import net.authorize.AuthNetField;

/**
 * @author Chad Harchar (charchar)
 */
@Deprecated
@Service("ucAuthorizeNetWebResponseService")
public class AuthorizeNetWebResponseServiceImpl extends AbstractPaymentGatewayWebResponseService implements PaymentGatewayWebResponseService {

    @Resource(name = "ucAuthorizeNetConfiguration")
    protected AuthorizeNetConfiguration configuration;
    
    @Resource(name = "ucPaymentGatewayWebResponsePrintService")
    protected PaymentGatewayWebResponsePrintService webResponsePrintService;
    
    @Resource(name = "ucAuthorizeNetCheckoutService")
    protected AuthorizeNetCheckoutService authorizeNetCheckoutService;

    @Override
    public PaymentResponseDTO translateWebResponse(HttpServletRequest request) throws PaymentException {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.CREDIT_CARD,
                AuthorizeNetGatewayType.AUTHORIZENET)
                .rawResponse(webResponsePrintService.printRequest(request));

        Map<String,String[]> paramMap = request.getParameterMap();
        for (String key : paramMap.keySet())  {
            responseDTO.responseMap(key, paramMap.get(key)[0]);
        }

        Map<String, String> params = responseDTO.getResponseMap();

        Money amount = Money.ZERO;
        if (responseDTO.getResponseMap().containsKey(AuthNetField.X_AMOUNT.getFieldName())) {
            String amt = responseDTO.getResponseMap().get(AuthNetField.X_AMOUNT.getFieldName());
            amount = new Money(amt);
        }

        boolean approved = false;
        if (params.get(AuthNetField.X_RESPONSE_CODE.getFieldName()).equals("1")) {
            approved = true;
        }

        PaymentTransactionType type = PaymentTransactionType.AUTHORIZE_AND_CAPTURE;
        if (!configuration.isPerformAuthorizeAndCapture()) {
            type = PaymentTransactionType.AUTHORIZE;
        }

        // Validate this is a real request from Authorize.net
        String customerId = responseDTO.getResponseMap().get(MessageConstants.UC_CID);
        String orderId = responseDTO.getResponseMap().get(MessageConstants.UC_OID);
        String tps = responseDTO.getResponseMap().get(MessageConstants.UC_TPS);
        responseDTO.valid(authorizeNetCheckoutService.verifyTamperProofSeal(customerId, orderId, tps));

        responseDTO.successful(approved)
        .amount(amount)
        .paymentTransactionType(type)
        .orderId(params.get(MessageConstants.UC_OID))
        .customer()
            .firstName(params.get(AuthNetField.X_FIRST_NAME.getFieldName()))
            .lastName(params.get(AuthNetField.X_LAST_NAME.getFieldName()))
            .customerId(params.get(AuthNetField.X_CUST_ID.getFieldName()))
            .done()
        .billTo()
            .addressFirstName(params.get(AuthNetField.X_FIRST_NAME.getFieldName()))
            .addressLastName(params.get(AuthNetField.X_LAST_NAME.getFieldName()))
            .addressLine1(params.get(AuthNetField.X_ADDRESS.getFieldName()))
            .addressCityLocality(params.get(AuthNetField.X_CITY.getFieldName()))
            .addressStateRegion(params.get(AuthNetField.X_STATE.getFieldName()))
            .addressPostalCode(params.get(AuthNetField.X_ZIP.getFieldName()))
            .addressCountryCode(params.get(AuthNetField.X_COUNTRY.getFieldName()))
            .addressPhone(params.get(AuthNetField.X_PHONE.getFieldName()))
            .done()
        .creditCard()
            .creditCardLastFour(params.get(AuthNetField.X_ACCOUNT_NUMBER.getFieldName()))
            .creditCardType(params.get(AuthNetField.X_CARD_TYPE.getFieldName()));

        return responseDTO;
    }

}
