package cn.mojoup.ai.upload.domain;

/**
 * 存储类型枚举
 * 
 * @author matt
 */
public enum StorageType {
    
    /**
     * 本地文件系统存储
     */
    LOCAL("local", "本地文件系统"),
    
    /**
     * MinIO对象存储
     */
    MINIO("minio", "MinIO对象存储");
    
    private final String code;
    private final String description;
    
    StorageType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取存储类型
     */
    public static StorageType fromCode(String code) {
        for (StorageType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown storage type code: " + code);
    }
} 