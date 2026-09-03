package org.example.myapp.dto;

public class PresignResponseDTO {
    public String uploadUrl; // URL used by frontend to PUT the file
    public String fileUrl;   // final public URL to store in MessageAttachment
}
