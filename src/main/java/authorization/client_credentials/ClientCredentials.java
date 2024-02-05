package authorization.client_credentials;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

public class ClientCredentials {
    private static final String clientId = "75fbfb16c5444e02bc6453caea24bcdd";
    private static final String clientSecret = "635faf496b964337937c2d26d92d5dda";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();
    private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
            .build();

    public static String clientCredentials_Sync() {
        try {
            final se.michaelthelin.spotify.model_objects.credentials.ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            System.out.println("Access token: " + clientCredentials.getAccessToken());
            System.out.println("Expires in: " + clientCredentials.getExpiresIn());

            // Return access token as String
            return clientCredentials.getAccessToken();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return null; // Return null if an exception occurs
        }
    }


    public static void main(String[] args) {
        clientCredentials_Sync();
    }
}
