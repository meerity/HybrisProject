package com.epam.training.service.impl;

import com.epam.training.model.MatchModel;
import com.epam.training.model.PlayerModel;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@IntegrationTest
public class PlayerAndMatchServiceIntegrationTest extends ServicelayerTransactionalTest {

    @Resource
    private PlayerAndMatchService playerAndMatchService;

    @Resource
    private ModelService modelService;

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Before
    public void setUp() {
        playerAndMatchService.setModelService(modelService);
    }

    @Test
    public void shouldCreatePlayerAndMatchInTransaction() throws Exception {
        // when
        playerAndMatchService.createPlayerAndMatchTransaction();

        // then
        String playerQuery = "SELECT {" + PlayerModel.PK + "} FROM {" + PlayerModel._TYPECODE + "} " +
                "WHERE {" + PlayerModel.PLAYERCODE + "}=?playerCode";
        FlexibleSearchQuery fsqPlayer = new FlexibleSearchQuery(playerQuery);
        fsqPlayer.addQueryParameter("playerCode", "somecode");
        PlayerModel foundPlayer = flexibleSearchService.<PlayerModel>search(fsqPlayer).getResult().get(0);

        String matchQuery = "SELECT {" + MatchModel.PK + "} FROM {" + MatchModel._TYPECODE + "} " +
                "WHERE {" + MatchModel.NAME + "}=?name";
        FlexibleSearchQuery fsqMatch = new FlexibleSearchQuery(matchQuery);
        fsqMatch.addQueryParameter("name", "Match 1");
        MatchModel foundMatch = flexibleSearchService.<MatchModel>search(fsqMatch).getResult().get(0);

        // verify player
        assertNotNull(foundPlayer);
        assertEquals("Player 1", foundPlayer.getName());
        assertEquals("somecode", foundPlayer.getPlayerCode());
        assertEquals(1, foundPlayer.getMatches().size());

        // verify match
        assertNotNull(foundMatch);
        assertEquals("Match 1", foundMatch.getName());
        assertEquals(1, foundMatch.getPlayers().size());

        // verify relationships
        assertTrue(foundMatch.getPlayers().contains(foundPlayer));
        assertTrue(foundPlayer.getMatches().contains(foundMatch));
    }

}