package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class UserUpdateDTO {

    private String username;
    private String password;
    private String birthday;
    private String email;
    private String biography;
    private Boolean sharable;
    private Boolean publicRatings;
    private String profilePictureUrl;
    private String avatarKey;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public Boolean getSharable() {
        return sharable;
    }

    public void setSharable(Boolean sharable) {
        this.sharable = sharable;
    }

    public Boolean getPublicRatings() {
        return publicRatings;
    }

    public void setPublicRatings(Boolean publicRatings) {
        this.publicRatings = publicRatings;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getAvatarKey() {
        return avatarKey;
    }

    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
    }
}
