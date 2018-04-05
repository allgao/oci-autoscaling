package ociautoscaling.controller;

import com.oracle.bmc.core.model.Instance;
import ociautoscaling.Model.GroupInfo;
import ociautoscaling.Service.ComputeService;
import ociautoscaling.Service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LoginController {
    @Autowired
    LoginService service;
    @Autowired
    ComputeService computeService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String login() {
        return "login";
    }

    @RequestMapping(value = "/main", method = RequestMethod.GET)
    public String main(Model model) {
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
        model.addAttribute("groupInfo", list);
        return "main";
    }

    @PostMapping(value = "/login")
    @ResponseBody
    public String login(Model model, @RequestParam String name, @RequestParam String password) {
        boolean isValidUser = service.validateUser(name, password);
        if (!isValidUser) {
            model.addAttribute("errorMessage", "Invalid Credentials");
            return "/";
        }
        return "main";
    }

}
