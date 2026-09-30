package com.tuition;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.Map;
import java.util.HashMap;

public class CloudinaryTest {
    public static void main(String[] args) throws Exception {
        String url = "cloudinary://156523962328761:jiqp29oEGfTS8fEL40fNK7qeNb0@ckvqyq2c";
        Cloudinary cloudinary = new Cloudinary(url);
        
        // Create a 1x1 pixel PNG in memory
        byte[] img = new byte[] {
            (byte)137, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82,
            0, 0, 0, 1, 0, 0, 0, 1, 8, 2, 0, 0, 0, (byte)144, 119, 83, (byte)222,
            0, 0, 0, 12, 73, 68, 65, 84, 8, (byte)215, 99, (byte)248, (byte)255, (byte)255,
            63, 0, 5, (byte)254, 2, (byte)254, (byte)220, (byte)204, 89, (byte)231, 0, 0, 0, 0,
            73, 69, 78, 68, (byte)174, 66, 96, (byte)130
        };
        
        Map params = ObjectUtils.asMap(
            "folder", "tuition/avatar",
            "resource_type", "image"
        );
        Map result = cloudinary.uploader().upload(img, params);
        System.out.println("RESULT JSON:");
        System.out.println(result);
    }
}
