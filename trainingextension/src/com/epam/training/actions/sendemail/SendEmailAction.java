package com.epam.training.actions.sendemail;

import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SendEmailAction extends AbstractSimpleDecisionAction<BusinessProcessModel> {

    private static final Logger LOG = LogManager.getLogger(SendEmailAction.class);

    @Override
    public Transition executeAction(BusinessProcessModel businessProcessModel) {
        try {
            String email = businessProcessModel.getContextParameters()
                    .stream()
                    .filter(param -> param.getName().equals("email"))
                    .map(BusinessProcessParameterModel::getValue)
                    .map(String::valueOf)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Email parameter not found"));

            LOG.info("Sending email to " + email);
            return Transition.OK;
        } catch (Exception e) {
            LOG.error("Failed to send email", e);
            return Transition.NOK;
        }
    }
}
