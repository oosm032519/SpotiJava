package data.playlists;

import authorization.client_credentials.ClientCredentials;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.Modality;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GetPlaylistsItems {
    private static final String accessToken = ClientCredentials.clientCredentials_Sync();
    private static final String playlistId = "4yTb5kNnV6FB8Vnabzcvy3";
    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setAccessToken(accessToken)
            .build();

    // プレイリストの曲を取得するメソッド
    private static List<PlaylistTrack> getPlaylistTracks() throws IOException, SpotifyWebApiException, ParseException {
        List<PlaylistTrack> playlistTracks = new ArrayList<>();
        int offset = 0;
        Paging<PlaylistTrack> playlistTrackPaging;

        do {
            GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi
                    .getPlaylistsItems(playlistId)
                    .limit(100)
                    .offset(offset)
                    .build();

            playlistTrackPaging = getPlaylistsItemsRequest.execute();
            playlistTracks.addAll(List.of(playlistTrackPaging.getItems()));

            if (playlistTrackPaging.getNext() != null) {
                offset = Integer.parseInt(playlistTrackPaging.getNext().split("offset=|&")[1]);
            }
        } while (playlistTrackPaging.getNext() != null);

        return playlistTracks;
    }

    // 曲の情報を出力するメソッド
    private static void printTrackInfo(PlaylistTrack playlistTrack) throws IOException, SpotifyWebApiException, ParseException {
        Track track = (Track) playlistTrack.getTrack();
        String trackName = track.getName();
        String artistName = track.getArtists()[0].getName();
        String trackId = track.getId();

        GetAudioFeaturesForTrackRequest getAudioFeaturesForTrackRequest = spotifyApi.getAudioFeaturesForTrack(trackId)
                .build();

        AudioFeatures audioFeatures = getAudioFeaturesForTrackRequest.execute();

        float bpm = audioFeatures.getTempo();
        if (bpm <= 115) {
            bpm *= 2;
        }
        bpm = Math.round(bpm);

        int key = audioFeatures.getKey();
        String[] keyNames = {"C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"};
        String keyName = keyNames[key];

        Modality mode = audioFeatures.getMode();
        float acousticness = audioFeatures.getAcousticness();
        float danceability = audioFeatures.getDanceability();
        float energy = audioFeatures.getEnergy();
        float liveness = audioFeatures.getLiveness();
        float valence = audioFeatures.getValence();

        try (PrintWriter writer = new PrintWriter(new FileWriter("曲情報.txt", true))) {
            writer.println(trackName + " | " + artistName + " | " + bpm + " | " + keyName + " | " + mode + " | " + acousticness + " | " + danceability + " | " + energy + " | " + liveness + " | " + valence);
        }
    }

    public static void getPlaylistsItems_Sync() {
        try {
            List<PlaylistTrack> playlistTracks = getPlaylistTracks();
            try (PrintWriter writer = new PrintWriter(new FileWriter("曲情報.txt", true))) {
                writer.println("Total: " + playlistTracks.size());
                writer.println("楽曲名 | アーティスト名 | BPM | Key | Mode | アコースティックス | ダンサビリティ | 元気さ | ライブ感 | 明るさ");
            }

            for (PlaylistTrack playlistTrack : playlistTracks) {
                printTrackInfo(playlistTrack);
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        getPlaylistsItems_Sync();
    }
}
