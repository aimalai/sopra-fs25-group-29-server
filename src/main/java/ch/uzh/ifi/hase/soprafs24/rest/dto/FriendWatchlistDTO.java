package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class FriendWatchlistDTO {
    private Long friendId;
    private String username;
    private List<String> watchlist;

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getWatchlist() {
        return watchlist;
    }

    public void setWatchlist(List<String> watchlist) {
        this.watchlist = watchlist;
    }
}  
