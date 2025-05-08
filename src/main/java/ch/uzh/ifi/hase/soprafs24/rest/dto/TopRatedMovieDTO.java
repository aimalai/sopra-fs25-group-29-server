package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class TopRatedMovieDTO {
    private String movieId;
    private String title;
    private String posterPath;
    private double rating;
    private String friendUsername;

    public TopRatedMovieDTO() {}

    public TopRatedMovieDTO(String movieId, String title, String posterPath, double rating, String friendUsername) {
        this.movieId = movieId;
        this.title = title;
        this.posterPath = posterPath;
        this.rating = rating;
        this.friendUsername = friendUsername;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }
}
