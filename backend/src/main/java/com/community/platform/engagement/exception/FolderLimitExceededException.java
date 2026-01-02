package com.community.platform.engagement.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 스크랩 폴더 개수 제한 초과 시 발생하는 예외
 */
public class FolderLimitExceededException extends BusinessException {
    
    public FolderLimitExceededException(int maxFolders) {
        super("FOLDER_LIMIT_EXCEEDED", 
            String.format("스크랩 폴더는 최대 %d개까지 생성할 수 있습니다", maxFolders));
    }
}