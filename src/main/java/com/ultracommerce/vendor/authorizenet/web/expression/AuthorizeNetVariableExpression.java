/*
 * #%L
 * UltraCommerce Authorize.net
 * %%
 * Copyright (C) 2009 - 2017 Ultra Commerce
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
package com.ultracommerce.vendor.authorizenet.web.expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ultracommerce.common.web.expression.UltraVariableExpression;
import com.ultracommerce.payment.service.gateway.AuthorizeNetConfiguration;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * <p>A Thymeleaf Variable Expression implementation for Authorize.net specific fields.</p>
 *
 * <p>For example, to generate the client-side token for the payment nonce retrieved by the Client-Side SDK:</p>
 * <pre><code>
 * <script th:inline="javascript">
 * ... CDATA ...
 *    authData.clientKey = [[${#authorizenet.getClientKey()}]];
 *    authData.apiLoginID = [[${#authorizenet.getApiLoginId()}]];
 * ...
 * </script>
 * </code></pre>
 *
 * @author Elbert Bautista (elbertbautista)
 */
@Service("ucAuthorizeNetVariableExpression")
public class AuthorizeNetVariableExpression implements UltraVariableExpression {

    protected static final Log LOG = LogFactory.getLog(AuthorizeNetVariableExpression.class);

    @Resource(name = "ucAuthorizeNetConfiguration")
    protected AuthorizeNetConfiguration configuration;

    @Override
    public String getName() {
        return "authorizenet";
    }

    public String getClientKey() {
        return configuration.getClientKey();
    }

    public String getApiLoginId() {
        return configuration.getLoginId();
    }

    public String getAcceptJsUrl() {
        return configuration.getAcceptJsUrl();
    }

    public String getGatewayMerchantId() {
        return configuration.getGatewayMerchantId();
    }

    public String getAppleMerchantId() {
        return configuration.getAppleMerchantId();
    }

}

