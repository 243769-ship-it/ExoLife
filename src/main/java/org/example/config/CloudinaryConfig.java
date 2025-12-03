package org.example.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class CloudinaryConfig {

    private static Cloudinary cloudinary;

    static {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dxdqz9yz2",
                "api_key", "338387218691892",
                "api_secret", "KJ7ZAAIX08W3w0fDEBNiZ2ipfyU",
                "secure", true
        ));
    }

    public static Cloudinary getInstance() {
        return cloudinary;
    }
}
