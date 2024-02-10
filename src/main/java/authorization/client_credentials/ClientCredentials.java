package authorization.client_credentials;

import io.github.cdimascio.dotenv.Dotenv;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

public class ClientCredentials {
    static Dotenv dotenv = Dotenv.load();
    private static final String clientId;
    private static final String clientSecret;

    static {
        clientId = dotenv.get("CLIENT_ID");
        clientSecret = dotenv.get("CLIENT_SECRET");
        if (clientId == null || clientSecret == null) {
            throw new IllegalArgumentException("環境変数 CLIENT_ID または CLIENT_SECRET が設定されていません。");
        }
    }

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();
    private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
            .build();

    public static String clientCredentials() {
        try {
            final se.michaelthelin.spotify.model_objects.credentials.ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            // アクセストークンを設定
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            System.out.println("アクセストークン: " + clientCredentials.getAccessToken());
            System.out.println("有効期限: " + clientCredentials.getExpiresIn());

            // アクセストークンを文字列として返す
            return clientCredentials.getAccessToken();
        } catch (IOException e) {
            System.out.println("IOエラーが発生しました: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (SpotifyWebApiException e) {
            System.out.println("Spotify APIからエラーが返されました: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (ParseException e) {
            System.out.println("パースエラーが発生しました: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        clientCredentials();
    }
}
