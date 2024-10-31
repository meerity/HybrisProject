/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.core.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import com.epam.training.core.constants.TrainingCoreConstants;
import com.epam.training.core.setup.CoreSystemSetup;


/**
 * Do not use, please use {@link CoreSystemSetup} instead.
 * 
 */
public class TrainingCoreManager extends GeneratedTrainingCoreManager
{
	public static final TrainingCoreManager getInstance()
	{
		final ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (TrainingCoreManager) em.getExtension(TrainingCoreConstants.EXTENSIONNAME);
	}
}
