/*
 * #%L
 * UltraCommerce Authorize.net
 * %%
 * Copyright (C) 2009 - 2015 Ultra Commerce
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

import net.authorize.data.creditcard.CardType;

import com.ultracommerce.common.UltraEnumerationType;
import com.ultracommerce.common.payment.CreditCardType;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


public class AuthorizeNetCardType implements Serializable, UltraEnumerationType {
   
    private static final long serialVersionUID = 1L;

    private static final Map<String, AuthorizeNetCardType> TYPES = new LinkedHashMap<String, AuthorizeNetCardType>();
    
    public static final AuthorizeNetCardType MASTERCARD  = new AuthorizeNetCardType("MASTERCARD", "Master Card", CardType.MASTER_CARD);
    public static final AuthorizeNetCardType VISA  = new AuthorizeNetCardType("VISA", "Visa", CardType.VISA);
    public static final AuthorizeNetCardType AMEX  = new AuthorizeNetCardType("AMEX", "American Express", CardType.AMERICAN_EXPRESS);
    public static final AuthorizeNetCardType DINERSCLUB_CARTEBLANCHE  = new AuthorizeNetCardType("DINERSCLUB_CARTEBLANCHE", "Diner's Club / Carte Blanche", CardType.DINERS_CLUB);
    public static final AuthorizeNetCardType DISCOVER  = new AuthorizeNetCardType("DISCOVER", "Discover", CardType.DISCOVER);
    public static final AuthorizeNetCardType JCB  = new AuthorizeNetCardType("JCB", "JCB", CardType.JCB);
    public static final AuthorizeNetCardType ECHECK = new AuthorizeNetCardType("ECHECK", "eCheck", CardType.ECHECK);
    public static final AuthorizeNetCardType UNKNOWN = new AuthorizeNetCardType("UNKNOWN", "Unknown", CardType.UNKNOWN);

    public static AuthorizeNetCardType getInstance(final String type) {
        return TYPES.get(type);
    }
    
    public static AuthorizeNetCardType getInstanceFromAuthorizeNetType(final CardType authNetCardType) {
        for (String type : TYPES.keySet()) {
            if (TYPES.get(type).getAuthorizeNetCardType().equals(authNetCardType)) {
                return TYPES.get(type);
            }
        }
        return null;
    }
    
    public static AuthorizeNetCardType getInstanceFromUltraType(final CreditCardType ucCardType) {
        return getInstance(ucCardType.getType());
    }

    private String type;
    private String friendlyType;
    private CardType authCardType;

    public AuthorizeNetCardType() {
        //do nothing
    }

    public AuthorizeNetCardType(final String type, final String friendlyType, final CardType authCardType) {
        this.friendlyType = friendlyType;
        this.authCardType = authCardType;
        setType(type);
    }

    public String getType() {
        return type;
    }

    public String getFriendlyType() {
        return friendlyType;
    }
    
    public CardType getAuthorizeNetCardType() {
        return authCardType;
    }
    
    public CreditCardType getUltraCardType() {
        CreditCardType ucCardType = CreditCardType.getInstance(type);
        return ucCardType == null ? new CreditCardType(type, friendlyType) : ucCardType;
    }

    private void setType(final String type) {
        this.type = type;
        if (!TYPES.containsKey(type)){
            TYPES.put(type, this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((authCardType == null) ? 0 : authCardType.hashCode());
        result = prime * result + ((friendlyType == null) ? 0 : friendlyType.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) 
            return true;
        if (obj == null) 
            return false;
        if (getClass() != obj.getClass()) 
            return false;
        AuthorizeNetCardType other = (AuthorizeNetCardType) obj;
        if (authCardType != other.authCardType) 
            return false;
        if (friendlyType == null) {
            if (other.friendlyType != null) 
                return false;
        } else if (!friendlyType.equals(other.friendlyType)) 
            return false;
        if (type == null) {
            if (other.type != null) 
                return false;
        } else if (!type.equals(other.type)) 
            return false;
        
        return true;
    }

}

