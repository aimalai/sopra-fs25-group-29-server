package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class UserUpdateDTO {

    private String username;
    private String password;
    private String birthday;

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
}
