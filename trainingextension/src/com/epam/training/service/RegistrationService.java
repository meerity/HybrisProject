package com.epam.training.service;

import de.hybris.platform.core.model.user.UserModel;

/**
 * An interface for user registration services
 */
public interface RegistrationService {

    /**
     * Simple registration method
     * @param user user model from facade
     */
    void registerUser(UserModel user);
}
