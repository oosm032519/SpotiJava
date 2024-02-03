package data.playlists;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Episode;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetPlaylistsItemsExample {
    private static final String accessToken = authorization.client_credentials.ClientCredentialsExample.clientCredentials_Sync();
    private static final String playlistId = "5FnRflOUzM0LboxOnscTsZ";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setAccessToken(accessToken)
            .build();

    public static void getPlaylistsItems_Sync() {
        try {
            // プレイリストの曲を格納するリストを作成
            List<PlaylistTrack> playlistTracks = new ArrayList<>();
            // 最初のリクエストを作成
            GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi
                    .getPlaylistsItems(playlistId)
                    .limit(100) // 一度に取得できる最大数
                    .offset(0) // 最初のページ
                    .build();
            // 最初のページの曲を取得
            Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();
            // リストに追加
            playlistTracks.addAll(List.of(playlistTrackPaging.getItems()));
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
            // リストの全ての曲の曲名とアーティスト名を出力
            for (PlaylistTrack playlistTrack : playlistTracks) {
                // 曲のオブジェクトを取得
                Track track = (Track) playlistTrack.getTrack();
                // 曲名を取得
                String trackName = track.getName();
                // アーティスト名を取得
                String artistName = track.getArtists()[0].getName();
                // 曲名とアーティスト名を出力
                System.out.println(trackName + " by " + artistName);
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        getPlaylistsItems_Sync();
    }
}
