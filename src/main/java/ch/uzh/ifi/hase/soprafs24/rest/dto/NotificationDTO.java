package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class NotificationDTO {
    private String type;
    private String message;
    private String link;

    public NotificationDTO() {}

    public NotificationDTO(String type, String message, String link) {
        this.type = type;
        this.message = message;
        this.link = link;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
