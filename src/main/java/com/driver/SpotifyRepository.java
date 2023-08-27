package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {

        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {

        for(Artist artist: artists) {
            if(artistName.equals(artist.getName())) {
                Album newAlbum = new Album(title);
                albums.add(newAlbum);

                if(!artistAlbumMap.containsKey(artist)) artistAlbumMap.put(artist, new ArrayList<>());
                artistAlbumMap.get(artist).add(newAlbum);
                return newAlbum;
            }
        }

        Artist newArtist = new Artist(artistName);
        artists.add(newArtist);

        Album newAlbum = new Album(title);
        albums.add(newAlbum);

        artistAlbumMap.put(newArtist, new ArrayList<>());
        artistAlbumMap.get(newArtist).add(newAlbum);

        return newAlbum;

    }

    public Song createSong(String title, String albumName, int length) throws Exception{

        for(Album album: albums) {
            if(album.getTitle().equals(albumName)){

                Song newSong = new Song(title, length);
                songs.add(newSong);

                if(!albumSongMap.containsKey(album)) {
                    albumSongMap.put(album, new ArrayList<>());
                }
                albumSongMap.get(album).add(newSong);
                return newSong;
            }
        }

        throw new Exception("Album does not exist");

    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {

        for(User user: users){
            if(user.getName().equals(mobile)) {

                Playlist playlist = new Playlist(title);
                playlists.add(playlist);

                for(Song song : songs) {
                    if(song.getLength() == length) {
                        if(playlistSongMap.containsKey(playlist)){
                            playlistSongMap.get(playlist).add(song);
                        }else {
                            playlistSongMap.put(playlist, new ArrayList<>());
                            playlistSongMap.get(playlist).add(song);
                        }
                    }
                }

                creatorPlaylistMap.put(user, playlist);
                playlistListenerMap.put(playlist, new ArrayList<>());
                playlistListenerMap.get(playlist).add(user);

                if(userPlaylistMap.containsKey(user)) {
                    userPlaylistMap.get(user).add(playlist);
                }else{
                    List<Playlist> playlistList = new ArrayList<>();
                    playlistList.add(playlist);
                    userPlaylistMap.put(user, playlistList);
                }

                return playlist;
            }
        }

        throw new Exception("User does not exist");
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {

        for(User user: users){
            if(user.getName().equals(mobile)) {

                Playlist playlist = new Playlist(title);
                playlists.add(playlist);

                for(Song song : songs) {
                    if(song.getTitle().equals(title)) {
                        if(playlistSongMap.containsKey(playlist)){
                            playlistSongMap.get(playlist).add(song);
                        }else {
                            playlistSongMap.put(playlist, new ArrayList<>());
                            playlistSongMap.get(playlist).add(song);
                        }
                    }
                }

                creatorPlaylistMap.put(user, playlist);
                playlistListenerMap.put(playlist, new ArrayList<>());
                playlistListenerMap.get(playlist).add(user);

                if(userPlaylistMap.containsKey(user)) {
                    userPlaylistMap.get(user).add(playlist);
                }else{
                    List<Playlist> playlistList = new ArrayList<>();
                    playlistList.add(playlist);
                    userPlaylistMap.put(user, playlistList);
                }

                return playlist;
            }
        }

        throw new Exception("User does not exist");
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //Find the playlist with given title and add user as listener of that playlist and update user accordingly
        //If the user is creater or already a listener, do nothing
        //If the user does not exist, throw "User does not exist" exception
        //If the playlist does not exists, throw "Playlist does not exist" exception
        // Return the playlist after updatingd


        User currUser = null;
        Playlist currPlaylist = null;

        for(User user: users) {
            if(user.getName().equals(mobile)) {
                currUser = user;
                break;
            }
        }
        if(currUser == null) throw new Exception("User does not exist");

        for(Playlist playlist: playlists) {
            if(playlist.getTitle().equals(playlistTitle)){
                currPlaylist = playlist;
                break;
            }
        }

        if(currPlaylist == null) throw new Exception("Playlist does not exist");

        if(creatorPlaylistMap.containsKey(currUser) &&
                creatorPlaylistMap.get(currPlaylist).getTitle().equals(playlistTitle)) return currPlaylist;

        List<User> list = playlistListenerMap.get(currPlaylist);
        for(User user: list) {
            if(user.getName().equals(mobile)) {
                return currPlaylist;
            }
        }

        playlistListenerMap.get(currPlaylist).add(currUser);
        userPlaylistMap.get(currUser).add(currPlaylist);

        return currPlaylist;

    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        //The user likes the given song. The corresponding artist of the song gets auto-liked
        //A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
        //However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
        //If the user does not exist, throw "User does not exist" exception
        //If the song does not exist, throw "Song does not exist" exception
        //Return the song after updating

        User currUser = null;
        Song currSong = null;
        for(User user: users) {
            if(user.getName().equals(mobile)){
                currUser = user;
                break;
            }
        }
        if(currUser == null) throw new Exception("User does not exist");

        for(Song song: songs) {
            if(song.getTitle().equals(songTitle)) {
                currSong = song;
                break;
            }
        }

        if(currSong == null) throw new Exception("Song does not exist");

        if(!songLikeMap.containsKey(currSong)){
            List<User> listOfUserLikeSong = new ArrayList<>();
            listOfUserLikeSong.add(currUser);
            songLikeMap.put(currSong, listOfUserLikeSong);
            return currSong;
        }else{

            List<User> userLikingSong = songLikeMap.get(currSong);

            for(User user: userLikingSong) {
                if(user.getName().equals(mobile)) {
                    return currSong;
                }
            }

            userLikingSong.add(currUser);
            return currSong;
        }
    }

    public String mostPopularArtist() {

        int noOfLikes = 0;
        String mostPopularArtist = "";

        for(Artist artist : artistAlbumMap.keySet()){

            List<Album> albums1 = artistAlbumMap.get(artist);
            int currNoOfLikes = 0;
            for(Album album: albums1) {

                List<Song> songs1 = albumSongMap.get(album);
                int likes = songLikeMap.get(songs1).size();
                currNoOfLikes += likes;
            }

            if(currNoOfLikes > noOfLikes) {
                noOfLikes = currNoOfLikes;
                mostPopularArtist = artist.getName();
            }
        }

        return mostPopularArtist;
    }

    public String mostPopularSong() {

        int noOfUser = 0;
        String mostPopularSong = "";

        for(Song song: songLikeMap.keySet()) {

            if(noOfUser < songLikeMap.get(song).size()){
                noOfUser = songLikeMap.get(song).size();
                mostPopularSong = song.getTitle();
            }
        }

        return mostPopularSong;
    }

}
