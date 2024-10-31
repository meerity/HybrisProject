/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.setup;

import static com.epam.training.constants.TrainingextensionConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import com.epam.training.constants.TrainingextensionConstants;
import com.epam.training.service.TrainingextensionService;


@SystemSetup(extension = TrainingextensionConstants.EXTENSIONNAME)
public class TrainingextensionSystemSetup
{
	private final TrainingextensionService trainingextensionService;

	public TrainingextensionSystemSetup(final TrainingextensionService trainingextensionService)
	{
		this.trainingextensionService = trainingextensionService;
	}

	@SystemSetup(process = SystemSetup.Process.ALL, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		trainingextensionService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return TrainingextensionSystemSetup.class.getResourceAsStream("/trainingextension/sap-hybris-platform.png");
	}
}
