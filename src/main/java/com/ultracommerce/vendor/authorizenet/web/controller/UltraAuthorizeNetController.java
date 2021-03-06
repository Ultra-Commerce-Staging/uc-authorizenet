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

package com.ultracommerce.vendor.authorizenet.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ultracommerce.common.payment.dto.PaymentRequestDTO;
import com.ultracommerce.common.payment.dto.PaymentResponseDTO;
import com.ultracommerce.common.payment.service.PaymentGatewayConfiguration;
import com.ultracommerce.common.payment.service.PaymentGatewayTransactionService;
import com.ultracommerce.common.payment.service.PaymentGatewayWebResponsePrintService;
import com.ultracommerce.common.payment.service.PaymentGatewayWebResponseService;
import com.ultracommerce.common.vendor.service.exception.PaymentException;
import com.ultracommerce.common.web.payment.controller.PaymentGatewayAbstractController;
import com.ultracommerce.payment.service.gateway.AuthorizeNetConfiguration;
import com.ultracommerce.vendor.authorizenet.service.payment.AuthorizeNetCheckoutService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.authorize.sim.Result;

/**
 * @author Chad Harchar (charchar)
 * @author Elbert Bautista (elbertbautista)
 * @deprecated - Transparent Redirect is no longer used in favor of Accept.js integration
 */
@Deprecated
@Controller("ucAuthorizeNetController")
@RequestMapping("/" + UltraAuthorizeNetController.GATEWAY_CONTEXT_KEY)
public class UltraAuthorizeNetController extends PaymentGatewayAbstractController {

    protected static final Log LOG = LogFactory.getLog(UltraAuthorizeNetController.class);
    protected static final String GATEWAY_CONTEXT_KEY = "authorizenet";

    @Resource(name = "ucAuthorizeNetWebResponseService")
    protected PaymentGatewayWebResponseService paymentGatewayWebResponseService;

    @Resource(name = "ucAuthorizeNetCheckoutService")
    protected AuthorizeNetCheckoutService authorizeNetCheckoutService;

    @Resource(name = "ucAuthorizeNetConfiguration")
    protected AuthorizeNetConfiguration configuration;

    @Resource(name = "ucPaymentGatewayWebResponsePrintService")
    protected PaymentGatewayWebResponsePrintService webResponsePrintService;
    
    @Resource(name = "ucAuthorizeNetTransactionService")
    protected PaymentGatewayTransactionService transactionService;

    @Override
    public String getGatewayContextKey() {
        return GATEWAY_CONTEXT_KEY;
    }

    @Override
    public void handleProcessingException(Exception e, RedirectAttributes redirectAttributes) throws PaymentException {
        // Don't do anything here because there isn't enough information in the given parameters to actually
        // void the payment that we need to void the payment
        throw new PaymentException(e);
    }

    @Override
    public void handleUnsuccessfulTransaction(Model model, RedirectAttributes redirectAttributes,
            PaymentResponseDTO responseDTO) throws PaymentException {
        LOG.trace("A Processing Exception Occurred for " + GATEWAY_CONTEXT_KEY + ". Adding Error to Redirect Attributes.");
        redirectAttributes.addAttribute(PAYMENT_PROCESSING_ERROR, getProcessingErrorMessage());
    }

    @Override
    public PaymentGatewayWebResponseService getWebResponseService() {
        return paymentGatewayWebResponseService;
    }

    @Override
    public PaymentGatewayConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String errorEndpoint(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes,
            Map<String, String> pathVars) throws PaymentException {
        redirectAttributes.addAttribute(PAYMENT_PROCESSING_ERROR, getProcessingErrorMessage());
        for (String key : (Set<String>) request.getParameterMap().keySet()) {
            redirectAttributes.addAttribute(key, request.getParameter(key));
        }
        return getOrderReviewRedirect();
    }

    @Override
    @RequestMapping(value = "/return", method = RequestMethod.GET)
    public String returnEndpoint(Model model, HttpServletRequest request,
            final RedirectAttributes redirectAttributes,
            @PathVariable Map<String, String> pathVars) throws PaymentException {
        PaymentResponseDTO responseDTO = getWebResponseService().translateWebResponse(request);
        String orderNumber = lookupOrderNumberFromOrderId(responseDTO);
        return getConfirmationViewRedirect(orderNumber);
    }

    /**
     * <p>
     * Invoked by Authorize.net after taking the user's credit card. This should process the payment and add it to the order
     * as well as check out the order if configured.
     * 
     * <p>
     * At this point in the lifecyle of taking payment, the card has already been charged. If there is a problem checking
     * out the order, we need to communicate that explicitly up to Authorize.net via a VOID transaction. Otherwise, the user
     * will be left with a non-submitted Order but their card will still be charged.
     * 
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public @ResponseBody String process(HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            Model model) throws PaymentException {
        LOG.debug("Authorize URL request - " + request.getRequestURL().toString());
        LOG.debug("Authorize Request Parameter Map (params: [" + requestParamToString(request) + "])");
        Result result = Result.createResult(configuration.getLoginId(), configuration.getMd5Key(), request.getParameterMap());
        
        boolean error = false;
        String returnUrl = "";
        try {
            // Process the payment on the Ultra order side
            returnUrl = super.process(model, request, redirectAttributes);
        } catch (Exception e) {
            error = true;
        }
        
        // This is a hacky way to do this but has to be done because the handleProcessingException does not give enough
        // information to actually be useful in doing what I need. Ideally this logic would go in handleProcessingException
        // TODO: It is possible that this has already been voided if this was an exception in the checkout workflow itself
        if (error || getErrorViewRedirect().equals(returnUrl)) {
            PaymentResponseDTO responseDTO = getWebResponseService().translateWebResponse(request);
            PaymentRequestDTO voidRequest = responseToVoidRequest(responseDTO);
            transactionService.voidPayment(voidRequest);
            
            return authorizeNetCheckoutService.buildRelayResponse(configuration.getErrorUrl(), result);
        }

        return authorizeNetCheckoutService.buildRelayResponse(configuration.getConfirmUrl(), result);
    }
    
    /**
     * Builds a request with enough information to void the given response from the gateway
     * 
     * @param response
     * @return
     */
    protected PaymentRequestDTO responseToVoidRequest(PaymentResponseDTO response) {
        PaymentRequestDTO request = new PaymentRequestDTO()
            .transactionTotal(response.getAmount().toString());
        
        for (Entry<String, String> field : response.getResponseMap().entrySet()) {
            request.additionalField(field.getKey(), field.getValue());
        }
        
        return request;
    }

    protected String requestParamToString(HttpServletRequest request) {
        StringBuffer requestMap = new StringBuffer();
        for (String key : (Set<String>) request.getParameterMap().keySet()) {
            requestMap.append(key + ": " + request.getParameter(key) + ", ");
        }
        return requestMap.toString();
    }

}
