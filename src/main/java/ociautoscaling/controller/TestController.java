package ociautoscaling.controller;

import ociautoscaling.Service.ComputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {
    @Autowired
    private ComputeService service;

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        int i=service.getScalableInstances("group1",false).size();
        return String.valueOf(i);
    }
}
