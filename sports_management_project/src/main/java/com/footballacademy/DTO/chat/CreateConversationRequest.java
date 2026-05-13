package com.footballacademy.DTO.chat;

import java.util.List;

/**  * Request body for POST /api/chat/conversations  * Expected JSON example:  * {  *   "participantIds": [1, 42],  *   "title": "My chat"  * }  */
public
class CreateConversationRequest {
    private List<Long> participantIds;
    private String title;
    public CreateConversationRequest() {
    }
    public CreateConversationRequest(List<Long> participantIds, String title) {
        this.participantIds = participantIds;
        this.title = title;
    }
    public List<Long> getParticipantIds() {
        return participantIds;
    }
    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
