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
package com.ultracommerce.vendor.authorizenet.service.payment.type;

import net.authorize.AuthNetField;

/**
 * @author Chad Harchar (charchar)
 */
public class MessageConstants {

    public static final String UC_CID = "uc_cid";
    public static final String UC_OID = "uc_oid";
    public static final String UC_TPS = "uc_tps";
    public static final String UC_SAVE_CARD = "uc_save_card";
    public static final String AUTHORIZENET_SERVER_URL = "authorizenet_server_url";
    public static final String REQ_AMOUNT = "req_amount";
    public static final String TRANSACTION_TIME = "transaction_time";
    
    /**
     * Used to save and pay with saved customer data
     */
    public static final String CUSTOMER_PROFILE_ID = AuthNetField.ELEMENT_CUSTOMER_PROFILE_ID.getFieldName();
    
    /**
     * Used to save and pay with customer data
     */
    public static final String PAYMENT_PROFILE_ID = AuthNetField.ELEMENT_CUSTOMER_PAYMENT_PROFILE_ID.getFieldName();
    
    /**
     * The transaction ID for the payment
     */
    public static final String TRANSACTION_ID = AuthNetField.X_TRANS_ID.getFieldName();
    
}
