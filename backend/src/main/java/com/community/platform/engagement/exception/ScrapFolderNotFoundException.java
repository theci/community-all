package com.community.platform.engagement.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 스크랩 폴더를 찾을 수 없을 때 발생하는 예외
 */
public class ScrapFolderNotFoundException extends BusinessException {
    
    public ScrapFolderNotFoundException(Long folderId) {
        super("SCRAP_FOLDER_NOT_FOUND", "스크랩 폴더를 찾을 수 없습니다. ID: " + folderId);
    }
}