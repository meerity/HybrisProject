/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.epam.training.core.exceptions;



public class UnknownResourceException extends RuntimeException
{
	public UnknownResourceException(final String msg)
	{
		super(msg);
	}
}
