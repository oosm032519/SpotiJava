package data.playlists;

import authorization.client_credentials.ClientCredentials;
import io.github.cdimascio.dotenv.Dotenv;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class PrintPlaylistsItems {
    static Dotenv dotenv = Dotenv.load();
    private static final String accessToken = ClientCredentials.clientCredentials();
    private static final Scanner scanner = new Scanner(System.in);

    private static String getPlaylistId() {
        System.out.println("プレイリストIDを入力してください（何も入力しない場合はデフォルトのIDが使用されます）:");
        String input = scanner.nextLine();
        return input.isEmpty() ? dotenv.get("PLAYLIST_ID") : input;
    }

    private static final String playlistId = getPlaylistId();
    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setAccessToken(accessToken)
            .build();

    private static List<PlaylistTrack> getPlaylistTracks() {
        List<PlaylistTrack> playlistTracks = new ArrayList<>();
        int offset = 0;
        Paging<PlaylistTrack> playlistTrackPaging;

        do {
            try {
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
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.out.println("プレイリストの曲の取得中にエラーが発生しました: " + e.getMessage());
                break;
            }
        } while (playlistTrackPaging.getNext() != null);

        return playlistTracks;
    }

    private static void printTrackInfo(PlaylistTrack playlistTrack) {
        try {
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

            System.out.println(trackName + " | " + artistName + " | " + bpm + " | " + keyName + " | " + mode + " | " + acousticness + " | " + danceability + " | " + energy + " | " + liveness + " | " + valence);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("曲の情報の出力中にエラーが発生しました: " + e.getMessage());
        }
    }

    public static void printPlaylistsItems() {
        List<PlaylistTrack> playlistTracks = getPlaylistTracks();
        System.out.println("Total: " + playlistTracks.size());
        System.out.println("楽曲名 | アーティスト名 | BPM | Key | Mode | アコースティックス | ダンサビリティ | 元気さ | ライブ感 | 明るさ");

        for (PlaylistTrack playlistTrack : playlistTracks) {
            printTrackInfo(playlistTrack);
        }
    }

    public static void main(String[] args) {
        printPlaylistsItems();
    }
}
