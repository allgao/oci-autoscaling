package ociautoscaling.autoconfigure;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.core.VirtualNetwork;
import com.oracle.bmc.core.VirtualNetworkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(VirtualNetwork.class)
@EnableConfigurationProperties(SDKProperties.class)
public class VirtualNetworkAutoConfigure {
    private SimpleAuthenticationDetailsProvider provider;
    private SDKProperties properties;

    @Autowired
    public VirtualNetworkAutoConfigure(SimpleAuthenticationDetailsProvider provider, SDKProperties properties) {
        this.provider = provider;
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public VirtualNetwork virtualNetwork() {
        VirtualNetwork virtualNetwork = new VirtualNetworkClient(provider, ClientConfiguration.builder().connectionTimeoutMillis(properties.getConnectiontimeout()).readTimeoutMillis(properties.getReadtimeout()).build());
        virtualNetwork.setRegion(Region.fromRegionId(properties.getRegion()));
        return virtualNetwork;
    }
}
