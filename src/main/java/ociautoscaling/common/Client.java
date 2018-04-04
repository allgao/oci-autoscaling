package ociautoscaling.common;

import com.oracle.bmc.Region;
import com.oracle.bmc.core.Compute;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.VirtualNetwork;
import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.loadbalancer.LoadBalancer;
import com.oracle.bmc.loadbalancer.LoadBalancerClient;

import java.io.IOException;

public class Client {

    public static Compute getCompute(String profile) throws NumberFormatException, IOException {
        Compute compute = new ComputeClient(Config.getAuthProvider(profile.toUpperCase()), Config.getClientConfig(profile));
        compute.setRegion(Region.fromRegionId(Config.getConfigFileReader(profile.toUpperCase()).get("region")));
        return compute;
    }

    public static LoadBalancer getLoadBalancer(String profile) throws NumberFormatException, IOException {
        LoadBalancer lb = new LoadBalancerClient(Config.getAuthProvider(profile.toUpperCase()), Config.getClientConfig(profile));
        lb.setRegion(Region.fromRegionId(Config.getConfigFileReader(profile.toUpperCase()).get("region")));
        return lb;
    }

    /**
     * Network service client.
     *
     * @param profile
     * @return
     * @throws IOException
     */
    public static VirtualNetwork getVirtualNetwork(String profile) throws IOException {
        VirtualNetwork vnService = new VirtualNetworkClient(Config.getAuthProvider(profile.toUpperCase()), Config.getClientConfig(profile));
        vnService.setRegion(Region.fromRegionId(Config.getConfigFileReader(profile.toUpperCase()).get("region")));
        return vnService;
    }

}
