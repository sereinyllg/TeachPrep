package com.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.service.domain.dto.PageDTO;
import com.service.domain.entity.File;
import com.service.domain.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService extends IService<File> {
    Object getFilePage(PageDTO page);

    FileVO uploadFile(String userId, MultipartFile file);
}
