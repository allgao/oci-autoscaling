package ociautoscaling.Service;

import com.oracle.bmc.core.Compute;
import com.oracle.bmc.core.VirtualNetwork;
import com.oracle.bmc.core.model.*;
import com.oracle.bmc.core.requests.*;
import com.oracle.bmc.core.responses.ListSubnetsResponse;
import ociautoscaling.autoconfigure.SDKProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NetworkService implements IService {
    private Compute compute;
    private VirtualNetwork virtualNetwork;
    private SDKProperties properties;

    @Autowired
    public NetworkService(Compute compute, VirtualNetwork virtualNetwork, SDKProperties properties) {
        this.compute = compute;
        this.virtualNetwork = virtualNetwork;
        this.properties = properties;
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
        List<Vnic> atchs = this.getVnicByInstanceId(instanceId, properties.getCompartment());
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
    public List<Vnic> getVnicByInstanceId(String instanceId, String compartmentId) {
        List<Vnic> vnics = new ArrayList<>();
        List<VnicAttachment> atchs = compute.listVnicAttachments(ListVnicAttachmentsRequest.builder().compartmentId(compartmentId).instanceId(instanceId).build()).getItems();
        for (VnicAttachment va : atchs) {
            VnicAttachment.LifecycleState state = va.getLifecycleState();
            if (state.equals(VnicAttachment.LifecycleState.Attached)
                    || state.equals(VnicAttachment.LifecycleState.Attaching)) {
                String vid = va.getVnicId();
                Vnic v = virtualNetwork.getVnic(GetVnicRequest.builder().vnicId(vid).build()).getVnic();
                vnics.add(v);
            }
        }
        return vnics;
    }

    public List<Subnet> getSubnetsInVcn(String vcnId) {
        ListSubnetsResponse res = virtualNetwork.listSubnets(
                ListSubnetsRequest.builder().compartmentId(properties.getCompartment()).vcnId(vcnId).build());
        return res.getItems();
    }

    public Vcn getVncByInstance(Instance i) {
        List<Vnic> atchs = this.getVnicByInstanceId(i.getId(), i.getCompartmentId());
        String subnetId = atchs.get(0).getSubnetId();
        Subnet instanceSubnet = virtualNetwork.getSubnet(GetSubnetRequest.builder().subnetId(subnetId).build()).getSubnet();
        Vcn vcn = virtualNetwork.getVcn(GetVcnRequest.builder().vcnId(instanceSubnet.getVcnId()).build()).getVcn();
        return vcn;
    }

}
