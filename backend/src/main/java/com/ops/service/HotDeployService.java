package com.ops.service;

import org.springframework.web.multipart.MultipartFile;

public interface HotDeployService {
    String uploadFile(MultipartFile file);
    void deploy(Long clusterId, String namespace, String podName,
                String containerName, String fileId, String className, String methodName);
}
