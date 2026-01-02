export enum AttachmentType {
  IMAGE = 'IMAGE',
  VIDEO = 'VIDEO',
  AUDIO = 'AUDIO',
  DOCUMENT = 'DOCUMENT',
  OTHER = 'OTHER',
}

export interface Attachment {
  id: number;
  postId: number;
  fileName: string;
  originalName: string;
  fileType: AttachmentType;
  filePath: string;
  fileUrl: string;
  fileSize: number;
  mimeType: string;
  displayOrder: number;
}
