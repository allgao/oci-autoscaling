package ociautoscaling.Service;

import com.oracle.bmc.loadbalancer.model.CreateBackendDetails;
import com.oracle.bmc.loadbalancer.model.UpdateBackendDetails;
import com.oracle.bmc.loadbalancer.requests.CreateBackendRequest;
import com.oracle.bmc.loadbalancer.requests.DeleteBackendRequest;
import com.oracle.bmc.loadbalancer.requests.UpdateBackendRequest;
import ociautoscaling.common.Client;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LoadBalanceService implements IService {

    /**
     * Add backend server to backendset.
     *
     * @param lbId
     * @param backendSetName
     * @param ip
     * @param port
     * @return
     */
    public String addBackendToBackendSet(String lbId, String backendSetName, String ip, int port) throws IOException {
        String workReqId = "";
        CreateBackendDetails cbd = CreateBackendDetails.builder().ipAddress(ip).port(port).build();
        CreateBackendRequest req = CreateBackendRequest.builder().loadBalancerId(lbId).backendSetName(backendSetName).createBackendDetails(cbd).build();
        workReqId = Client.getLoadBalancer("TEST").createBackend(req).getOpcWorkRequestId();
        return workReqId;
    }

    /**
     * Delete backend server from backendset.
     *
     * @param backendSetName
     * @param lbId
     * @param backendName
     * @return
     */
    public void removeBackendFromBackendSet(String lbId, String backendSetName, String backendName) {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            DeleteBackendRequest req = DeleteBackendRequest.builder().loadBalancerId(lbId).backendSetName(backendSetName).backendName(backendName).build();
                            Client.getLoadBalancer("TEST").deleteBackend(req).getOpcWorkRequestId();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println("Removing backend " + backendName + " from LoadBalancer BackendSet...");
                    }
                },
                2 * 60 * 1000
        );
    }

    public String drainBackend(String lbId, String backendSetName, String backendName) throws IOException {
        String workReqId = "";
        UpdateBackendDetails details = UpdateBackendDetails.builder().weight(1).offline(true).backup(false).drain(true).build();
        UpdateBackendRequest req = UpdateBackendRequest.builder().loadBalancerId(lbId).backendSetName(backendSetName).backendName(backendName).updateBackendDetails(details).build();
        workReqId = Client.getLoadBalancer("TEST").updateBackend(req).getOpcWorkRequestId();

        return workReqId;
    }
}
