package org.seaPack.service.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档生成结果
 * <p>DocumentGenerator 返回此对象，包含生成文件的 URL、文件名和大小。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedFile {

    /** 文件访问 URL（如 /files/xxx.pdf） */
    private String url;

    /** 原始文件名（含扩展名） */
    private String fileName;

    /** 文件大小（字节） */
    private long fileSize;
}
