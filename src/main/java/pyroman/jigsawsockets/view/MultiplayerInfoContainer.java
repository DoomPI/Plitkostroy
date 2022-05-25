package pyroman.jigsawsockets.view;

import pyroman.jigsawsockets.connection.client.Client;

import java.time.LocalTime;

public record MultiplayerInfoContainer(Client client, LocalTime playingTime, String nickname, String partnerNickname) {

    @Override
    public String toString() {
        return "lobbyClient";
    }
}
