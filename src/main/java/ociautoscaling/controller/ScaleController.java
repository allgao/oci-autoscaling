package ociautoscaling.controller;

import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.model.Subnet;
import com.oracle.bmc.core.model.Vcn;
import ociautoscaling.Model.GroupInfo;
import ociautoscaling.Model.Result;
import ociautoscaling.Model.SubnetAndInstanceCount;
import ociautoscaling.Service.ComputeService;
import ociautoscaling.Service.LoadBalanceService;
import ociautoscaling.Service.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ScaleController {
    private ComputeService computeService;
    private LoadBalanceService lbService;
    private NetworkService networkService;

    @Autowired
    public ScaleController(ComputeService computeService, LoadBalanceService lbService, NetworkService networkService) {
        this.computeService = computeService;
        this.lbService = lbService;
        this.networkService = networkService;
    }

    @RequestMapping(value = "/main")
    public String main(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            return "login";
        }
        Map<String, GroupInfo> gMap = new HashMap<>();
        try {
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        List<GroupInfo> list = new ArrayList<>(gMap.values());
        model.addAttribute("groupInfo", list);
        return "main";
    }

    @GetMapping("/scale")
    @ResponseBody
    public Result<Integer> scale(@RequestParam(value = "groupName") String groupName, @RequestParam(value = "num") int num) {
        if (num == 0) {
            return new Result<>(true, 0, "");
        }
        int succ = 0;
        boolean scalein = false;
        String action = "start";
        if (num < 0) {
            scalein = true;
            action = "stop";
        }
        try {
            List<Instance> scalableList = computeService.getScalableInstances(groupName, scalein);
            if (scalableList != null && scalableList.size() > 0) {
                for (Instance i : scalableList) {
                    String loadBalancerSucc = "";
                    String lbId = i.getFreeformTags().get("loadbalancer");
                    String backendSet = i.getFreeformTags().get("backendset");
                    String ip;
                    ip = networkService.getPrivateIpByInstanceId(i.getId());
                    //todo shoud move port to configuration file for versatility
                    int port = 8080;
                    String backendName = ip + ":" + String.valueOf(port);
                    if (lbId == null || backendSet == null || ip.equals("")) continue;
                    //scalein should remove backend from lb first.
                    if (scalein) {
                        loadBalancerSucc = lbService.drainBackend(lbId, backendSet, backendName);
                        if (!loadBalancerSucc.equals("")) {
                            //execute after 2 minutes.
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
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result<>(false, 0, "Operation Failed! Reason:" + ex.getMessage());
        }
        return new Result<>(true, succ, "Operation Succeed! Scaling " + succ + " Server(s)");
    }

    @GetMapping("/createAuxiliary")
    @ResponseBody
    public Result<Integer> createAuxiliary(@RequestParam(value = "groupName") String groupName, @RequestParam(value = "num") int num) {
        int i = 0;
        try {
            Instance regular = computeService.getAnRegularInstance(groupName);
            if (regular != null) {
                //select AD to create auxiliary servers.
                Vcn vcn = networkService.getVncByInstance(regular);
                List<Subnet> snList = networkService.getSubnetsInVcn(vcn.getId());
                List<SubnetAndInstanceCount> list = computeService.getOrderedSubnet(groupName, snList);
                for (; i < num; ) {
                    Subnet sn = list.get(i % list.size()).getSn();
                    Instance newInstance = computeService.createAuxiliaryInstance(regular, sn);
                    if (newInstance == null) {
                        break;
                    } else {
                        i++;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result<>(false, i, "Operation Failed! Reason:" + ex.getMessage());
        }
        return new Result<>(true, i, "Operation Succeed! Creating " + i + " Server(s)");
    }
}
