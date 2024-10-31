/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.service;

public interface TrainingextensionService
{
	String getHybrisLogoUrl(String logoCode);

	void createLogo(String logoCode);
}
