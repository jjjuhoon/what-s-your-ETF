package back.whats_your_ETF.dto;


public record NoticeResponse(
        Long id,          // 알림 ID
        String content,   // 알림 내용
        boolean isRead,   // 읽음 여부
        Long userId       // 사용자 ID
) {}
