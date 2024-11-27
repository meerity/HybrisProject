package com.epam.training.service.impl;

import com.epam.training.model.MatchModel;
import com.epam.training.model.PlayerModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.tx.Transaction;

import java.util.Collections;

public class PlayerAndMatchService {

    private ModelService modelService;

    public void createPlayerAndMatchTransaction() throws Exception {
        Transaction.current().execute(() -> {
            PlayerModel player = modelService.create(PlayerModel.class);
            player.setName("Player 1");
            player.setPlayerCode("somecode");

            MatchModel match = modelService.create(MatchModel.class);
            match.setName("Match 1");

            match.setPlayers(Collections.singleton(player));
            player.setMatches(Collections.singleton(match));

            modelService.saveAll(player, match);
            return null;
        });
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
