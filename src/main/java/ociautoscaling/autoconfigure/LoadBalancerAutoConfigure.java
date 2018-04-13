package ociautoscaling.autoconfigure;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.loadbalancer.LoadBalancer;
import com.oracle.bmc.loadbalancer.LoadBalancerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(LoadBalancer.class)
@EnableConfigurationProperties(SDKProperties.class)
public class LoadBalancerAutoConfigure {
    private SimpleAuthenticationDetailsProvider provider;
    private SDKProperties sdkProperties;

    @Autowired
    public LoadBalancerAutoConfigure(SimpleAuthenticationDetailsProvider provider, SDKProperties sdkProperties) {
        this.provider = provider;
        this.sdkProperties = sdkProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadBalancer loadBalancer() {
        LoadBalancer loadBalancer = new LoadBalancerClient(provider, ClientConfiguration.builder().connectionTimeoutMillis(sdkProperties.getConnectiontimeout())
                .readTimeoutMillis(sdkProperties.getReadtimeout()).build());
        loadBalancer.setRegion(Region.fromRegionId(sdkProperties.getRegion()));
        return loadBalancer;
    }
}
