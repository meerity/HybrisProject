package com.epam.training.listeners;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.event.events.AfterItemCreationEvent;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AddressAfterSaveListener extends AbstractEventListener<AfterItemCreationEvent> {

    private static final Logger LOG = Logger.getLogger(AddressAfterSaveListener.class);
    private static final String PROCESS_DEFINITION_NAME = "AddressDefaultProcess";

    private BusinessProcessService businessProcessService;

    @Override
    protected void onEvent(AfterItemCreationEvent event) {
        if (event.getSource() instanceof AddressModel address) {
            try {
                launchAddressDefaultProcess(address);
            } catch (Exception e) {
                LOG.error("Failed to launch address process", e);
            }
        }
    }

    private void launchAddressDefaultProcess(AddressModel address) {
        String processCode = "addressDefaultProcess-" + System.currentTimeMillis();
        Map<String, Object> variables = new HashMap<>();
        variables.put("address", address);

        businessProcessService.startProcess(
                processCode,
                PROCESS_DEFINITION_NAME,
                variables
        );
    }

    public void setBusinessProcessService(BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }

}