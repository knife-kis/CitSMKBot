package io.cit.CitSMKBot.cache;


import io.cit.CitSMKBot.botapi.BotState;

public interface DataCache {
    void setUsersCurrentBotState(long userId, BotState botState);

    BotState getUsersCurrentBotState(long userId);

}
