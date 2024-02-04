package data.playlists;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.Modality;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetPlaylistsItemsExample {
    private static final String accessToken = authorization.client_credentials.ClientCredentialsExample.clientCredentials_Sync();
    private static final String playlistId = "4yTb5kNnV6FB8Vnabzcvy3";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setAccessToken(accessToken)
            .build();

    public static void getPlaylistsItems_Sync() {
        try {
            FileWriter fw = new FileWriter("曲情報.txt");
            // プレイリストの曲を格納するリストを作成
            // 最初のリクエストを作成
            GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi
                    .getPlaylistsItems(playlistId)
                    .limit(100) // 一度に取得できる最大数
                    .offset(0) // 最初のページ
                    .build();
            // 最初のページの曲を取得
            Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();
            // リストに追加
            List<PlaylistTrack> playlistTracks = new ArrayList<>(List.of(playlistTrackPaging.getItems()));
            // 次のページがあるかどうか判定
            while (playlistTrackPaging.getNext() != null) {
                // 次のページのURLからoffsetを取得
                int nextOffset = Integer.parseInt(playlistTrackPaging.getNext().split("offset=|&")[1]);
                // 次のページのリクエストを作成
                getPlaylistsItemsRequest = spotifyApi
                        .getPlaylistsItems(playlistId)
                        .limit(100)
                        .offset(nextOffset)
                        .build();
                // 次のページの曲を取得
                playlistTrackPaging = getPlaylistsItemsRequest.execute();
                // リストに追加
                playlistTracks.addAll(List.of(playlistTrackPaging.getItems()));
            }
            // リストのサイズを出力
            System.out.println("Total: " + playlistTracks.size());

            //各項目名を出力
            fw.write("楽曲名 | アーティスト名 | BPM | Key | Mode | アコースティックス | ダンサビリティ | 元気さ | ライブ感 | 明るさ");
            fw.write(System.lineSeparator());

            // リストの全ての曲の情報を出力
            for (PlaylistTrack playlistTrack : playlistTracks) {
                // 曲のオブジェクトを取得
                Track track = (Track) playlistTrack.getTrack();

                // 曲名を取得
                String trackName = track.getName();

                // アーティスト名を取得
                String artistName = track.getArtists()[0].getName();

                // 曲のIDを取得
                String trackId = track.getId();

                // GetAudioFeaturesForTrackRequestクラスのインスタンスを作り、曲のIDをセット
                GetAudioFeaturesForTrackRequest getAudioFeaturesForTrackRequest = spotifyApi.getAudioFeaturesForTrack(trackId)
                        .build();

                // executeメソッドを呼び出して、AudioFeaturesオブジェクトを取得
                AudioFeatures audioFeatures = getAudioFeaturesForTrackRequest.execute();

                // getTempoメソッドを呼び出して、BPMを取得
                float bpm = audioFeatures.getTempo();

                // BPMが115以下の場合は2倍にする
                if (bpm <= 115) {
                    bpm *= 2;
                }

                // BPMをfloat型からint型に変換
                bpm = Math.round(bpm);

                // getKeyメソッドを呼び出して、曲のキーを取得
                int key = audioFeatures.getKey();

                // キーを数字から音名に変換
                String[] keyNames = {"C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"};
                String keyName = keyNames[key];

                // getModeメソッドを呼び出して、曲の調を取得
                Modality mode = audioFeatures.getMode();

                // getAcousticnessメソッドを呼び出して、曲のアコースティック度合いを取得
                float acousticness = audioFeatures.getAcousticness();

                // getDanceabilityメソッドを呼び出して、曲のダンス性を取得
                float danceability = audioFeatures.getDanceability();

                // getEnergyメソッドを呼び出して、曲のエネルギー度合いを取得
                float energy = audioFeatures.getEnergy();

                // getLivenessメソッドを呼び出して、曲のライブ感を取得
                float liveness = audioFeatures.getLiveness();

                // getValenceメソッドを呼び出して、曲のポジティブ感を取得
                float valence = audioFeatures.getValence();

                // 曲の情報を書き込む
                fw.write(trackName + " | " + artistName + " | " + bpm + " | " + keyName + " | " + mode + " | " + acousticness + " | " + danceability + " | " + energy +  " | " + liveness + " | " + valence);
                fw.write(System.lineSeparator());
            }
            fw.close();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        getPlaylistsItems_Sync();
    }
}
