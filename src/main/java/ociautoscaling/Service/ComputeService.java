package ociautoscaling.Service;

import com.oracle.bmc.core.Compute;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.model.Vnic;
import com.oracle.bmc.core.model.VnicAttachment;
import com.oracle.bmc.core.requests.GetVnicRequest;
import com.oracle.bmc.core.requests.InstanceActionRequest;
import com.oracle.bmc.core.requests.ListInstancesRequest;
import com.oracle.bmc.core.requests.ListVnicAttachmentsRequest;
import com.oracle.bmc.core.responses.InstanceActionResponse;
import com.oracle.bmc.core.responses.ListInstancesResponse;
import ociautoscaling.common.Client;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ComputeService implements IService {

    public List<Instance> getAllInstances() {
        List<Instance> list = new ArrayList();
        //todo put compartmentId into config file.
        ListInstancesRequest req = ListInstancesRequest.builder().compartmentId("ocid1.compartment.oc1..aaaaaaaabfwhydtmr5gitadyrswhmm5rk72oodmqph6inzg5ttqbettud7pa").build();
        ListInstancesResponse resp;
        try {
            Compute compute = Client.getCompute("TEST");
            resp = compute.listInstances(req);
            list = resp.getItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get private ip addresses on VM instance.
     *
     * @param instanceId
     * @return
     */
    public String getPrivateIpByInstanceId(String instanceId) {
        String ip = "";
        List<String> ips = new ArrayList<>();
        try {
            List<Vnic> atchs = this.getVnicByInstanceId(instanceId, "ocid1.compartment.oc1..aaaaaaaabfwhydtmr5gitadyrswhmm5rk72oodmqph6inzg5ttqbettud7pa");
            for (Vnic v : atchs) {
                ips.add(v.getPrivateIp());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ips.size() > 0) {
            ip = ips.get(0);
        }
        return ip;
    }

    /**
     * Get VNICs on VM instance.
     *
     * @param instanceId
     * @param compartmentId
     * @return
     */
    public List<Vnic> getVnicByInstanceId(String instanceId, String compartmentId) {
        List<Vnic> vnics = new ArrayList<>();
        try {
            List<VnicAttachment> atchs = Client.getCompute("TEST").listVnicAttachments(ListVnicAttachmentsRequest.builder().compartmentId(compartmentId).instanceId(instanceId).build()).getItems();
            for (VnicAttachment va : atchs) {
                VnicAttachment.LifecycleState state = va.getLifecycleState();
                if (state.equals(VnicAttachment.LifecycleState.Attached)
                        || state.equals(VnicAttachment.LifecycleState.Attaching)) {
                    String vid = va.getVnicId();
                    Vnic v = Client.getVirtualNetwork("TEST").getVnic(GetVnicRequest.builder().vnicId(vid).build()).getVnic();
                    vnics.add(v);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vnics;
    }

    public List<Instance> getScalableInstances(String groupName, boolean scalein) {
        List<Instance> list = new ArrayList();
        ListInstancesRequest.Builder builder = ListInstancesRequest.builder().compartmentId("ocid1.compartment.oc1..aaaaaaaabfwhydtmr5gitadyrswhmm5rk72oodmqph6inzg5ttqbettud7pa");
        if (scalein) {
            builder.lifecycleState(Instance.LifecycleState.Running);
        } else {
            builder.lifecycleState(Instance.LifecycleState.Stopped);
        }
        //todo put compartmentId into config file.
        ListInstancesRequest req = builder.build();
        ListInstancesResponse resp;
        try {
            Compute compute = Client.getCompute("TEST");
            resp = compute.listInstances(req);
            for (Instance i : resp.getItems()) {
                if (i.getFreeformTags().get("group") != null && i.getFreeformTags().get("group").equals(groupName)) {
                    if (i.getFreeformTags().get("category") != null && i.getFreeformTags().get("category").equals("auxiliary")) {
                        list.add(i);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void scale(Instance i, String action) {
        int delay = 0;
        if (action.equals("stop")) {
            delay = 2;
        }
        try {
            Compute compute = Client.getCompute("TEST");
            InstanceActionRequest req = InstanceActionRequest.builder().action(action).instanceId(i.getId()).build();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            compute.instanceAction(req);
                            System.out.println("Executing " + action + " instance...");
                        }
                    }, delay * 60 * 1000);
            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
