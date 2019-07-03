/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.si.cool.jconon.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;

import java.util.Arrays;
import java.util.List;

@Configuration
public class FlowsConfiguration extends WebSecurityConfigurerAdapter {
    @Value("${flows.resource}")
    private String baseUrl;

    @Value("${flows.authorize}")
    private String authorizeUrl;

    @Value("${flows.token}")
    private String tokenUrl;

    @Value("${flows.username}")
    private String username;
    @Value("${flows.password}")
    private String password;

    @Value("${flows.clientId}")
    private String clientId;
    @Value("${flows.clientSecret}")
    private String clientSecret;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
    }

    @Bean
    protected OAuth2ProtectedResourceDetails resource() {

        CustomResourceOwnerPasswordResourceDetails resource = new CustomResourceOwnerPasswordResourceDetails();

        List<String> scopes = Arrays.asList("read", "write");
        resource.setAccessTokenUri(tokenUrl);
        resource.setScope(scopes);
        resource.setClientId(clientId);
        resource.setClientSecret(clientSecret);
        resource.setUsername(username);
        resource.setPassword(password);

        return resource;
    }

    @Bean
    public OAuth2RestOperations restTemplate() {
        return new OAuth2RestTemplate(resource());
    }

    public class CustomOAuth2ClientContext extends DefaultOAuth2ClientContext {
        @Override
        public AccessTokenRequest getAccessTokenRequest() {
            return super.getAccessTokenRequest();
        }
    }

    public class CustomResourceOwnerPasswordResourceDetails extends ResourceOwnerPasswordResourceDetails {
        @Override
        public boolean isClientOnly() {
            return true;
        }
    }

}
