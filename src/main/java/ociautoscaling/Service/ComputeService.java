package ociautoscaling.Service;

import com.oracle.bmc.core.Compute;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.model.InstanceSourceViaImageDetails;
import com.oracle.bmc.core.model.LaunchInstanceDetails;
import com.oracle.bmc.core.model.Subnet;
import com.oracle.bmc.core.requests.InstanceActionRequest;
import com.oracle.bmc.core.requests.LaunchInstanceRequest;
import com.oracle.bmc.core.requests.ListInstancesRequest;
import com.oracle.bmc.core.responses.LaunchInstanceResponse;
import com.oracle.bmc.core.responses.ListInstancesResponse;
import ociautoscaling.Model.SubnetAndInstanceCount;
import ociautoscaling.autoconfigure.SDKProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@EnableConfigurationProperties(SDKProperties.class)
public class ComputeService implements IService {
    private Compute compute;
    private SDKProperties sdkProperties;

    @Autowired
    public ComputeService(Compute compute, SDKProperties sdkProperties) {
        this.compute = compute;
        this.sdkProperties = sdkProperties;
    }

    public List<Instance> getAllInstances() {
        List<Instance> list;
        //todo put compartmentId into config file.
        ListInstancesRequest req = ListInstancesRequest.builder().compartmentId(sdkProperties.getCompartment()).build();
        ListInstancesResponse resp;
        resp = compute.listInstances(req);
        list = resp.getItems();
        return list;
    }

    public int countInstancesInSubnet(String groupName, Subnet sn) {
        int counter = 0;
        List<Instance> list;
        ListInstancesRequest.Builder builder = ListInstancesRequest.builder().availabilityDomain(sn.getAvailabilityDomain()).compartmentId("ocid1.compartment.oc1..aaaaaaaabfwhydtmr5gitadyrswhmm5rk72oodmqph6inzg5ttqbettud7pa");
        list = compute.listInstances(builder.build()).getItems();
        for (Instance i : list) {
            if (i.getFreeformTags().get("group").equals(groupName)) {
                counter += 1;
            }
        }
        return counter;
    }

    public List<Instance> getScalableInstances(String groupName, boolean scalein) {
        List<Instance> list = new ArrayList();
        ListInstancesRequest.Builder builder = ListInstancesRequest.builder().compartmentId(sdkProperties.getCompartment());
        if (scalein) {
            builder.lifecycleState(Instance.LifecycleState.Running);
        } else {
            builder.lifecycleState(Instance.LifecycleState.Stopped);
        }
        //todo put compartmentId into config file.
        ListInstancesRequest req = builder.build();
        ListInstancesResponse resp;
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

    public void scale(Instance i, String action) {
        int delay = 0;
        if (action.equals("stop")) {
            delay = 2;
        }
        InstanceActionRequest req = InstanceActionRequest.builder().action(action).instanceId(i.getId()).build();
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        compute.instanceAction(req);
                        System.out.println("Executing " + action + " instance...");
                    }
                }, delay * 60 * 1000);
    }

    public Instance getAnRegularInstance(String groupName) {
        List<Instance> list = getAllInstances();
        for (Instance i : list) {
            if (i.getFreeformTags().get("group").equals(groupName) && i.getFreeformTags().get("category").equals("regular"))
                return i;
        }
        return null;
    }

    public Instance createAuxiliaryInstance(Instance i, Subnet sn) {
        Instance result = null;
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ssh_authorized_keys", i.getMetadata().get("ssh_authorized_keys"));
        Map<String, String> tags = i.getFreeformTags();
        tags.put("category", "auxiliary");
        LaunchInstanceDetails details = LaunchInstanceDetails.builder().availabilityDomain(sn.getAvailabilityDomain()).compartmentId(i.getCompartmentId()).metadata(metadata).shape(i.getShape()).sourceDetails(
                InstanceSourceViaImageDetails.builder()
                        .imageId(i.getImageId())
                        .build()).freeformTags(tags).subnetId(sn.getId()).build();
        LaunchInstanceRequest req = LaunchInstanceRequest.builder().launchInstanceDetails(details).build();
        LaunchInstanceResponse resp = compute.launchInstance(req);
        if (null != resp) {
            result = resp.getInstance();
            //close server after creation
            InstanceActionRequest iaReq = InstanceActionRequest.builder().action("stop").instanceId(result.getId()).build();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            compute.instanceAction(iaReq);
                        }
                    }, 2 * 60 * 1000);

        }
        return result;
    }

    public List<SubnetAndInstanceCount> getOrderedSubnet(String groupName, List<Subnet> snList) {
        List<SubnetAndInstanceCount> list = new ArrayList<>();
        for (Subnet sn : snList) {
            list.add(new SubnetAndInstanceCount(sn, this.countInstancesInSubnet(groupName, sn)));
        }
        list.sort(new Comparator<SubnetAndInstanceCount>() {
            @Override
            public int compare(SubnetAndInstanceCount o1, SubnetAndInstanceCount o2) {
                if (o1.getCount() >= o2.getCount()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return list;
    }

}
