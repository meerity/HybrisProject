package com.epam.training.service.impl;

import com.epam.training.service.RegistrationService;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * A stub class for Process task
 */
public class StubRegistrationService implements RegistrationService {

    private static final Logger LOG = Logger.getLogger(StubRegistrationService.class);
    private static final String PROCESS_DEFINITION_NAME = "SendEmailProcess";

    private ModelService modelService;
    private BusinessProcessService businessProcessService;

    @Override
    public void registerUser(UserModel user) {
        LOG.info("Registering user " + user.getName());
        modelService.save(user);

        String email = Optional.of(user)
                .map(UserModel::getAddresses)
                .stream()
                .flatMap(Collection::stream)
                .map(AddressModel::getEmail)
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse(null);

        if (email != null) {
            launchSendEmailProcess(email);
        } else {
            LOG.warn("No valid email found for user: " + user.getUid());
        }
    }

    private void launchSendEmailProcess(String email){
        String processCode = "SendEmailProcess-" + System.currentTimeMillis();
        BusinessProcessModel process = businessProcessService.createProcess(
                processCode,
                PROCESS_DEFINITION_NAME
        );
        BusinessProcessParameterModel emailParam = new BusinessProcessParameterModel();
        emailParam.setName("email");
        emailParam.setValue(email);
        process.setContextParameters(Collections.singletonList(emailParam));

        modelService.save(process);
        businessProcessService.startProcess(process);
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public void setBusinessProcessService(BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }
}
