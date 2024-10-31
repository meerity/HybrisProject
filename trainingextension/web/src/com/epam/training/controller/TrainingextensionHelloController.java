/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.controller;

import static com.epam.training.constants.TrainingextensionConstants.PLATFORM_LOGO_CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.epam.training.service.TrainingextensionService;


@Controller
public class TrainingextensionHelloController
{
	@Autowired
	private TrainingextensionService trainingextensionService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String printWelcome(final ModelMap model)
	{
		model.addAttribute("logoUrl", trainingextensionService.getHybrisLogoUrl(PLATFORM_LOGO_CODE));
		return "welcome";
	}
}
