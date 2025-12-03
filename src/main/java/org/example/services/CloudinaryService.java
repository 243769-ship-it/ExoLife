package org.example.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.example.config.CloudinaryConfig;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudinaryService {

    private final Cloudinary cloudinary = CloudinaryConfig.getInstance();

    public Map uploadFile(File file, String folderName) throws IOException {
        return cloudinary.uploader().upload(file, ObjectUtils.asMap(
                "folder", folderName
        ));
    }
}
