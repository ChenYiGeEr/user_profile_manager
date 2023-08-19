package com.atguigu.userprofile.service;

import org.springframework.web.multipart.MultipartFile;

public interface HdfsService {

    Long createFile(String path, MultipartFile file);
}
