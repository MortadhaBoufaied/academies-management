package com.footballacademy.DTO.chat;

/**  * A contact list item can be a USER or a GROUP.  * kind = USER: id is User.id  * kind = GROUP: id is synthetic (negative), conversationId is Conversation.id  */
public
record ContactDTO(Long id, String name, String email, String role, Long divisionId, String kind, Long conversationId) {
    public static ContactDTO user(Long id, String name, String email, String role, Long divisionId) {
        return new ContactDTO(id, name, email, role, divisionId, "USER", null);
    }
    public static ContactDTO group(Long syntheticId, String title, Long divisionId, Long conversationId) {
        return new ContactDTO(syntheticId, title, null, null, divisionId, "GROUP", conversationId);
    }
}
