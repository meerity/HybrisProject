/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import com.epam.training.constants.TrainingsmarteditConstants;
import org.apache.log4j.Logger;

@SuppressWarnings("PMD")
public class TrainingsmarteditManager extends GeneratedTrainingsmarteditManager
{
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger( TrainingsmarteditManager.class.getName() );
	
	public static final TrainingsmarteditManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (TrainingsmarteditManager) em.getExtension(TrainingsmarteditConstants.EXTENSIONNAME);
	}
	
}
