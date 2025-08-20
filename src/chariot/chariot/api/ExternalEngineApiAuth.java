package chariot.api;

import chariot.model.*;

public interface ExternalEngineApiAuth extends ExternalEngineApi {

    Many<ExternalEngineInfo> list();
    One<ExternalEngineInfo>  create(ExternalEngineRegistration registration);
    /**
     * @param engineId The external engine id. Example: {@code eei_aTKImBJOnv6j}
     */
    One<ExternalEngineInfo>  get(String engineId);
    /**
     * @param engineId The external engine id. Example: {@code eei_aTKImBJOnv6j}
     */
    One<ExternalEngineInfo>  update(String engineId, ExternalEngineRegistration registration);
    /**
     * @param engineId The external engine id. Example: {@code eei_aTKImBJOnv6j}
     */
    Ack                 delete(String engineId);
}
