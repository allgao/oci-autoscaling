package ociautoscaling.controller;

import com.oracle.bmc.core.model.Instance;
import ociautoscaling.Model.GroupInfo;
import ociautoscaling.Service.ComputeService;
import ociautoscaling.Service.LoadBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ScaleController {
    @Autowired
    private ComputeService computeService;
    @Autowired
    private LoadBalanceService lbService;

    @GetMapping("/getGroupInfo")
    @ResponseBody
    public List<GroupInfo> getGroupInfo() {
        Map<String, GroupInfo> gMap = new HashMap<>();
        List<Instance> iList = computeService.getAllInstances();
        for (Instance i : iList) {
            String gName = i.getFreeformTags().get("group");
            if (gName != null) {
                GroupInfo gInfo;
                if (gMap.get(gName) == null) {
                    gInfo = new GroupInfo();
                    gInfo.setGroupName(gName);

                } else {
                    gInfo = gMap.get(gName);
                }
                switch (i.getFreeformTags().get("category")) {
                    case "regular": {
                        gInfo.addRegular();
                        break;
                    }
                    case "auxiliary": {
                        switch (i.getLifecycleState().toString()) {
                            case "Running":
                                gInfo.addAuxiliaryRunning();
                                break;
                            case "Staring":
                                gInfo.addAuxiliaryStarting();
                                break;
                            case "Stopping":
                                gInfo.addAuxiliaryStopping();
                                break;
                            case "Stopped":
                                gInfo.addAuxiliaryStopped();
                                break;
                        }
                    }
                }
                gMap.put(gName, gInfo);
            }
        }
        List<GroupInfo> list = new ArrayList<>(gMap.values());
        return list;
    }

    @GetMapping("/scale")
    @ResponseBody
    public int scale(@RequestParam(value = "groupName") String groupName, @RequestParam(value = "num") int num) {
        if (num == 0) {
            return 0;
        }
        int succ = 0;
        boolean scalein = false;
        String action = "start";
        if (num < 0) {
            scalein = true;
            action = "stop";
        }
        List<Instance> scalableList = computeService.getScalableInstances(groupName, scalein);
        if (scalableList != null && scalableList.size() > 0) {
            for (Instance i : scalableList) {
                String loadBalancerSucc = "";
                String lbId = i.getFreeformTags().get("loadbalancer");
                String backendSet = i.getFreeformTags().get("backendset");
                //todo how to get ip?
                String ip = "";
                ip = computeService.getPrivateIpByInstanceId(i.getId());
                //todo shoud move port to configuration file for versatility
                int port = 8080;
                String backendName = ip + ":" + String.valueOf(port);
                if (lbId == null || backendSet == null || ip.equals("")) continue;
                //scalein should remove backend from lb first.
                if (scalein) {
                    loadBalancerSucc = lbService.drainBackend(lbId, backendSet, backendName);
                    if (!loadBalancerSucc.equals("")) {
                        //this method will execute after 2 minutes.
                        lbService.removeBackendFromBackendSet(lbId, backendSet, backendName);
                    } else {
                        continue;
                    }
                }
                //operate compute node.
                computeService.scale(i, action);
                //scaleout should startup compute node first,then add backend to lb;
                if (!scalein) {
                    loadBalancerSucc = lbService.addBackendToBackendSet(lbId, backendSet, ip, port);
                }
                if (!loadBalancerSucc.equals("")) {
                    succ += 1;
                    if (succ >= Math.abs(num)) {
                        break;
                    }
                }
            }
        }
        return succ;
    }
}
