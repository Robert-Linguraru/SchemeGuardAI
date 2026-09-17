package org.schemeguard.backend.repository.CardScheme;

import java.util.UUID;

public interface CardSchemeLifecycleRepository {

    void activateCascade(UUID cardSchemeId);

    void deactivateCascade(UUID cardSchemeId);

    void deleteCascade(UUID cardSchemeId);
}
