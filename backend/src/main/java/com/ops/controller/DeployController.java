package com.ops.controller;

import com.ops.common.Result;
import com.ops.service.HotDeployService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/deploy")
@RequiredArgsConstructor
public class DeployController {

    private final HotDeployService hotDeployService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(hotDeployService.uploadFile(file));
    }

    @PostMapping("/hot")
    public Result<Void> hotDeploy(
            @RequestParam Long clusterId,
            @RequestParam String namespace,
            @RequestParam String podName,
            @RequestParam String containerName,
            @RequestParam String fileId,
            @RequestParam String className,
            @RequestParam(required = false) String methodName) {
        hotDeployService.deploy(clusterId, namespace, podName, containerName, fileId, className, methodName);
        return Result.success();
    }
}
