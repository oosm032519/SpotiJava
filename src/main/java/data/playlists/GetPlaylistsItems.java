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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GetPlaylistsItems {
    static Dotenv dotenv = Dotenv.load();
    private static final String accessToken = ClientCredentials.clientCredentials();
    // ユーザーからの入力を受け取るためのScannerを作成
    private static final Scanner scanner = new Scanner(System.in);

    // プレイリストIDを取得するメソッド
    private static String getPlaylistId() {
        System.out.println("プレイリストIDを入力してください（何も入力しない場合はデフォルトのIDが使用されます）:");
        String input = scanner.nextLine();
        // 入力がない場合は環境変数から読み込んだデフォルトのIDを返す
        return input.isEmpty() ? dotenv.get("PLAYLIST_ID") : input;
    }

    // プレイリストIDを取得
    private static final String playlistId = getPlaylistId();
    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setAccessToken(accessToken)
            .build();

    // プレイリストの曲を取得するメソッド
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

    // 曲の情報をファイルに書き込むメソッド
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

            try (PrintWriter writer = new PrintWriter(new FileWriter("曲情報.txt", true))) {
                writer.println(trackName + " | " + artistName + " | " + bpm + " | " + keyName + " | " + mode + " | " + acousticness + " | " + danceability + " | " + energy + " | " + liveness + " | " + valence);
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("曲の情報の出力中にエラーが発生しました: " + e.getMessage());
        }
    }

    public static void getPlaylistsItems_Sync() {
        List<PlaylistTrack> playlistTracks = getPlaylistTracks();
        try (PrintWriter writer = new PrintWriter(new FileWriter("曲情報.txt", true))) {
            writer.println("Total: " + playlistTracks.size());
            writer.println("楽曲名 | アーティスト名 | BPM | Key | Mode | アコースティックス | ダンサビリティ | 元気さ | ライブ感 | 明るさ");
        } catch (IOException e) {
            System.out.println("ファイル書き込み中にエラーが発生しました: " + e.getMessage());
        }

        for (PlaylistTrack playlistTrack : playlistTracks) {
            printTrackInfo(playlistTrack);
        }
    }

    public static void main(String[] args) {
        getPlaylistsItems_Sync();
    }
}
