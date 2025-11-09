package org.example.game.ai;

import org.example.game.GameInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AiRegistry {

    private static final Map<GameInfo, AiController> CONTROLLERS = new ConcurrentHashMap<>();

    private AiRegistry() {
    }

    public static void register(GameInfo info, AiController controller) {
        if (info != null && controller != null) {
            CONTROLLERS.put(info, controller);
        }
    }

    public static void unregister(GameInfo info) {
        if (info != null) {
            CONTROLLERS.remove(info);
        }
    }

    public static void onTurnStart(GameInfo info) {
        AiController controller = CONTROLLERS.get(info);
        if (controller != null) {
            controller.onTurnStart(info);
        }
    }
}

