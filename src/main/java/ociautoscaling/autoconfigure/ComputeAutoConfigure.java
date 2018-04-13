package ociautoscaling.autoconfigure;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.core.Compute;
import com.oracle.bmc.core.ComputeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Compute.class)
@EnableConfigurationProperties(SDKProperties.class)
public class ComputeAutoConfigure {
    private SimpleAuthenticationDetailsProvider provider;
    private SDKProperties sdkProperties;

    @Autowired
    public ComputeAutoConfigure(SimpleAuthenticationDetailsProvider provider, SDKProperties sdkProperties) {
        this.provider = provider;
        this.sdkProperties = sdkProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public Compute compute() {
        Compute compute = new ComputeClient(provider, ClientConfiguration.builder().connectionTimeoutMillis(sdkProperties.getConnectiontimeout())
                .readTimeoutMillis(sdkProperties.getReadtimeout()).build());
        compute.setRegion(Region.fromRegionId(sdkProperties.getRegion()));
        return compute;
    }
}
