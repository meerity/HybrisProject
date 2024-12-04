/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.trainingsmartedit;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

@UnitTest
public class TrainingsmarteditDummyTest {

    @Test
    public void Will_Pass_All_The_Time(){
        assertTrue(alwaysTrue());
    }

    private boolean alwaysTrue() {
        return true;
    }
}
