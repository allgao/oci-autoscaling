package ociautoscaling.Service;

import com.oracle.bmc.core.Compute;
import com.oracle.bmc.core.model.*;
import com.oracle.bmc.core.requests.*;
import com.oracle.bmc.core.responses.InstanceActionResponse;
import com.oracle.bmc.core.responses.LaunchInstanceResponse;
import com.oracle.bmc.core.responses.ListInstancesResponse;
import ociautoscaling.common.Client;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComputeService implements IService {

    public List<Instance> getAllInstances() throws Exception {
        List<Instance> list = new ArrayList();
        //todo put compartmentId into config file.
        ListInstancesRequest req = ListInstancesRequest.builder().compartmentId("ocid1.compartment.oc1..aaaaaaaabfwhydtmr5gitadyrswhmm5rk72oodmqph6inzg5ttqbettud7pa").build();
        ListInstancesResponse resp;
        Compute compute = Client.getCompute("TEST");
        resp = compute.listInstances(req);
        list = resp.getItems();
        return list;
    }

    /**
     * Get private ip addresses on VM instance.
     *
     * @param instanceId
     * @return
     */
    public String getPrivateIpByInstanceId(String instanceId) throws Exception {
        String ip = "";
        List<String> ips = new ArrayList<>();
        List<Vnic> atchs = this.getVnicByInstanceId(instanceId, "ocid1.compartment.oc1..aaaaaaaabfwhydtmr5gitadyrswhmm5rk72oodmqph6inzg5ttqbettud7pa");
        for (Vnic v : atchs) {
            ips.add(v.getPrivateIp());
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
    public List<Vnic> getVnicByInstanceId(String instanceId, String compartmentId) throws Exception {
        List<Vnic> vnics = new ArrayList<>();
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
        return vnics;
    }

    public List<Instance> getScalableInstances(String groupName, boolean scalein) throws Exception {
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
        Compute compute = Client.getCompute("TEST");
        resp = compute.listInstances(req);
        for (Instance i : resp.getItems()) {
            if (i.getFreeformTags().get("group") != null && i.getFreeformTags().get("group").equals(groupName)) {
                if (i.getFreeformTags().get("category") != null && i.getFreeformTags().get("category").equals("auxiliary")) {
                    list.add(i);
                }
            }
        }
        return list;
    }

    public void scale(Instance i, String action) throws Exception {
        int delay = 0;
        if (action.equals("stop")) {
            delay = 2;
        }
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

    }

    public Instance getAnRegularInstance(String groupName) throws Exception {
        List<Instance> list = getAllInstances();
        for (Instance i : list) {
            if (i.getFreeformTags().get("group").equals(groupName) && i.getFreeformTags().get("category").equals("regular"))
                return i;
        }
        return null;
    }

    public Instance createAuxiliaryInstance(Instance i) throws Exception {
        Instance result = null;
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ssh_authorized_keys", i.getMetadata().get("ssh_authorized_keys"));
        Map<String, String> tags = i.getFreeformTags();
        tags.put("category", "auxiliary");
        List<Vnic> atchs = this.getVnicByInstanceId(i.getId(), i.getCompartmentId());
        String subnetId = atchs.get(0).getSubnetId();
        if (subnetId == null) {
            return null;
        }
        LaunchInstanceDetails details = LaunchInstanceDetails.builder().availabilityDomain(i.getAvailabilityDomain()).compartmentId(i.getCompartmentId()).metadata(metadata).shape(i.getShape()).sourceDetails(
                InstanceSourceViaImageDetails.builder()
                        .imageId(i.getImageId())
                        .build()).freeformTags(tags).subnetId(subnetId).build();
        LaunchInstanceRequest req = LaunchInstanceRequest.builder().launchInstanceDetails(details).build();
        LaunchInstanceResponse resp = Client.getCompute("TEST").launchInstance(req);
        if (null != resp) {
            result = resp.getInstance();
        }
        return result;
    }
}
