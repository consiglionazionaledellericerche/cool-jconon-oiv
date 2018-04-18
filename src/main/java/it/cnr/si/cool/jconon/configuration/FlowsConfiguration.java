package it.cnr.si.cool.jconon.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;

import java.util.Arrays;
import java.util.List;

@Configuration
public class FlowsConfiguration extends WebSecurityConfigurerAdapter{
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

}
