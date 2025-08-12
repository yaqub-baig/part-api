package com.autoadda.apis.part.service;


import com.autoadda.apis.part.entity.MediaFile;
import com.autoadda.apis.part.response.GenericResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaFileService {

    List<MediaFile> upload(List<MultipartFile> files) throws IOException;

    GenericResponse delete(Long mediaFileId);

    GenericResponse getMediaFileById(Long mediaFileId);
}
