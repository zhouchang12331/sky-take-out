package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequestMapping("/admin/common" )
@RestController
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    //使用oss文件上传
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);
        //获取原始文件名
        String originalFileName=file.getOriginalFilename();
        log.info("原始文件名：{}",originalFileName);

        String suffix=originalFileName.substring(originalFileName.lastIndexOf("."));
        String objectName = UUID.randomUUID().toString()+suffix;

        try {
            //1，调用阿里云上传方法
            String url=aliOssUtil.upload(file.getBytes(),objectName);
            return Result.success(url);
        } catch (IOException e) {
            log.info("文件上传失败");
        }
        //2,返回图片路径结果
        return Result.error(MessageConstant.UPLOAD_FAILED);

    }
}
